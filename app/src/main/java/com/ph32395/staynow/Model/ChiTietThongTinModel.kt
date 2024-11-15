package com.ph32395.staynow.Model

class ChiTietThongTinModel(
    val don_vi: String = "",
    val icon_thongtin: String = "",
    val ma_phongtro: String = "",
    val so_luong_donvi: Long = 0L,
    val ten_thongtin: String = ""
) {
    constructor() : this("", "", "", 0L, "")

}
