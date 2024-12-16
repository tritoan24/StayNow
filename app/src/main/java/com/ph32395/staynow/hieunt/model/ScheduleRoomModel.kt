package com.ph32395.staynow.hieunt.model

import com.google.firebase.firestore.PropertyName
import java.io.Serializable

data class ScheduleRoomModel (
    @PropertyName("ma_phong_hen") var roomScheduleId: String = "",
    @PropertyName("ma_phong") var roomId: String = "",
    @PropertyName("ten_phong") var roomName: String = "",
    @PropertyName("dia_chi") var roomAddress: String = "",
    @PropertyName("ma_nguoi_thue") var tenantId: String = "",
    @PropertyName("ma_chu_tro") var renterId: String = "",
    @PropertyName("ten_nguoi_thue") var tenantName: String = "",
    @PropertyName("ten_chu_tro") var renterName: String = "",
    @PropertyName("sdt_nguoi_thue") var tenantPhoneNumber: String = "",
    @PropertyName("sdt_chu_tro") var renterPhoneNumber: String = "",
    @PropertyName("ngay") var date: String = "",
    @PropertyName("gio") var time: String = "",
    @PropertyName("ghi_chu") var notes: String = "",
    @PropertyName("trang_thai") var status: Int = 0,
    @PropertyName("chu_tro_thay_doi_lich") var changedScheduleByRenter: Boolean = false,

    ): Serializable