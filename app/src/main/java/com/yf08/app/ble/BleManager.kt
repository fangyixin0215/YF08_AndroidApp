package com.yf08.app.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * YF08 BLE 管理器：扫描、连接、GATT 服务发现、命令收发、状态/事件分发
 *
 * 命令帧: [cmd][seq][plen][payload...]
 * 应答帧: [cmd][seq][result][len][payload...]
 */
@SuppressLint("MissingPermission")
class BleManager(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val btAdapter: BluetoothAdapter? = btManager.adapter

    private var gatt: BluetoothGatt? = null
    private var writeChar: BluetoothGattCharacteristic? = null

    private var seqCounter: Byte = 0
    private val pending = ConcurrentHashMap<Int, suspend (ByteArray) -> Unit>()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _deviceState = MutableStateFlow<DeviceState?>(null)
    val deviceState: StateFlow<DeviceState?> = _deviceState.asStateFlow()

    private val _events = MutableSharedFlow<BleEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<BleEvent> = _events.asSharedFlow()

    private val _scanResults = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val scanResults: StateFlow<List<DiscoveredDevice>> = _scanResults.asStateFlow()

    private var scanCallback: ScanCallback? = null

    /* ==================== 扫描 ==================== */

    fun startScan() {
        if (!hasPermission()) return
        val scanner = btAdapter?.bluetoothLeScanner ?: return
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(UUID.fromString(BleProtocol.SERVICE_YF08)))
            .build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        val cb = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val name = result.device.name ?: return
                if (!name.startsWith(BleProtocol.NAME_PREFIX)) return
                val dev = DiscoveredDevice(
                    address = result.device.address,
                    name = name,
                    rssi = result.rssi
                )
                val list = _scanResults.value.toMutableList()
                val idx = list.indexOfFirst { it.address == dev.address }
                if (idx >= 0) list[idx] = dev else list.add(dev)
                _scanResults.value = list.sortedByDescending { it.rssi }
            }
        }
        scanCallback = cb
        try {
            scanner.startScan(listOf(filter), settings, cb)
        } catch (e: Exception) {
            Log.e(TAG, "startScan failed", e)
        }
    }

    fun stopScan() {
        btAdapter?.bluetoothLeScanner?.let { scanner ->
            scanCallback?.let {
                try { scanner.stopScan(it) } catch (_: Exception) {}
            }
        }
        scanCallback = null
        _scanResults.value = emptyList()
    }

    /* ==================== 连接 ==================== */

    fun connect(address: String) {
        if (!hasPermission()) return
        stopScan()
        _connectionState.value = ConnectionState.CONNECTING
        val device = btAdapter?.getRemoteDevice(address) ?: run {
            _connectionState.value = ConnectionState.DISCONNECTED
            return
        }
        gatt = device.connectGatt(context, false, gattCallback)
    }

    fun disconnect() {
        try {
            gatt?.disconnect()
            gatt?.close()
        } catch (_: Exception) {}
        gatt = null
        writeChar = null
        _connectionState.value = ConnectionState.DISCONNECTED
        _deviceState.value = null
        pending.clear()
    }

    /* ==================== GATT 回调 ==================== */

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    _connectionState.value = ConnectionState.CONNECTING
                    g.requestMtu(247)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    _connectionState.value = ConnectionState.DISCONNECTED
                    writeChar = null
                    _deviceState.value = null
                }
            }
        }

        override fun onMtuChanged(g: BluetoothGatt, mtu: Int, status: Int) {
            g.discoverServices()
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            val service = g.getService(UUID.fromString(BleProtocol.SERVICE_YF08)) ?: run {
                disconnect(); return
            }
            writeChar = service.getCharacteristic(UUID.fromString(BleProtocol.CHAR_WRITE))
            // 使能 4 个 notify 特征
            enableNotify(g, BleProtocol.CHAR_NOTIFY_RSP)
            enableNotify(g, BleProtocol.CHAR_NOTIFY_STATE)
            enableNotify(g, BleProtocol.CHAR_NOTIFY_EVENT)
            enableNotify(g, BleProtocol.CHAR_RECORD_NOTIFY)
            _connectionState.value = ConnectionState.CONNECTED
            // 连接成功后主动拉一次 State
            scope.launch { sendCommand(BleProtocol.CMD_GET_STATE, ByteArray(0)) }
        }

        override fun onCharacteristicChanged(
            g: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val value = characteristic.value ?: return
            when (characteristic.uuid.toString().uppercase()) {
                BleProtocol.CHAR_NOTIFY_RSP.uppercase() -> handleResponse(value)
                BleProtocol.CHAR_NOTIFY_STATE.uppercase() -> {
                    if (value.size >= BleProtocol.STATE_V01_LEN) {
                        _deviceState.value = DeviceState.fromBytes(value)
                    }
                }
                BleProtocol.CHAR_NOTIFY_EVENT.uppercase() -> handleEvent(value)
            }
        }

        override fun onCharacteristicWrite(
            g: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            // 写成功后等待 onCharacteristicChanged 应答
        }
    }

    private fun enableNotify(g: BluetoothGatt, charUuid: String) {
        val service = g.getService(UUID.fromString(BleProtocol.SERVICE_YF08)) ?: return
        val ch = service.getCharacteristic(UUID.fromString(charUuid)) ?: return
        g.setCharacteristicNotification(ch, true)
        val ccc = ch.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805F9B34FB"))
        ccc?.let {
            it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            g.writeDescriptor(it)
        }
    }

    /* ==================== 帧处理 ==================== */

    private fun handleResponse(data: ByteArray) {
        if (data.size < 4) return
        val cmd = data[0].toInt() and 0xFF
        val seq = data[1].toInt() and 0xFF
        val result = data[2].toInt() and 0xFF
        val key = (cmd shl 8) or seq
        val cb = pending.remove(key)
        scope.launch { cb?.invoke(data) }
        Log.d(TAG, "rsp cmd=0x${cmd.toString(16)} seq=$seq result=$result")
    }

    private fun handleEvent(data: ByteArray) {
        if (data.size < 7) return
        val evt = data[0].toInt() and 0xFF
        val seq = data[1].toInt() and 0xFF
        val ts = ByteBuffer.wrap(data, 2, 4).order(ByteOrder.LITTLE_ENDIAN).int.toLong() and 0xFFFFFFFFL
        val len = data[6].toInt() and 0xFF
        val payload = if (data.size >= 7 + len) data.copyOfRange(7, 7 + len) else ByteArray(0)
        scope.launch { _events.emit(BleEvent(evt, seq, ts, payload)) }
    }

    /* ==================== 命令发送 ==================== */

    /**
     * 发送命令并等待应答。返回应答 payload（含 result 字节之后的部分），
     * 超时或失败返回 null。
     */
    suspend fun sendCommand(cmd: Int, payload: ByteArray = ByteArray(0)): ByteArray? {
        val char = writeChar ?: return null
        val seq = (seqCounter++.toInt() and 0xFF).toByte()
        val frame = ByteBuffer.allocate(3 + payload.size).order(ByteOrder.LITTLE_ENDIAN)
            .put(cmd.toByte())
            .put(seq)
            .put(payload.size.toByte())
            .put(payload)
            .array()
        val key = (cmd shl 8) or (seq.toInt() and 0xFF)
        return withTimeoutOrNull(2000) {
            suspendCancellableCoroutine { cont ->
                pending[key] = { rsp ->
                    if (cont.isActive) cont.resumeWith(Result.success(rsp))
                }
                try {
                    char.value = frame
                    gatt?.writeCharacteristic(char)
                } catch (e: Exception) {
                    pending.remove(key)
                    if (cont.isActive) cont.resumeWith(Result.success(null))
                }
            }
        }
    }

    suspend fun setPower(on: Boolean): Boolean {
        val rsp = sendCommand(BleProtocol.CMD_SET_POWER, byteArrayOf(if (on) 1 else 0))
        return rsp?.getOrNull(2)?.toInt() == BleProtocol.RESULT_OK
    }

    suspend fun start(): Boolean {
        val rsp = sendCommand(BleProtocol.CMD_START)
        return rsp?.getOrNull(2)?.toInt() == BleProtocol.RESULT_OK
    }

    suspend fun stop(): Boolean {
        val rsp = sendCommand(BleProtocol.CMD_STOP)
        return rsp?.getOrNull(2)?.toInt() == BleProtocol.RESULT_OK
    }

    suspend fun setTimerLevel(level: Int): Boolean {
        val rsp = sendCommand(BleProtocol.CMD_SET_TIMER_LVL, byteArrayOf(level.toByte()))
        return rsp?.getOrNull(2)?.toInt() == BleProtocol.RESULT_OK
    }

    suspend fun setTimerSeconds(seconds: Int): Boolean {
        val p = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(seconds.toShort()).array()
        val rsp = sendCommand(BleProtocol.CMD_SET_TIMER_SEC, p)
        return rsp?.getOrNull(2)?.toInt() == BleProtocol.RESULT_OK
    }

    suspend fun setAuto(on: Boolean): Boolean {
        val rsp = sendCommand(BleProtocol.CMD_SET_AUTO, byteArrayOf(if (on) 1 else 0))
        return rsp?.getOrNull(2)?.toInt() == BleProtocol.RESULT_OK
    }

    suspend fun setSpeed(level: Int): Boolean {
        val rsp = sendCommand(BleProtocol.CMD_SET_SPEED, byteArrayOf(level.toByte()))
        return rsp?.getOrNull(2)?.toInt() == BleProtocol.RESULT_OK
    }

    suspend fun setSpinTime(spinTime: Int): Boolean {
        val rsp = sendCommand(BleProtocol.CMD_SET_SPIN_TIME, byteArrayOf(spinTime.toByte()))
        return rsp?.getOrNull(2)?.toInt() == BleProtocol.RESULT_OK
    }

    suspend fun setTime(unixSeconds: Long): Boolean {
        val p = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(unixSeconds.toInt()).array()
        val rsp = sendCommand(BleProtocol.CMD_SET_TIME, p)
        return rsp?.getOrNull(2)?.toInt() == BleProtocol.RESULT_OK
    }

    suspend fun saveConfig(): Boolean {
        val rsp = sendCommand(BleProtocol.CMD_SAVE_CONFIG)
        return rsp?.getOrNull(2)?.toInt() == BleProtocol.RESULT_OK
    }

    suspend fun factoryReset(): Boolean {
        val rsp = sendCommand(BleProtocol.CMD_FACTORY_RESET, byteArrayOf(0x59, 0x46, 0x30, 0x38)) // "YF08"
        return rsp?.getOrNull(2)?.toInt() == BleProtocol.RESULT_OK
    }

    /* ==================== 工具 ==================== */

    private fun hasPermission(): Boolean {
        val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            listOf(Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION)
        }
        return perms.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
    }

    companion object {
        private const val TAG = "YF08_Ble"
    }
}

enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED }

data class DiscoveredDevice(val address: String, val name: String, val rssi: Int)

data class BleEvent(val evt: Int, val seq: Int, val timestampMs: Long, val payload: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BleEvent) return false
        return evt == other.evt && seq == other.seq && timestampMs == other.timestampMs && payload.contentEquals(other.payload)
    }
    override fun hashCode(): Int {
        var result = evt
        result = 31 * result + seq
        result = 31 * result + timestampMs.hashCode()
        result = 31 * result + payload.contentHashCode()
        return result
    }
}
