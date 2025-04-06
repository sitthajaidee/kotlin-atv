package com.example.test_atv.model

import java.util.Date

/**
 * เก็บข้อมูลเกี่ยวกับ network request ที่เกิดขึ้นในระหว่างการสตรีม
 */
data class NetworkRequest(
    val url: String,
    val method: String,
    val timestamp: Date = Date(),
    val contentType: String? = null,
    val contentLength: Long? = null,
    val statusCode: Int? = null,
    val responseTime: Long? = null
)