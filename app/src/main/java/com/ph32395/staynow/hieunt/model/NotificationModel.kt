package com.ph32395.staynow.hieunt.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NotificationModel(
    @PrimaryKey (autoGenerate = true)
    var id : Int = 0,
    var title: String = "",
    var message: String = "",
    var date: String = "",
    var time: String = "",
    var mapLink: String? = null,
    var timestamp : Long = 0,
    var isRead: Boolean = false,
    var isPushed: Boolean = false,
    var readTime : String = "",
    var typeNotification: String = "",
    var idModel : String = ""
)
