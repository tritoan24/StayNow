package com.ph32395.staynow_datn.utils

object Constants {
    private const val PORT = 10000
    const val URL_SERVER_OCEANTECH = "http://192.168.31.80:$PORT"
    const val URL_SERVER_QUYET_LOCAL = "http://192.168.1.7:$PORT"
    const val APP_ID = 2554

    const val URL_SERVER_QUYET = "https://staynow-server.onrender.com"
    const val ENDPOINT_VERIFY_OTP = "otp/verify-otp"
    const val ENDPOINT_RESEND_OTP = "otp/resend-otp"
    const val ENDPOINT_VERIFY_TOKEN = "otp/verify-token"
}