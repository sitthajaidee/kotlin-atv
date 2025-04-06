// app/src/main/java/com/example/test_atv/repository/ChannelRepository.kt
package com.example.test_atv.repository

import com.example.test_atv.model.Channel

class ChannelRepository {
    // ในตัวอย่างนี้เรากำหนด Channels แบบ Hardcode
    // ในการใช้งานจริงอาจจะโหลดจาก API หรือไฟล์ Configuration
    fun getChannels(): List<Channel> {
        return listOf(
            Channel(
                id = "channel1",
                name = "Channel 1",
                description = "This is test channel 1",
                thumbnailUrl = null,
                dashUrl = "https://mani-manip.vdosol-play.com/live/localdisk/V0003_STG/DNF/V0003_STG.mpd"
            ),
            Channel(
                id = "channel2",
                name = "Channel 2",
                description = "This is test channel 2",
                thumbnailUrl = null,
                dashUrl = "https://mani-manip.vdosol-play.com/live/localdisk/V0003_STG/DNF/V0003_STG.mpd"
            ),
            Channel(
                id = "channel3",
                name = "Channel 3",
                description = "This is test channel 3",
                thumbnailUrl = null,
                dashUrl = "https://mani-manip.vdosol-play.com/live/localdisk/V0003_STG/DNF/V0003_STG.mpd"
            )
        )
    }
}