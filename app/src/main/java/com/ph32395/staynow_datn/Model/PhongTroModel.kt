package com.ph32395.staynow_datn.Model

import java.io.Serializable

data class PhongTroModel(
    val maNguoiDung: String = "", // ma chu tro
    val maPhongTro: String = "",
    val tenPhongTro: String = "",
    val diaChi: String = "",
    val diaChiChiTiet: String = "",
    val moTaChiTiet: String? = "",
    val maLoaiNhaTro: String = "",
    val trangThai: Boolean = false,
    val giaPhong: Double = 0.0,
    val soLuotXemPhong: Int = 0,
    val thoiGianTaoPhong: Long? = 0L,
    val thoiGianXem: Long? = 0L,
    val ngayCapNhat: Long? = 0L,
    val imageUrls: ArrayList<String> = ArrayList(),
    val maGioiTinh: String = "",
    var dienTich: Long? = 0L,
    val trangThaiDuyet: String = "",
    val trangThaiLuu: Boolean = false,
    val trangThaiPhong: Boolean = false,
    val trangThaiYeuThich: Boolean = false,
    var thoiGianYeuThich: Long? = null,
    var maNhaTro: String = "",
): Serializable{
//    constructor() : this("", "", "", "", "", "", "","", false, 0.0, 0, 0L, 0L,ArrayList(), "")
}
