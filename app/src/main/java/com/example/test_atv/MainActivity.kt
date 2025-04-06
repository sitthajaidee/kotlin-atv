package com.example.test_atv

import android.os.Bundle
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.media3.ui.PlayerView
import com.example.test_atv.model.NetworkRequest
import com.example.test_atv.ui.theme.Test_atvTheme
import com.example.test_atv.viewmodel.DashPlayerViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@UnstableApi
class MainActivity : ComponentActivity() {
    private val dashUrl = "https://mani-manip.vdosol-play.com/live/localdisk/V0003_STG/DNF/V0003_STG.mpd"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Test_atvTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DashPlayerScreen(dashUrl)
                }
            }
        }
    }
}

@UnstableApi
@Composable
fun DashPlayerScreen(dashUrl: String) {
    val viewModel: DashPlayerViewModel = viewModel()
    val context = LocalContext.current
    val networkRequests by viewModel.networkRequests.collectAsState()

    // ขนาดสัดส่วนของพื้นที่แสดงผลวิดีโอกับพื้นที่ debug
    val videoWeightFraction = 0.5f

    // Effect สำหรับการเริ่มใช้งานและปิด Player
    DisposableEffect(Unit) {
        viewModel.initializePlayer(context, dashUrl)
        onDispose {
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
            // แสดง PlayerView
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = true
                        player = viewModel.getPlayer()
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { playerView ->
                    playerView.player = viewModel.getPlayer()
                }
            )

            // แสดงสถานะการโหลด
            if (networkRequests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xBB000000)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
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
                    onClick = { viewModel.clearNetworkRequests() }
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