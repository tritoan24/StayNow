package com.ph32395.staynow.DangKiDangNhap

import com.ph32395.staynow.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Callback
import java.io.IOException

object ServerWakeUpService {

    private const val SERVER_URL = Constants.URL_SERVER_QUYET

    fun wakeUpServer() {
        val client = OkHttpClient()

        // Tạo request GET đơn giản để đánh thức server
        val request = Request.Builder()
            .url(SERVER_URL) // URL server của bạn
            .build()

        // Thực hiện request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                // Xử lý khi request thất bại
                e.printStackTrace()
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                if (response.isSuccessful) {
                    // Server trả về thành công
                    println("Server được đánh thức thành công!")
                } else {
                    // Xử lý khi response không thành công
                    println("Đánh thức server thất bại: ${response.message}")
                }
            }
        })
    }
}
