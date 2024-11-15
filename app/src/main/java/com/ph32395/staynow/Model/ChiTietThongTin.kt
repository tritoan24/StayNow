package com.ph32395.staynow.Model

class ChiTietThongTin(
    val don_vi: String = "",
    val icon_thongtin: String = "",
    val ma_phongtro: String = "",
    val so_luong_donvi: Long = 0L,
    val ten_thongtin: String = ""
) {
    constructor() : this("", "", "", 0, "")

    override fun toString(): String {
        return "ChiTietThongTin(don_vi='$don_vi', icon_thongtin='$icon_thongtin', ma_phongtro='$ma_phongtro', so_luong_donvi=$so_luong_donvi, ten_thongtin='$ten_thongtin')"
    }
}
