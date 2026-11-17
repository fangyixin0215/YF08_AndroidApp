package com.yf08.app.ble

/**
 * YF08 BLE 协议常量（与固件 yf08_ble.c / yf08_gatt_service.c 一一对应）
 */
object BleProtocol {

    /* ---- 服务 / 特征 UUID（16-bit 转 128-bit） ---- */
    const val SERVICE_YF08 = "0000F0A0-0000-1000-8000-00805F9B34FB"
    const val CHAR_WRITE   = "0000F0A1-0000-1000-8000-00805F9B34FB"  // APP→设备 命令
    const val CHAR_NOTIFY_RSP   = "0000F0A2-0000-1000-8000-00805F9B34FB"  // 设备→APP 应答
    const val CHAR_NOTIFY_STATE = "0000F0A3-0000-1000-8000-00805F9B34FB"  // 设备→APP State 通知
    const val CHAR_NOTIFY_EVENT = "0000F0A4-0000-1000-8000-00805F9B34FB"  // 设备→APP 事件
    const val CHAR_RECORD_READ  = "0000F0A5-0000-1000-8000-00805F9B34FB"  // 设备→APP 记录帧
    const val CHAR_RECORD_NOTIFY= "0000F0A6-0000-1000-8000-00805F9B34FB"  // 记录相关 notify

    /* 广播名前缀（固件：YF08-XXXX_YYMMDDhhmm） */
    const val NAME_PREFIX = "YF08-"

    /* ---- 命令码（APP→设备） ---- */
    const val CMD_SET_POWER       = 0x01
    const val CMD_START           = 0x02
    const val CMD_STOP            = 0x03
    const val CMD_SET_TIMER_LVL   = 0x04
    const val CMD_SET_TIMER_SEC   = 0x05
    const val CMD_SET_AUTO        = 0x06
    const val CMD_SET_SPEED       = 0x07
    const val CMD_SET_SPIN_TIME   = 0x08
    const val CMD_SAVE_CONFIG     = 0x09
    const val CMD_GET_STATE       = 0x10
    const val CMD_SET_TIME        = 0x12
    const val CMD_GET_VERSION     = 0x18
    const val CMD_RECORD_COUNT    = 0x20
    const val CMD_RECORD_PULL     = 0x21
    const val CMD_RECORD_CLEAR    = 0x22
    const val CMD_FACTORY_RESET   = 0x23

    /* ---- 结果码 ---- */
    const val RESULT_OK          = 0x00
    const val RESULT_BUSY        = 0x01
    const val RESULT_RANGE       = 0x02
    const val RESULT_AUTH        = 0x03
    const val RESULT_NVM_FAIL    = 0x04
    const val RESULT_UNSUPPORT   = 0x05
    const val RESULT_NEED_CONFIRM= 0x06

    /* ---- 事件码 ---- */
    const val EVT_KEY            = 0x01
    const val EVT_SPIN_START     = 0x02
    const val EVT_SPIN_END       = 0x03
    const val EVT_TIMER_END      = 0x04
    const val EVT_MODE_CHANGE    = 0x05
    const val EVT_CONFIG_SAVED   = 0x06
    const val EVT_PARAM_CHANGED  = 0x07
    const val EVT_BLE_LINK       = 0x0A

    /* ---- State 结构字段偏移（v0.02，共 17 字节） ---- */
    const val STATE_VER          = 0   // u8
    const val STATE_POWER        = 1   // u8
    const val STATE_WORKING      = 2   // u8
    const val STATE_TIME_LEVEL   = 3   // u8
    const val STATE_AUTO_MODE    = 4   // u8
    const val STATE_SPEED_LEVEL  = 5   // u8
    const val STATE_SPIN_DIR     = 6   // u8
    const val STATE_PROGRESS     = 7   // u8
    const val STATE_TIMER_REMAIN = 8   // u16 LE
    const val STATE_TIMER_SET    = 10  // u16 LE
    const val STATE_SPIN_COUNT   = 12  // u16 LE
    const val STATE_SPIN_TIME_MS = 14  // u8
    const val STATE_FLAGS        = 15  // u8
    const val STATE_BATT_PCT     = 16  // u8

    const val STATE_V02_LEN      = 17
    const val STATE_V01_LEN      = 16

    /* ---- 帧格式 ----
     * 命令帧:  [cmd][seq][plen][payload...]
     * 应答帧:  [cmd][seq][result][len][payload...]
     * 事件帧:  [evt][seq][ts_ms u32 LE][len][payload]
     */
}
