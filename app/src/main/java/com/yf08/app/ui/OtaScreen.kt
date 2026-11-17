package com.yf08.app.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yf08.app.ui.theme.Primary
import com.yf08.app.ui.theme.Surface
import com.yf08.app.ui.theme.TextSecondary
import com.yf08.app.ui.theme.Warning
import com.yf08.app.viewmodel.MainViewModel

@Composable
fun OtaScreen(vm: MainViewModel) {
    val state by vm.deviceState.collectAsState()
    val lowBattery = (state?.battPct ?: 100) in 0..29

    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        // 版本信息
        Card(colors = CardDefaults.cardColors(containerColor = Surface), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("当前版本", color = TextSecondary)
                    Text("v1.0.0", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("可升级版本", color = TextSecondary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("v1.1.0", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.size(8.dp))
                        Box(Modifier.clip(CircleShape).background(Primary).padding(horizontal = 10.dp, vertical = 2.dp)) {
                            Text("可升级", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // 进度环
        Box(Modifier.size(180.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { 0.62f },
                modifier = Modifier.size(180.dp),
                strokeWidth = 14.dp,
                color = Primary
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("62%", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Primary)
                Text("传输中…", fontSize = 13.sp, color = TextSecondary)
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("firmware_v1.1.0.bin", color = TextSecondary, fontSize = 13.sp)

        Spacer(Modifier.height(24.dp))

        OutlinedButton(onClick = { vm.showToast("选择固件文件") }, modifier = Modifier.fillMaxWidth()) {
            Text("选择固件文件")
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { vm.showToast("开始升级") },
            enabled = !lowBattery,
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            modifier = Modifier.fillMaxWidth()
        ) { Text("开始升级") }

        Spacer(Modifier.height(20.dp))
        if (lowBattery) {
            Card(colors = CardDefaults.cardColors(containerColor = Warning.copy(alpha = 0.15f)), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, "警告", tint = Warning)
                    Spacer(Modifier.size(10.dp))
                    Text("电量不足，请更换电池后再升级", color = Warning, fontSize = 13.sp)
                }
            }
        }
    }
}
