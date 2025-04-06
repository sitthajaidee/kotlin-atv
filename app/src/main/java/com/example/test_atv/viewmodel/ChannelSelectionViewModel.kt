// app/src/main/java/com/example/test_atv/viewmodel/ChannelSelectionViewModel.kt
package com.example.test_atv.viewmodel

import androidx.lifecycle.ViewModel
import com.example.test_atv.model.Channel
import com.example.test_atv.repository.ChannelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChannelSelectionViewModel : ViewModel() {
    private val repository = ChannelRepository()

    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels: StateFlow<List<Channel>> = _channels.asStateFlow()

    init {
        // โหลดข้อมูล channels
        loadChannels()
    }

    private fun loadChannels() {
        // ในตัวอย่างนี้เราโหลดข้อมูลโดยตรง
        // ในการใช้งานจริงอาจจะโหลดผ่าน coroutine หรือ background thread
        _channels.value = repository.getChannels()
    }
}