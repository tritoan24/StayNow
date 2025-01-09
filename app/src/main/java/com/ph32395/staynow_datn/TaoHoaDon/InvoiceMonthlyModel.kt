package com.ph32395.staynow_datn.TaoHoaDon

import com.ph32395.staynow_datn.TaoHopDong.InvoiceStatus
import com.ph32395.staynow_datn.TaoHopDong.UtilityFeeDetail
import java.io.Serializable

data class InvoiceMonthlyModel(
    var idHoaDon: String = "",
    val idNguoiNhan: String = "",
    val idNguoiGui: String = "",
    val idHopDong: String = "",
    val tenKhachHang: String = "",
    val tenPhong: String = "",
    val ngayTaoHoaDon: String = "",
    val hoaDonThang: Int = 1,
    val phiCoDinh: List<UtilityFeeDetail> = emptyList(),
    val phiBienDong: List<UtilityFeeDetail> = emptyList(),
    val tongTien: Double = 0.0,
    val trangThai: InvoiceStatus = InvoiceStatus.PENDING,
    val tienPhong: Double = 0.0,
    val tienCoc: Double = 0.0,
    val tongPhiCoDinh: Double = 0.0,
    val tongPhiBienDong: Double = 0.0,
    val tongTienDichVu: Double = 0.0,
    val kieuHoadon: String = "Hoa Don Hang Thang",
    val paymentDate: String = "",
    val soDienCu: Int = 0,
    val soNuocCu: Int = 0,
    val soDienMoi: Int = 0,
    val soNuocMoi: Int = 0,
    val soDienTieuThu: Int = 0,
    val soNuocTieuThu: Int = 0,
    val tienGiam: Double = 0.0,
    val tienThem: Double = 0.0,
    val ghiChu: String = "",
) : Serializable