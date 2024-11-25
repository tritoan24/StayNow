data class HopDong(
    val contractId: String = "", // ID của hợp đồng
    val createdDate: String = "", // Ngày tạo hợp đồng

    // Thông tin thời gian thuê
    val startDate: String = "", // Ngày bắt đầu thuê
    val endDate: String = "", // Ngày kết thúc thuê
    val rentDuration: String = "", // Số tháng thuê
    val paymentDay: Int = 1, // Ngày thanh toán hàng tháng

    //ghi chú thêm
    val note: String = "",

    // Thông tin phòng
    val roomInfo: RoomInfo = RoomInfo(),

    // Thông tin bên cho thuê
    val landlordInfo: PersonInfo = PersonInfo(),

    // Thông tin người thuê
    val tenantInfo: PersonInfo = PersonInfo(),

    // Thông tin tài chính
    val financialInfo: FinancialInfo = FinancialInfo(),

    // Tiện ích
    val amenities: List<String> = listOf(),

    // Nội thất
    val furniture: List<String> = listOf(),


    // Điều khoản và quy định
    val terms: String = "", // Nội dung điều khoản hợp đồng

    // Trạng thái hợp đồng
    val status: ContractStatus = ContractStatus.PENDING
)

// Thông tin chi tiết về phòng
data class RoomInfo(
    val roomId: String = "", // Mã phòng trọ
    val roomName: String = "", // Tên phòng
    val address: String = "", // Địa chỉ chi tiết
    val area: Double = 0.0, // Diện tích (m²)
    val details: List<RoomDetail> = listOf() // Chi tiết thông tin phòng
)

// Chi tiết thông tin phòng
data class RoomDetail(
    val name: String = "", // Tên thông tin
    val value: Long = 0L, // Giá trị
    val unit: String = "" // Đơn vị
)

// Thông tin cá nhân (dùng cho cả chủ nhà và người thuê)
data class PersonInfo(
    val userId: String = "", // ID người dùng
    val fullName: String = "", // Họ và tên
    val idNumber: String = "", // Số CCCD
    val dateOfBirth: String = "", // Ngày sinh
    val gender: String = "", // Giới tính
    val phone: String = "", // Số điện thoại
    val address: String = "", // Địa chỉ
    val idIssueDate: String = "" // Ngày cấp CCCD
)

// Thông tin tài chính
data class FinancialInfo(
    val monthlyRent: Double = 0.0, // Giá thuê hàng tháng
    val deposit: Double = 0.0, // Tiền cọc

    // Các loại phí
    val utilities: List<UtilityFee> = listOf(),

    // Phương thức thanh toán
    val paymentMethod: String = "",

)

// Chi tiết phí dịch vụ
data class UtilityFee(
    val name: String = "", // Tên dịch vụ
    val amount: Double = 0.0, // Số tiền
    val unit: String = "", // Đơn vị tính
    val isRequired: Boolean = true // Bắt buộc hay không
)

// Trạng thái hợp đồng
enum class ContractStatus {
    PENDING, // Chờ ký
    ACTIVE, // Đang hiệu lực
    EXPIRED, // Hết hạn
    TERMINATED // Đã chấm dứt
}