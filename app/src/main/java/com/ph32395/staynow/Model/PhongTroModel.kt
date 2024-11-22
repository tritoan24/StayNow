package com.ph32395.staynow.Model

import java.io.Serializable

data class PhongTroModel(
    val Ma_nguoidung: String = "", // ma chu tro
    val Ten_phongtro: String = "",
    val Dia_chi: String = "",
    val Dia_chichitiet: String = "",
    val Mota_chitiet: String? = "",
    val Ma_loaiphong: String = "",
    val Trang_thai: Boolean ,
    val Gia_phong: Double = 0.0,
    val So_luotxemphong: Int? = 0,
    val ThoiGian_taophong: Long? = 0L,
    val Ngay_capnhat: Long? = 0L,
    val imageUrls: ArrayList<String> = ArrayList(),
    val Ma_gioiTinh: String = "",
    var Dien_tich: Long? = 0L,
    val Trang_thaiduyet: String = "",
    val Trang_thailuu: Boolean = false,
    val Trang_thaiphong: Boolean = false
): Serializable{
    constructor() : this("", "", "", "", "", "", false, 0.0, 0, 0L, 0L, ArrayList(), "")
}
