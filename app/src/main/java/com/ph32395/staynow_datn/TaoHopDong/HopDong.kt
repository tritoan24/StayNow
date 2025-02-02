package com.ph32395.staynow_datn.TaoHopDong

import java.io.Serializable

data class HopDong(
    val maHopDong: String = "", // ID của hợp đồng
    val ngayTao: String = "", // Ngày tạo hợp đồng

    // Thông tin thời gian thuê
    val ngayBatDau: String = "", // Ngày bắt đầu thuê
    val ngayKetThuc: String = "", // Ngày kết thúc thuê
    val thoiHanThue: String = "", // Số tháng thuê
    val ngayThanhToan: Int = 1, // Ngày thanh toán hàng tháng

    // Ghi chú thêm
    val ghiChu: String = "",

    // Thông tin phòng
    val thongtinphong: RoomInfo = RoomInfo(),

    // Thông tin bên cho thuê
    val chuNha: PersonInfo = PersonInfo(),

    // Thông tin người thuê
    val nguoiThue: PersonInfo = PersonInfo(),

    // Thông tin tài chính
    val thongTinTaiChinh: FinancialInfo = FinancialInfo(),

    // Tiện ích
    val tienNghi: List<String> = listOf(),

    // Nội thất
    val noiThat: List<String> = listOf(),

    // Điều khoản và quy định
    val dieuKhoan: String = "", // Nội dung điều khoản hợp đồng

    val soNguoiO: Int = 0, // Số người ở

    // Trạng thái hợp đồng
    val trangThai: ContractStatus = ContractStatus.PENDING,
    val yeuCauChamDut: TerminationStatus = TerminationStatus.NOT_YET,
    val lyDoChamDut: String = "",
    val daTaoHoaDonChamDut: Boolean = false,
    // Thông tin hóa đơn
    val hoaDonHopDong: Invoice = Invoice(),

    val soDienCu: Int = 0,
    val soNuocCu: Int = 0,
) : Serializable

// Thông tin chi tiết về phòng
data class RoomInfo(
    val maPhongTro: String = "", // Mã phòng trọ
    val tenPhong: String = "", // Tên phòng
    val diaChiPhong: String = "", // Địa chỉ chi tiết
    val dienTich: Double = 0.0, // Diện tích (m²)
    val thongTinChiTiet: List<RoomDetail?> = listOf() // Chi tiết thông tin phòng
) : Serializable

// Chi tiết thông tin phòng
data class RoomDetail(
    val ten: String = "", // Tên thông tin
    val giaTri: Long = 0L, // Giá trị
    val donVi: String = "" // Đơn vị
) : Serializable

// Thông tin cá nhân (dùng cho cả chủ nhà và người thuê)
data class PersonInfo(
    val maNguoiDung: String = "", // ID người dùng
    val hoTen: String = "", // Họ và tên
    val soCCCD: String = "", // Số CCCD
    val ngaySinh: String = "", // Ngày sinh
    val gioiTinh: String = "", // Giới tính
    val soDienThoai: String = "", // Số điện thoại
    val diaChi: String = "", // Địa chỉ
    val ngayCapCCCD: String = "" // Ngày cấp CCCD
) : Serializable

// Thông tin tài chính
data class FinancialInfo(
    val giaThue: Double = 0.0, // Giá thuê hàng tháng
    val tienCoc: Double = 0.0, // Tiền cọc
    val soNuocht: Int = 0,
    val soDienht: Int = 0,
    val soNguoio: Int = 0,
    // Các loại phí
    val phiDichVu: List<UtilityFee> = listOf(),

    // Phương thức thanh toán
    val phuongThucThanhToan: String = "",


    ) : Serializable


// Chi tiết phí dịch vụ
data class UtilityFee(
    val tenDichVu: String = "", // Tên dịch vụ
    val giaTien: Double = 0.0, // Số tiền
    val donVi: String = "", // Đơn vị tính
    val batBuoc: Boolean = true // Bắt buộc hay không
) : Serializable

data class UtilityFeeUiState(
    val depositAmount: Double = 0.0, // Số tiền cọc
    val roomPrice: Double = 0.0, // Giá phòng
    val contractUtilityFees: List<UtilityFeeDetail> = listOf(),
    val totalContractPrice: Double = 0.0, // Tổng giá trị hợp đồng
    val invoiceStatus: InvoiceStatus = InvoiceStatus.PENDING // Trạng thái hóa đơn
) : Serializable

data class Invoice(
    val idHoaDon: String = "",
    val idNguoinhan: String = "",
    val idNguoigui: String = "",
    val idHopDong: String = "",
    val tenKhachHang: String = "",
    val tenPhong: String = "",
    val ngayLap: String = "",
    val kyHoaDon: String = "",
    val phiCoDinh: List<UtilityFeeDetail> = emptyList(),
    val phiBienDong: List<UtilityFeeDetail> = emptyList(),
    val tongTien: Double = 0.0,
    val trangThai: InvoiceStatus = InvoiceStatus.PENDING,
    val tienPhong: Double = 0.0,
    val tienCoc: Double = 0.0,
    val tongTienDichVu: Double = 0.0,
    val kieuHoadon: String = "",
    val paymentDate: String = "",
    val ngayThanhToan: Int = 1,
    val soDienCu: Int = 0,
    val soNuocCu: Int = 0,
) : Serializable

// Trạng thái hợp đồng
enum class ContractStatus {
    PENDING,
    PROCESSING,
    ACTIVE, // Đang hiệu lực
    EXPIRED, // Hết hạn
    TERMINATED,
    TERMINATED_PROCESSING,
    CANCELLED
}

data class UtilityFeeDetail(
    val tenDichVu: String = "",
    val giaTien: Double = 0.0,
    val donVi: String = "",
    val soLuong: Int = 0,
    val thanhTien: Double = 0.0
) : Serializable {
    // No-argument constructor for Firestore
    constructor() : this("", 0.0, "", 0, 0.0)
}

enum class InvoiceStatus {
    PENDING,
    PROCESSING,
    PAID,
    CANCELLED,
    DONE
}

enum class TerminationStatus {
    NOT_YET,
    PENDING,
    APPROVED,
    DENIED,
}