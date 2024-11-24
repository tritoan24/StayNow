package com.ph32395.staynow.TaoHopDong
data class HopDong(
    val maHopDong: String = "",
    // Thông tin cơ bản
    val ngayLapHopDong: String = "",
    val ngayBatDau: String = "",
    val ngayKetThuc: String = "",
    val thoiHanThue: Int = 0, // Số tháng

    // Thông tin phòng
    val maPhongTro: String = "",
    val giaThue: Int = 0,
    val tienCoc: Int = 0,

    // Người thuê
    val maNguoiThue: String = "",
    val hoTenNguoiThue: String = "",
    val soCMND: String = "",
    val soDienThoai: String = "",
    val diaChiThuongTru: String = "",

    // Chủ trọ
    val maChuTro: String = "",
    val hoTenChuTro: String = "",
    val soCMNDChuTro: String = "",
    val soDienThoaiChuTro: String = "",

    // Điều khoản thanh toán
    val hanThanhToan: Int = 0, // Ngày phải thanh toán trong tháng
    val phuongThucThanhToan: String = "", // Tiền mặt/Chuyển khoản

    // Quy định và điều khoản
    val quyDinhChung: List<String> = listOf(),
    val thoaThuanKhac: String = "",

    // Trạng thái hợp đồng
    val trangThai: String = "Đang hiệu lực" // Đang hiệu lực/Đã kết thúc/Đã hủy
)