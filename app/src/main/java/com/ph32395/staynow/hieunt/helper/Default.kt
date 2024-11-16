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
        add(ScheduleStateModel("Chờ xác nhận", true))
        add(ScheduleStateModel("Chưa xem phòng", false))
        add(ScheduleStateModel("Đã xem phòng", false))
        add(ScheduleStateModel("Đã hủy", false))
    }

    object IntentKeys {
        const val SCREEN = "SCREEN"
        const val SPLASH_ACTIVITY = "SplashActivity"
        const val ROOM_DETAIL = "room_detail"
    }

    object SharePreKey {
        const val KEY_SEARCH_ENGINE = "key_search_engine"
    }

    object Collection {
        const val DAT_PHONG = "DatPhong"
        const val MA_NGUOI_DUNG = "ma_nguoi_dung"
    }

}