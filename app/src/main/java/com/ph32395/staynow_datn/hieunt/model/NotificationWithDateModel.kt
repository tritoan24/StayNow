package com.ph32395.staynow_datn.hieunt.model

data class NotificationWithDateModel (
    var date : String = "",
    var listNotification: MutableList<NotificationModel> = mutableListOf()
)