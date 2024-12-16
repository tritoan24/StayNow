package com.ph32395.staynow.QuanLyNguoiThue


data class NguoiThueModel(
    val idHopDong: String = "",
    val idDaiDienThue: String = "",
    val tenPhong: String = "",
    val ngayBatDau: String = "",
    val soNguoiGioHanO:Int = 0,
    val thanhVienList: List<ThanhVien> = listOf()
) {


}

data class ThanhVien(
    val maTv:String = "",
    val name: String = "",
    val image: String = "",
    val email: String = "",
    val ngayVao: String = "",
    val sdt: String = ""
) {

}