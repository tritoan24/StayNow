package com.ph32395.staynow_datn.TaoHopDong

import java.util.UUID

data class MonthlyInvoice(
    val id: String = UUID.randomUUID().toString(), // ID duy nhất cho mỗi hóa đơn
    val contractId: String, // ID hợp đồng gốc
    val paymentPeriodStart: String, // Ngày bắt đầu kỳ thanh toán
    val paymentPeriodEnd: String, // Ngày kết thúc kỳ thanh toán
    val paymentDay: Int, // Ngày thanh toán trong tháng
    val roomPrice: Double, // Tiền phòng cố định
    val fixedUtilityFees: List<UtilityFeeDetail>, // Các phí dịch vụ cố định theo hợp đồng
    val variableUtilityFees: List<UtilityFeeDetail>? = null, // Các phí biến động (điện nước) - để null ban đầu
    val totalFixedFees: Double, // Tổng phí cố định
    val totalVariableFees: Double? = null, // Tổng phí biến động
    val totalInvoiceAmount: Double, // Tổng số tiền cần thanh toán
    val invoiceStatus: InvoiceStatus = InvoiceStatus.PENDING
)

// Mở rộng các model để hỗ trợ quản lý hóa đơn chi tiết
data class MonthlyInvoiceSchedule(
    val invoiceId: String = "", // ID của hóa đơn
    val contractId: String, // ID hợp đồng
    val invoicePeriodStart: String, // Ngày bắt đầu kỳ hóa đơn
    val invoicePeriodEnd: String, // Ngày kết thúc kỳ hóa đơn
    val paymentDueDate: String, // Ngày phải thanh toán
    val fixedUtilityFees: List<UtilityFeeDetail>, // Các hí cố định đã tính theo ngày
    val variableUtilityFees: List<UtilityFeeDetail>, // Các phí biến động (chưa có số liệu)
    val roomPrice: Double, // Tiền phòng theo kỳ
    val totalAmount: Double, // Tổng số tiền
    val songayo : Int,
    val status: InvoiceStatus = InvoiceStatus.PENDING // Trạng thái hóa đơn
)