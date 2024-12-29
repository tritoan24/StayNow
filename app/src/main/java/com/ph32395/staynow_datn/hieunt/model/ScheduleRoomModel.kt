package com.ph32395.staynow_datn.hieunt.model

import java.io.Serializable

data class ScheduleRoomModel(
    var maDatPhong: String = "",
    var maPhongTro: String = "",
    var tenPhong: String = "",
    var diaChiPhong: String = "",
    var maNguoiThue: String = "",
    var maChuTro: String = "",
    var tenNguoiThue: String = "",
    var tenChuTro: String = "",
    var sdtNguoiThue: String = "",
    var sdtChuTro: String = "",
    var ngayDatPhong: String = "",
    var thoiGianDatPhong: String = "",
    var ghiChu: String = "",
    var trangThaiDatPhong: Int = 0,
    var thayDoiBoiChuTro: Boolean = false
) : Serializable