package com.ph32395.staynow_datn.Model

class ChiTietThongTin(
    val donVi: String = "",
    val iconThongTin: String = "",
    val maPhongTro: String = "",
    val soLuongDonVi: Long = 0L,
    val tenThongTin: String = ""
) {
    constructor() : this("", "", "", 0, "")

    override fun toString(): String {
        return "ChiTietThongTin(don_vi='$donVi', icon_thongtin='$iconThongTin', ma_phongtro='$maPhongTro', so_luong_donvi=$soLuongDonVi, ten_thongtin='$tenThongTin')"
    }
}
