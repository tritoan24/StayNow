package com.ph32395.staynow.Model


data class PhongTro(
    val maPhongTro: String? = null,
    val maNguoiDung: String? = null,
    val maGioiTinh: String? = null,
    val tenPhongTro: String? = null,
    val diaChi: String? = null,
    val motaChiTiet: String? = null,
    val loaiPhongTro: String? = null,
    val trangThai: String? = null,
    val dienTich: Double? = null,
    val giaThue: Double? = null,
    val soLuotXem: Int? = null,
    val ngayTao: Long? = null,
    val ngayCapNhat: Long? = null,
    val imageRoom: String? = null
) {
    constructor() : this("", "", "", "", "", "", "", "", 0.0, 0.0, 0, 0L, 0L, "")
}
