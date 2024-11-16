package com.ph32395.staynow.hieunt.model

import com.google.firebase.firestore.PropertyName

data class ScheduleRoomModel (
    @PropertyName("ma_phong") var roomId: String = "",
    @PropertyName("ma_nguoi_dung") var userId: String = "",
    @PropertyName("ngay") var date: String = "",
    @PropertyName("gio") var time: String = "",
    @PropertyName("ghi_chu") var notes: String = "",
    @PropertyName("trang_thai") var status: Int = 0
)