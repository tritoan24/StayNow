package com.ph32395.staynow_datn.hieunt.helper

import android.Manifest
import android.os.Build
import com.ph32395.staynow_datn.hieunt.model.ScheduleStateModel

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
        add(ScheduleStateModel("Chờ xác nhận",0, 0,true))
        add(ScheduleStateModel("Đã xác nhận",1, 0,false))
        add(ScheduleStateModel("Đã xem",2, 0,false))
        add(ScheduleStateModel("Đã hủy",3,0, false))
    }

    object IntentKeys {
        const val SCREEN = "SCREEN"
        const val SPLASH_ACTIVITY = "SplashActivity"
        const val ROOM_DETAIL = "room_detail"
        const val ROOM_ID = "room_id"
        const val OPEN_MANAGE_SCHEDULE_ROOM_BY_NOTIFICATION = "open_manage_schedule_room_by_notification"
        const val ROOM_SCHEDULE = "room_schedule"
    }

    object SharePreKey {
        const val KEY_SEARCH_ENGINE = "key_search_engine"
    }

    object Collection {
        const val DAT_PHONG = "DatPhong"
        const val NGUOI_DUNG = "NguoiDung"
        const val THONG_BAO = "ThongBao"
        const val HO_TEN = "hoTen"
        const val SO_DIEN_THOAI = "sdt"
        const val RENTER_ID = "maChuTro"
        const val TENANT_ID = "maNguoiThue"
        const val STATUS = "trangThaiDatPhong"
        const val DATE = "ngayDatPhong"
        const val TIME = "thoiGianDatPhong"
        const val TITLE = "tieuDe"
        const val MAP_LINK = "mapLink"
        const val TIME_STAMP = "thoiGianGuiThongBao"
        const val DATE_PUSH_NOTIFICATION = "ngayGuiThongBao"
        const val MESSAGE = "tinNhan"
        const val CHANGED_SCHEDULE_BY_RENTER = "thayDoiBoiChuTro"
        const val ROOM_SCHEDULE_ID = "maDatPhong"
        const val IS_PUSHED = "daGui"
        const val TYPE_NOTIFICATION = "loaiThongBao"
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
        const val TITLE_CANCELED_BY_OVER_TIME = "Lịch hẹn đã bị hủy bỏ vì quá hẹn"
        const val TITLE_LEAVED_BY_RENTER = "Lịch hẹn đã thay đổi bởi chủ trọ"
        const val TITLE_LEAVED_BY_TENANT = "Lịch hẹn đã thay đổi bởi người thuê"
        const val TITLE_SCHEDULE_ROOM_SUCCESSFULLY = "Phòng trọ đã được đặt thành công"
    }

    object TypeNotification{
        const val TYPE_SCHEDULE_ROOM_RENTER = "type_schedule_room_renter"
        const val TYPE_SCHEDULE_ROOM_TENANT = "type_schedule_room_tenant"
        const val TYPE_NOTI_BILL_MONTHLY = "invoiceCreation"
        const val TYPE_NOTI_BILL_MONTHLY_REMIND = "invoiceRemind"
        const val TYPE_NOTI_MASSAGES = "send_massage"
        const val TYPE_CONTRACT_DONE = "ContractInvoice"

        const val TYPE_NOTI_PAYMENT_CONTRACT = "hoadonhopdong"
        const val TYPE_NOTI_PAYMENT_INVOICE = "hoadonhangthang"
        const val TYPE_NOTI_TERMINATED_REQUEST = "yeuCauChamDut"
        const val TYPE_NOTI_TERMINATED_DENY = "tuChoiChamDut"
        const val TYPE_NOTI_TERMINATED_CONFIRM = "xacNhanChamDut"

        const val TYPE_NOTI_REMIND_STATUS_CONTRACT = "kiemtranhacnhohopdong"

        const val TYPE_NOTI_CONTRACT = "HopDongMoi"
    }

}