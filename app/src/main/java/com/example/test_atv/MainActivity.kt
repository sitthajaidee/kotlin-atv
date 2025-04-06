package com.example.test_atv

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.test_atv.model.Channel
import com.example.test_atv.model.NetworkRequest
import com.example.test_atv.ui.ChannelSelectionScreen
import com.example.test_atv.ui.theme.Test_atvTheme
import com.example.test_atv.viewmodel.DashPlayerViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@UnstableApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Test_atvTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var selectedChannel by remember { mutableStateOf<Channel?>(null) }

                    if (selectedChannel == null) {
                        // แสดงหน้าเลือก Channel
                        ChannelSelectionScreen(
                            onChannelSelected = { channel ->
                                Log.d("MainActivity", "Channel selected: ${channel.name}, URL: ${channel.dashUrl}")
                                selectedChannel = channel
                            }
                        )
                    } else {
                        // แสดงหน้าเล่นวิดีโอ
                        DashPlayerScreen(selectedChannel!!.dashUrl) {
                            // กลับไปยังหน้าเลือก Channel เมื่อกด Back
                            Log.d("MainActivity", "Navigating back to channel selection")
                            selectedChannel = null
                        }
                    }
                }
            }
        }
    }
}

@UnstableApi
@Composable
fun DashPlayerScreen(dashUrl: String, onBackPress: () -> Unit) {
    val viewModel: DashPlayerViewModel = viewModel()
    val context = LocalContext.current
    val networkRequests by viewModel.networkRequests.collectAsState()

    Log.d("DashPlayerScreen", "Composable started with URL: $dashUrl")

    // ขนาดสัดส่วนของพื้นที่แสดงผลวิดีโอกับพื้นที่ debug
    val videoWeightFraction = 0.5f

    // Effect สำหรับการเริ่มใช้งานและปิด Player
    // เปลี่ยนเป็นใช้ LaunchedEffect แทน DisposableEffect
    // เพื่อให้การ initialize player เสร็จก่อนที่ UI จะถูกสร้าง
    val playerState = remember { mutableStateOf<ExoPlayer?>(null) }

    LaunchedEffect(dashUrl) {
        Log.d("DashPlayerScreen", "LaunchedEffect triggered")
        viewModel.initializePlayer(context, dashUrl)
        val player = viewModel.getPlayer()
        Log.d("DashPlayerScreen", "Player initialized: ${player != null}")
        playerState.value = player
    }

    // Effect สำหรับการปิด Player เมื่อออกจากหน้า
    DisposableEffect(Unit) {
        onDispose {
            Log.d("DashPlayerScreen", "DisposableEffect disposed, releasing player")
            viewModel.releasePlayer()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // หน้าต่างเล่นวิดีโอ (ครึ่งบน)
        Box(
            modifier = Modifier
                .weight(videoWeightFraction)
                .fillMaxWidth()
        ) {
            // แสดง PlayerView เมื่อ player ถูกสร้างแล้วเท่านั้น
            playerState.value?.let { player ->
                AndroidView(
                    factory = { ctx ->
                        Log.d("DashPlayerScreen", "Creating PlayerView")
                        PlayerView(ctx).apply {
                            useController = true
                            this.player = player
                            Log.d("DashPlayerScreen", "PlayerView created with player: ${this.player != null}")
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { playerView ->
                        Log.d("DashPlayerScreen", "Updating PlayerView")
                        playerView.player = player
                        Log.d("DashPlayerScreen", "PlayerView updated with player: ${playerView.player != null}")
                    }
                )
            }

            // แสดงสถานะการโหลด
            if (networkRequests.isEmpty() || playerState.value == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xBB000000)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }

            // ปุ่มย้อนกลับ
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.TopStart
            ) {
                Button(onClick = onBackPress) {
                    Text("Back")
                }
            }
        }

        // หน้าต่าง debug (ครึ่งล่าง)
        Column(
            modifier = Modifier
                .weight(1 - videoWeightFraction)
                .fillMaxWidth()
                .background(Color(0xFF101010))
                .padding(8.dp)
        ) {
            // ส่วนหัวของหน้าต่าง debug
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Network Requests",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = {
                        Log.d("DashPlayerScreen", "Clearing network requests")
                        viewModel.clearNetworkRequests()
                    }
                ) {
                    Text("Clear")
                }
            }

            // แสดงรายการ network requests
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(networkRequests.reversed()) { request ->
                    NetworkRequestItem(request)
                    HorizontalDivider(color = Color.DarkGray, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
fun NetworkRequestItem(request: NetworkRequest) {
    val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // แสดง URL และ HTTP method
        Text(
            text = "${request.method} ${request.url}",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        // แสดงเวลาและรายละเอียดอื่นๆ
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = dateFormat.format(request.timestamp),
                color = Color.LightGray,
                fontSize = 12.sp
            )

            Text(
                text = request.statusCode?.toString() ?: "Pending",
                color = when {
                    request.statusCode == null -> Color.Yellow
                    request.statusCode in 200..299 -> Color.Green
                    else -> Color.Red
                },
                fontSize = 12.sp
            )
        }

        // แสดงขนาดไฟล์และเวลาในการตอบกลับ
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = request.contentType ?: "Unknown type",
                color = Color.LightGray,
                fontSize = 12.sp
            )

            Text(
                text = if (request.contentLength != null) {
                    formatFileSize(request.contentLength)
                } else {
                    "Unknown size"
                },
                color = Color.LightGray,
                fontSize = 12.sp
            )

            Text(
                text = if (request.responseTime != null) {
                    "${request.responseTime}ms"
                } else {
                    "..."
                },
                color = Color.LightGray,
                fontSize = 12.sp
            )
        }
    }
}

// ฟังก์ชันแปลงขนาดไฟล์เป็น KB, MB, GB
fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "${size / 1024} KB"
        size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
        else -> "${size / (1024 * 1024 * 1024)} GB"
    }
}