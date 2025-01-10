package com.ph32395.staynow_datn.DichVu

data class DichVu(
    var maDichVu: String = "",        // Giá trị mặc định
    var tenDichVu: String = "",       // Giá trị mặc định
    var iconDichVu: String = "",      // Giá trị mặc định
    var donVi: List<String> = listOf(), // Giá trị mặc định
    var trangThai: Boolean = false    // Giá trị mặc định
)