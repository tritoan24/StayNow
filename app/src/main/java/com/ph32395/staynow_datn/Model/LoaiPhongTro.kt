package com.ph32395.staynow_datn.Model


data class LoaiPhongTro(
    val maLoaiPhong: String,
    val status: Boolean,
    val tenLoaiPhong: String
) {
    constructor() : this("", false, "")

}

