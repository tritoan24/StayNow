package com.ph32395.staynow_datn.DichVu

data class DichVu (
    val maDichVu: String? = null,
    val tenDichVu: String,
    val iconDichVu: String,
    val donVi: List<String>,
    val trangThai: Boolean,
)