package com.ph32395.staynow_datn.DangKiDangNhap

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

object TokenService {

    private val client = OkHttpClient()

    fun sendTokenToServer(token: String, url: String, callback: TokenCallback) {
        val jsonObject = JSONObject().apply {
            put("idToken", token)
        }

        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaType(),
            jsonObject.toString()
        )

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure("Lỗi kết nối: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    callback.onSuccess()
                } else {
                    callback.onFailure("Lỗi xác minh token. Mã lỗi: ${response.code}")
                }
            }
        })
    }

    interface TokenCallback {
        fun onSuccess()
        fun onFailure(errorMessage: String)
    }
}
