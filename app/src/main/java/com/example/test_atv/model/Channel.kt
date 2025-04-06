// app/src/main/java/com/example/test_atv/model/Channel.kt
package com.example.test_atv.model

data class Channel(
    val id: String,
    val name: String,
    val description: String,
    val thumbnailUrl: String?,
    val dashUrl: String
)