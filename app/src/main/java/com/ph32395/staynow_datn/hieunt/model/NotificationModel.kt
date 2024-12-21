package com.ph32395.staynow_datn.hieunt.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NotificationModel(
    @PrimaryKey (autoGenerate = true)
    var maThongBao : Int = 0,
    var tieuDe: String = "",
    var tinNhan: String = "",
    var ngayGuiThongBao: String = "",
    var thoiGian: String = "",
    var thoiGianGuiThongBao : Long = 0,
    var daDoc: Boolean = false,
    var daGui: Boolean = false,
    var loaiThongBao: String = "",
    var mapLink: String? = null,
    var idModel : String = ""
)
