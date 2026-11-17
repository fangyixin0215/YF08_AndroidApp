package com.yf08.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yf08.app.ui.components.FlipPaddleIcon
import com.yf08.app.ui.theme.Primary
import com.yf08.app.ui.theme.PrimaryDark
import com.yf08.app.ui.theme.Surface
import com.yf08.app.ui.theme.Success
import com.yf08.app.ui.theme.TextSecondary
import com.yf08.app.viewmodel.MainViewModel

private val timerOptions = listOf("1分" to 1, "3分" to 2, "5分" to 3, "10分" to 4)

@Composable
fun ControlScreen(vm: MainViewModel) {
    val state by vm.deviceState.collectAsState()
    val conn by vm.connectionState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 顶部状态栏
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlipPaddleIcon(size = 48.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("YF08-3A2C", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        when (conn) {
                            com.yf08.app.ble.ConnectionState.CONNECTED -> "已连接"
                            com.yf08.app.ble.ConnectionState.CONNECTING -> "连接中…"
                            else -> "未连接"
                        },
                        color = if (conn == com.yf08.app.ble.ConnectionState.CONNECTED) Success else TextSecondary,
                        fontSize = 13.sp
                    )
                }
                BatteryRing(percent = state?.battPct ?: -1)
            }
        }

        Spacer(Modifier.height(24.dp))

        // 大启动按钮
        val working = state?.isWorking == true
        val btnScale by animateFloatAsState(if (working) 0.96f else 1f, label = "btn")
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(btnScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(listOf(Color.White, Surface))
                )
                .border(6.dp, Primary, CircleShape)
                .clickable { if (working) vm.stop() else vm.start() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FlipPaddleIcon(size = 72.dp, color = if (working) Primary else PrimaryDark)
                Spacer(Modifier.height(8.dp))
                Text(
                    if (working) "停止" else "启动",
                    color = if (working) PrimaryDark else Primary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        // 定时分段
        Text("定时", Modifier.fillMaxWidth().padding(start = 4.dp), color = TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            timerOptions.forEach { (label, level) ->
                val selected = state?.timeLevel == level
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selected) Primary else Surface)
                        .clickable { vm.setTimerLevel(level) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, color = if (selected) Color.White else TextSecondary, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // 速度滑块
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("速度", color = TextSecondary, fontSize = 14.sp)
            Spacer(Modifier.width(12.dp))
            Slider(
                value = (state?.speedLevel ?: 1).toFloat(),
                onValueChange = { vm.setSpeed(it.toInt().coerceIn(1, 5)) },
                valueRange = 1f..5f,
                modifier = Modifier.weight(1f)
            )
            Text("${state?.speedLevel ?: 1}", color = Primary, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))

        // 自动模式
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("自动模式", fontSize = 15.sp, modifier = Modifier.weight(1f))
            Switch(checked = state?.isAuto == true, onCheckedChange = { vm.setAuto(it) })
        }
    }
}

@Composable
private fun BatteryRing(percent: Int) {
    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
        // 简单环形电量（用两个叠加圆形实现）
        Box(
            Modifier.size(48.dp).clip(CircleShape)
                .background(Surface)
                .border(4.dp, Primary.copy(alpha = 0.25f), CircleShape)
        )
        Text(
            if (percent < 0) "--" else "$percent%",
            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryDark
        )
    }
}
