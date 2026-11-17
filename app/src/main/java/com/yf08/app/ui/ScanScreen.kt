package com.yf08.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yf08.app.ui.components.FlipPaddleIcon
import com.yf08.app.ui.theme.Primary
import com.yf08.app.ui.theme.Surface
import com.yf08.app.ui.theme.TextSecondary
import com.yf08.app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(vm: MainViewModel, onBack: () -> Unit) {
    val results by vm.scanResults.collectAsState()
    val conn by vm.connectionState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("扫描设备") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "返回") } }
            )
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp)) {
            OutlinedTextField(
                value = "", onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索设备") },
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(results, key = { it.address }) { dev ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FlipPaddleIcon(size = 44.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(dev.name, fontWeight = FontWeight.Medium)
                                Text(dev.address, color = TextSecondary, fontSize = 12.sp)
                            }
                            Icon(Icons.Filled.SignalCellularAlt, "信号", tint = TextSecondary)
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = { vm.connect(dev.address) },
                                colors = ButtonDefaults.buttonColors(containerColor = Primary)
                            ) { Text("连接") }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("请开启蓝牙权限以连接设备", color = TextSecondary, fontSize = 12.sp)
        }
    }
}
