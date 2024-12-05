package com.ph32395.staynow.hieunt.model

data class NotificationWithDateModel (
    var date : String = "",
    var listNotification: MutableList<NotificationModel> = mutableListOf()
)