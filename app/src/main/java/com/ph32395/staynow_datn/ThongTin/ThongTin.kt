package com.ph32395.staynow_datn.ThongTin

data class ThongTin(
    val maThongTin: String, // Mã thông tin (bắt buộc, không nullable)
    val tenThongTin: String, // Tên thông tin
    val iconThongTin: String, // Icon đại diện cho thông tin
    val donVi: String, // Đơn vị tính
    val giaTien: Int = 0, // Giá tiền (mặc định là 0 nếu không được truyền vào)
    val trangThai: Boolean // Trạng thái kích hoạt
)
