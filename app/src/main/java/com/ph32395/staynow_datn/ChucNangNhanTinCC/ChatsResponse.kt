package com.ph32395.staynow_datn.ChucNangNhanTinCC
data class Chat(
    val maTinNhan: String? = null,
    val tinNhanCuoi: String? = null,
    val thoiGianTinNhanCuoi: Long? = null,
    val soTinChuaDoc: Int = 0,
    val maNguoiDungKhac: String? = null
)

data class Messenger(
    val maNguoiGui: String? = null,
    val tinNhan: String? = null,
    val timestamp: Long? = null
)

data class ChatDetails(
    val messages: Map<String, Messenger>? = null,
    val participants: Map<String, Boolean>? = null
)
