package com.yf08.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yf08.app.ble.BleManager
import com.yf08.app.ble.ConnectionState
import com.yf08.app.ble.DeviceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {

    val ble = BleManager(app)

    val connectionState: StateFlow<ConnectionState> = ble.connectionState
    val deviceState: StateFlow<DeviceState?> = ble.deviceState
    val scanResults = ble.scanResults

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast.asStateFlow()

    fun showToast(msg: String) {
        _toast.value = msg
    }
    fun clearToast() {
        _toast.value = null
    }

    fun startScan() = ble.startScan()
    fun stopScan() = ble.stopScan()
    fun connect(address: String) = ble.connect(address)
    fun disconnect() = ble.disconnect()

    fun setPower(on: Boolean) = viewModelScope.launch {
        if (ble.setPower(on)) showToast(if (on) "已开机" else "已关机") else showToast("操作失败")
    }
    fun start() = viewModelScope.launch {
        if (ble.start()) showToast("已启动") else showToast("启动失败")
    }
    fun stop() = viewModelScope.launch {
        if (ble.stop()) showToast("已停止") else showToast("停止失败")
    }
    fun setTimerLevel(level: Int) = viewModelScope.launch {
        ble.setTimerLevel(level)
    }
    fun setAuto(on: Boolean) = viewModelScope.launch {
        ble.setAuto(on)
    }
    fun setSpeed(level: Int) = viewModelScope.launch {
        ble.setSpeed(level)
    }
    fun syncTime() = viewModelScope.launch {
        val now = System.currentTimeMillis() / 1000
        if (ble.setTime(now)) showToast("校时成功") else showToast("校时失败")
    }
    fun saveConfig() = viewModelScope.launch {
        if (ble.saveConfig()) showToast("配置已保存") else showToast("保存失败")
    }
    fun factoryReset() = viewModelScope.launch {
        if (ble.factoryReset()) showToast("已出厂复位") else showToast("复位失败")
    }
}
