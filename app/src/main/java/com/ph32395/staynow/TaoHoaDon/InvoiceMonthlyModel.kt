package com.ph32395.staynow.TaoHoaDon

import com.ph32395.staynow.TaoHopDong.InvoiceStatus
import com.ph32395.staynow.TaoHopDong.UtilityFeeDetail
import java.io.Serializable
data class InvoiceMonthlyModel(
    val idHoaDon: String = "",
    val idNguoinhan: String = "",
    val idNguoigui: String = "",
    val idHopDong: String = "",
    val tenKhachHang: String = "",
    val tenPhong: String = "",
    val ngayLap: String = "",
    val hoaDonThang: String= "",
    val phiCoDinh: List<UtilityFeeDetail> = emptyList(),
    val phiBienDong: List<UtilityFeeDetail> = emptyList(),
    val tongTien: Double = 0.0,
    val trangThai: InvoiceStatus = InvoiceStatus.PENDING,
    val tienPhong: Double = 0.0,
    val tienCoc: Double = 0.0,
    val tongTienDichVu: Double = 0.0,
    val kieuHoadon: String = "Hoa Don Hang Thang",
    val paymentDate: String = "",
    val ngayThanhToan: Int = 1,
    val soDienCu: Int = 0,
    val soNuocCu: Int = 0,
    val tienGiam: Double = 0.0,
    val tienThem: Double = 0.0,
) : Serializable