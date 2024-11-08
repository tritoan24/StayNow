package com.ph32395.staynow.Model


data class PhongTroModel(
    val maPhongTro: String = "",
    val maNguoiDung: String = "",
    val tenPhongTro: String = "",
    val diaChi: String = "",
    val motaChiTiet: String? = "",
    val loaiPhongTro: String = "",
    val trangThai: String = "",
    val dienTich: Double = 0.0,
    val giaThue: Double = 0.0,
    val soLuotXem: Int? = 0,
    val ngayTao: Long? = 0L,
    val ngayCapNhat: Long? = 0L,
    val imageRoom: String = "",
    val danhSachAnh: ArrayList<String> = ArrayList(),
    val soNguoi: Int = 0,
    val tang: Int = 0,
    val tienCoc: Double = 0.0,
    val gioiTinh: String = ""
)
