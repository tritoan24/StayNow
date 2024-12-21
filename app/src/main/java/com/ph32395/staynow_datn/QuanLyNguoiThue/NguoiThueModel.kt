package com.ph32395.staynow_datn.QuanLyNguoiThue


data class NguoiThueModel(
    val idHopDong: String = "",
    val idDaiDienThue: String = "",
    val tenPhong: String = "",
    val ngayBatDau: String = "",
    val soNguoiGioHanO:Int = 0,
    val danhSachThanhVien: List<ThanhVien> = listOf()
) {


}

data class ThanhVien(
    val maThanhVien:String = "",
    val tenThanhVien: String = "",
    val hinhAnh: String = "",
    val email: String = "",
    val ngayVao: String = "",
    val soDienThoai: String = ""
) {

}