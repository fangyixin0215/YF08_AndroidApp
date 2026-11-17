package com.yf08.app.ble

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * 解析固件上报的 State 结构（v0.02，17 字节）
 *
 * 布局（与固件 yf08_core.h yf08_state_t 一致，小端 packed）：
 *   0  ver          u8
 *   1  power        u8
 *   2  working      u8
 *   3  time_level   u8
 *   4  auto_mode    u8
 *   5  speed_level  u8
 *   6  spin_dir     u8
 *   7  progress     u8
 *   8  timer_remain u16 LE
 *  10  timer_set    u16 LE
 *  12  spin_count   u16 LE
 *  14  spin_time_ms u8
 *  15  flags        u8
 *  16  batt_pct     u8  (v0.02 新增)
 */
data class DeviceState(
    val ver: Int,
    val power: Int,
    val working: Int,
    val timeLevel: Int,
    val autoMode: Int,
    val speedLevel: Int,
    val spinDir: Int,
    val progress: Int,
    val timerRemainS: Int,
    val timerSetS: Int,
    val spinCount: Int,
    val spinTimeMs: Int,
    val flags: Int,
    val battPct: Int
) {
    val isPowerOn: Boolean get() = power == 1
    val isWorking: Boolean get() = working != 0
    val isAuto: Boolean get() = autoMode != 0
    val isBleConnected: Boolean get() = (flags and 0x04) != 0
    val isTimeValid: Boolean get() = (flags and 0x08) != 0

    companion object {
        fun fromBytes(data: ByteArray): DeviceState {
            val buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
            val ver = buf.get().toInt() and 0xFF
            val power = buf.get().toInt() and 0xFF
            val working = buf.get().toInt() and 0xFF
            val timeLevel = buf.get().toInt() and 0xFF
            val autoMode = buf.get().toInt() and 0xFF
            val speedLevel = buf.get().toInt() and 0xFF
            val spinDir = buf.get().toInt() and 0xFF
            val progress = buf.get().toInt() and 0xFF
            val timerRemain = buf.short.toInt() and 0xFFFF
            val timerSet = buf.short.toInt() and 0xFFFF
            val spinCount = buf.short.toInt() and 0xFFFF
            val spinTimeMs = buf.get().toInt() and 0xFF
            val flags = buf.get().toInt() and 0xFF
            // v0.01 固件没有 batt_pct 字段
            val battPct = if (data.size >= BleProtocol.STATE_V02_LEN) {
                buf.get().toInt() and 0xFF
            } else {
                -1
            }
            return DeviceState(
                ver = ver, power = power, working = working,
                timeLevel = timeLevel, autoMode = autoMode, speedLevel = speedLevel,
                spinDir = spinDir, progress = progress,
                timerRemainS = timerRemain, timerSetS = timerSet, spinCount = spinCount,
                spinTimeMs = spinTimeMs, flags = flags, battPct = battPct
            )
        }
    }
}
