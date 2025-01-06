package com.ph32395.staynow_datn.QuanLyNhaTro

data class NhaTroModel(
    val maNhaTro: String = "",
    val maNguoiDung: String = "",
    val maLoaiNhaTro: String = "",
    val dcQuanHuyen: String = "",
    val dcTinhTP: String = "",
    val diaChi: String = "",
    val diaChiChiTiet: String = "",
    val tenNhaTro: String = "",
    val tenLoaiNhaTro: String = "",
    val ngayTao: Long = 0L
)