package com.example.test_atv.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.source.MediaSource
import com.example.test_atv.model.NetworkRequest
import com.example.test_atv.network.NetworkRequestInterceptor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.Date
import java.util.concurrent.TimeUnit

@androidx.annotation.OptIn(UnstableApi::class)
class DashPlayerViewModel : ViewModel() {

    private val _networkRequests = MutableStateFlow<List<NetworkRequest>>(emptyList())
    val networkRequests: StateFlow<List<NetworkRequest>> = _networkRequests.asStateFlow()

    private var player: ExoPlayer? = null
    private val okHttpClient: OkHttpClient

    init {
        // สร้าง OkHttpClient พร้อมกับ interceptor สำหรับบันทึก network requests
        val networkInterceptor = NetworkRequestInterceptor { request ->
            _networkRequests.value = _networkRequests.value + request
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addNetworkInterceptor(networkInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    fun initializePlayer(context: Context, dashUrl: String) {
        // สร้าง ExoPlayer instance
        val newPlayer = ExoPlayer.Builder(context).build()
        player = newPlayer

        // สร้าง MediaSource สำหรับ DASH streaming
        val mediaSource = createDashMediaSource(dashUrl)

        // กำหนด MediaSource ให้กับ player
        newPlayer.setMediaSource(mediaSource)

        // เตรียม player และเริ่มเล่น
        newPlayer.prepare()
        newPlayer.playWhenReady = true
    }
    @UnstableApi
    fun getPlayer(): ExoPlayer? {
        return player
    }

    @UnstableApi
    private fun createDashMediaSource(dashUrl: String): MediaSource {
        // สร้าง OkHttpDataSource.Factory ที่ใช้ OkHttpClient ที่เราตั้งค่าไว้
        val dataSourceFactory = OkHttpDataSource.Factory(okHttpClient)

        // สร้าง MediaItem จาก DASH URL
        val mediaItem = MediaItem.Builder()
            .setUri(dashUrl)
            .build()

        // สร้างและส่งคืน DashMediaSource
        return DashMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mediaItem)
    }

    fun releasePlayer() {
        player?.release()
        player = null
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }

    fun clearNetworkRequests() {
        _networkRequests.value = emptyList()
    }

}