package com.ph32395.staynow.ThongBao

data class NotificationModel(
    val id : String = "",
    val title: String = "",
    val message: String = "",
    val date: String = "",
    val time: String = "",
    val mapLink: String? = null,
    val timestamp : Long = 0,
    var isRead: Boolean = false,
    var readTime : String = ""
)
