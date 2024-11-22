package com.ph32395.staynow.hieunt.helper

import android.Manifest
import android.os.Build
import com.ph32395.staynow.hieunt.model.ScheduleStateModel

object Default {
    //Name permission
    val NOTIFICATION_PERMISSION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.POST_NOTIFICATIONS
    else ""

    val STORAGE_PERMISSION = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) arrayOf(
        Manifest.permission.READ_MEDIA_VIDEO
    ) else arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    val listScheduleState = mutableListOf<ScheduleStateModel>().apply {
        add(ScheduleStateModel("Chờ xác nhận", 0,true))
        add(ScheduleStateModel("Đã xác nhận", 0,false))
        add(ScheduleStateModel("Đã xem", 0,false))
        add(ScheduleStateModel("Đã hủy",0, false))
    }

    object IntentKeys {
        const val SCREEN = "SCREEN"
        const val SPLASH_ACTIVITY = "SplashActivity"
        const val ROOM_DETAIL = "room_detail"
        const val ROOM_ID = "room_id"
    }

    object SharePreKey {
        const val KEY_SEARCH_ENGINE = "key_search_engine"
    }

    object Collection {
        const val DAT_PHONG = "DatPhong"
        const val MA_NGUOI_DUNG = "ma_nguoi_dung"
        const val MA_PHONG = "ma_phong"
        const val NGUOI_THUE = "NguoiThue"
        const val NGUOI_CHO_THUE = "NguoiChoThue"
        const val NGUOI_DUNG = "NguoiDung"
        const val HO_TEN = "ho_ten"
        const val SO_DIEN_THOAI = "sdt"
        const val RENTER_ID = "renterId"
        const val TENANT_ID = "tenantId"
        const val STATUS = "status"
        const val DATE = "date"
        const val TIME = "time"
        const val CHANGED_SCHEDULE_BY_RENTER = "changedScheduleByRenter"
        const val ROOM_SCHEDULE_ID = "roomScheduleId"
    }

    object StatusRoom {
        const val WAIT = 0
        const val CONFIRMED = 1
        const val WATCHED = 2
        const val CANCELED = 3
    }

    object NotificationTitle {
        const val TITLE_CONFIRMED = "Lịch hẹn đã được xác nhận"
        const val TITLE_CANCELED_BY_RENTER = "Lịch hẹn đã bị hủy bởi chủ trọ"
        const val TITLE_CANCELED_BY_TENANT = "Lịch hẹn đã bị hủy bỏ bởi người thuê"
        const val TITLE_LEAVED_BY_RENTER = "Lịch hẹn đã thay đổi bởi chủ trọ"
        const val TITLE_LEAVED_BY_TENANT = "Lịch hẹn đã thay đổi bởi người thuê"
    }

}