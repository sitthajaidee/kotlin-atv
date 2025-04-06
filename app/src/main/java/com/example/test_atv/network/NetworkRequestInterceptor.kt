package com.example.test_atv.network

import com.example.test_atv.model.NetworkRequest
import okhttp3.Interceptor
import okhttp3.Response
import java.util.Date

/**
 * Interceptor สำหรับดักจับและบันทึกข้อมูล network requests
 */
class NetworkRequestInterceptor(
    private val onRequestCaptured: (NetworkRequest) -> Unit
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.currentTimeMillis()

        // สร้าง NetworkRequest object สำหรับ request
        val networkRequest = NetworkRequest(
            url = request.url.toString(),
            method = request.method,
            timestamp = Date(startTime)
        )

        // เรียก proceed เพื่อดำเนินการส่ง request
        val response = chain.proceed(request)
        val endTime = System.currentTimeMillis()

        // อัปเดตข้อมูลเพิ่มเติมจาก response
        val updatedRequest = networkRequest.copy(
            contentType = response.header("Content-Type"),
            contentLength = response.header("Content-Length")?.toLongOrNull(),
            statusCode = response.code,
            responseTime = endTime - startTime
        )

        // เรียก callback เพื่อส่ง NetworkRequest ที่อัปเดตแล้ว
        onRequestCaptured(updatedRequest)

        return response
    }
}