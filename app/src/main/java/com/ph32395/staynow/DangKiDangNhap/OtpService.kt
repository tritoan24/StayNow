package com.ph32395.staynow.DangKiDangNhap

import android.content.Context
import android.widget.Toast
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

object OtpService {

    private val client = OkHttpClient()

    interface OtpCallback {
        fun onSuccess()
        fun onFailure(errorMessage: String)
    }

    // Hàm gửi OTP
    fun sendOtpToServer(
        context: Context,
        uid: String,
        otp: String,
        baseUrl: String,
        endpointVerifyOtp: String,
        callback: OtpCallback
    ) {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("uid", uid)
            jsonObject.put("otpCode", otp.toInt())
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val requestBody = RequestBody.create(
            "application/json;charset=utf-8".toMediaTypeOrNull(),
            jsonObject.toString()
        )
        val request = Request.Builder()
            .url("$baseUrl/$endpointVerifyOtp")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                (context as? android.app.Activity)?.runOnUiThread {
                    Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
                    callback.onFailure(e.message ?: "Lỗi không xác định")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    callback.onSuccess()
                } else {
                    (context as? android.app.Activity)?.runOnUiThread {
                        Toast.makeText(context, "OTP chưa đúng", Toast.LENGTH_SHORT).show()
                        callback.onFailure("OTP chưa đúng")
                    }
                }
            }
        })
    }

    // Hàm gửi lại OTP
    fun reSendOtpToServer(
        context: Context,
        uid: String,
        baseUrl: String,
        endpointResendOtp: String,
        callback: OtpCallback
    ) {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("uid", uid)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val requestBody = RequestBody.create(
            "application/json;charset=utf-8".toMediaTypeOrNull(),
            jsonObject.toString()
        )
        val request = Request.Builder()
            .url("$baseUrl/$endpointResendOtp")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                (context as? android.app.Activity)?.runOnUiThread {
                    Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
                    callback.onFailure(e.message ?: "Lỗi không xác định")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    callback.onSuccess()
                } else {
                    (context as? android.app.Activity)?.runOnUiThread {
                        callback.onFailure("Lỗi từ server: ${response.message}")
                    }
                }
            }
        })
    }
}
