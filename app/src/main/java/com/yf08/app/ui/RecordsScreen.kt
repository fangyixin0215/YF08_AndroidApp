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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yf08.app.ui.theme.Primary
import com.yf08.app.ui.theme.Surface
import com.yf08.app.ui.theme.TextSecondary
import com.yf08.app.viewmodel.MainViewModel

private data class RecordUi(
    val date: String,
    val timeRange: String,
    val duration: String,
    val spinCount: Int,
    val speedLevel: Int,
    val auto: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(vm: MainViewModel) {
    val state by vm.deviceState.collectAsState()
    // TODO: 拉取真实记录（CMD_RECORD_COUNT + CMD_RECORD_PULL），此处用空列表占位
    val records = emptyList<RecordUi>()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("历史记录") },
                actions = {
                    IconButton(onClick = { vm.showToast("同步中…") }) { Icon(Icons.Filled.Sync, "同步") }
                    IconButton(onClick = { vm.showToast("清空记录需长按确认") }) { Icon(Icons.Filled.Delete, "清空") }
                }
            )
        }
    ) { pad ->
        if (records.isEmpty()) {
            Column(
                Modifier.fillMaxSize().padding(pad),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("暂无记录", color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                Text("使用设备后将自动保存训练记录", color = TextSecondary, fontSize = 12.sp)
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(pad).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(records) { r ->
                    Card(colors = CardDefaults.cardColors(containerColor = Surface), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(r.date, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(8.dp))
                                AssistChip(onClick = {}, label = { Text(if (r.auto) "自动" else "手动") })
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(r.timeRange, color = TextSecondary, fontSize = 13.sp)
                            Spacer(Modifier.height(4.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("时长 ${r.duration}")
                                Text("转动 ${r.spinCount} 次")
                                Text("速度 ${r.speedLevel}")
                            }
                        }
                    }
                }
            }
        }
    }
}
