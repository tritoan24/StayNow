package com.ph32395.staynow_datn.LoaiPhong

class LoaiPhong (
    val maLoaiPhong: String? = null,
    val tenLoaiPhong: String,
    val trangThai: Boolean,
){
    constructor():this("","",false)
}