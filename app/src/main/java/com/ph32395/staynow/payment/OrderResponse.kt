package com.ph32395.staynow.payment

class OrderResponse(
    val success: Boolean,
    val zalopay_response: ZaloPayResponse
)

class ZaloPayResponse(
    val return_code: Int,
    val return_message: String,
    val sub_return_code: Int,
    val sub_return_message: String,
    val zp_trans_token: String?,
    val order_url: String?,
    val order_token: String?
)