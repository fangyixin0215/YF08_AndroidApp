package com.yf08.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yf08.app.ui.theme.Danger
import com.yf08.app.ui.theme.Primary
import com.yf08.app.ui.theme.Surface
import com.yf08.app.ui.theme.TextSecondary
import com.yf08.app.viewmodel.MainViewModel

@Composable
fun SettingsScreen(vm: MainViewModel) {
    var autoSync by remember { mutableStateOf(true) }
    val state by vm.deviceState.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GroupCard("时间同步") {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("自动校时", modifier = Modifier.weight(1f))
                Switch(checked = autoSync, onCheckedChange = { autoSync = it })
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { vm.syncTime() }, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                Text("立即校时")
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("设备时间", color = TextSecondary)
                Text("--", fontWeight = FontWeight.Medium)
            }
        }

        GroupCard("设备") {
            OutlinedButton(onClick = { vm.saveConfig() }, modifier = Modifier.fillMaxWidth()) {
                Text("保存配置")
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = { vm.factoryReset() },
                colors = ButtonDefaults.buttonColors(containerColor = Danger),
                modifier = Modifier.fillMaxWidth()
            ) { Text("出厂复位") }
        }

        GroupCard("关于") {
            InfoRow("APP版本", "1.0.0")
            InfoRow("固件版本", "v1.0.0")
            InfoRow("编译时间", "2024-10-01 14:22")
        }
    }
}

@Composable
private fun GroupCard(title: String, content: @Composable () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Surface), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary)
        Text(value, fontWeight = FontWeight.Medium)
    }
}
