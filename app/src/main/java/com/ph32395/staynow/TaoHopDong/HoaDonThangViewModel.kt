//import android.os.Build
//import android.util.Log
//import androidx.annotation.RequiresApi
//import androidx.lifecycle.viewModelScope
//import com.google.firebase.firestore.FirebaseFirestore
//import com.ph32395.staynow.TaoHopDong.MonthlyInvoiceSchedule
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.tasks.await
//import java.text.SimpleDateFormat
//import java.util.Calendar
//import java.util.Locale
//
///**
// * Tạo lịch hóa đơn cho toàn bộ thời hạn hợp đồng
// * @param contract Thông tin hợp đồng
// * @param utilityFees Danh sách phí dịch vụ
// */
//@RequiresApi(Build.VERSION_CODES.O)
//fun generateInvoiceSchedule(
//    contract: com.ph32395.staynow.TaoHopDong.com.ph32395.staynow.TaoHopDong.com.ph32395.staynow.TaoHopDong.HopDong,
//    utilityFees: List<com.ph32395.staynow.TaoHopDong.com.ph32395.staynow.TaoHopDong.com.ph32395.staynow.TaoHopDong.UtilityFee>
//): List<MonthlyInvoiceSchedule> {
//    val (fixedFees, variableFees) = categorizeUtilityFees(utilityFees)
//    val invoiceSchedules = mutableListOf<MonthlyInvoiceSchedule>()
//
//    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
//    val startDate = sdf.parse(contract.startDate)
//    val endDate = sdf.parse(contract.endDate)
//
//    val calendar = Calendar.getInstance()
//    calendar.time = startDate
//
//    while (calendar.time.before(endDate)) {
//        // Đặt ngày thanh toán
//        val paymentCalendar = Calendar.getInstance()
//        paymentCalendar.time = calendar.time
//        paymentCalendar.set(Calendar.DAY_OF_MONTH, contract.paymentDay)
//
//        // Nếu ngày thanh toán nhỏ hơn ngày hiện tại, chuyển sang tháng kế
//        if (paymentCalendar.time.before(calendar.time)) {
//            paymentCalendar.add(Calendar.MONTH, 1)
//        }
//
//        // Xác định kỳ thanh toán
//        val periodStart = calendar.time
//        paymentCalendar.add(Calendar.MONTH, 1)
//        var periodEnd = paymentCalendar.time
//
//        // Điều chỉnh nếu vượt quá ngày kết thúc hợp đồng
//        if (periodEnd.after(endDate)) {
//            periodEnd = endDate
//        }
//
//        // Tính số ngày thực tế trong kỳ
//        val actualStayDays = ((periodEnd.time - periodStart.time) / (1000 * 60 * 60 * 24)).toInt() + 1
//        val totalDaysInMonth = paymentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
//
//        val isFullMonth = actualStayDays > 27
//
//        val proRatedRoomPrice: Double
//        val fixedUtilityDetails: List<com.ph32395.staynow.TaoHopDong.com.ph32395.staynow.TaoHopDong.com.ph32395.staynow.TaoHopDong.UtilityFeeDetail>
//
//        if (isFullMonth) {
//            proRatedRoomPrice = contract.financialInfo.monthlyRent
//            fixedUtilityDetails = fixedFees.map { fee ->
//                val quantity = when (fee.unit) {
//                    "Người" -> contract.peopleCount
//                    "Phòng" -> 1
//                    else -> 1
//                }
//                com.ph32395.staynow.TaoHopDong.com.ph32395.staynow.TaoHopDong.com.ph32395.staynow.TaoHopDong.UtilityFeeDetail(
//                    name = fee.name,
//                    unitPrice = fee.amount,
//                    unit = fee.unit,
//                    quantity = quantity,
//                    subtotal = fee.amount * quantity
//                )
//            }
//        } else {
//            val dailyRoomRate = contract.financialInfo.monthlyRent / totalDaysInMonth
//            proRatedRoomPrice = dailyRoomRate * actualStayDays
//
//            fixedUtilityDetails = fixedFees.map { fee ->
//                val quantity = when (fee.unit) {
//                    "Người" -> contract.peopleCount
//                    "Phòng" -> 1
//                    else -> 1
//                }
//                val monthlyFee = fee.amount * quantity
//                val proRatedFee = (monthlyFee / totalDaysInMonth) * actualStayDays
//                com.ph32395.staynow.TaoHopDong.com.ph32395.staynow.TaoHopDong.com.ph32395.staynow.TaoHopDong.UtilityFeeDetail(
//                    name = fee.name,
//                    unitPrice = fee.amount,
//                    unit = fee.unit,
//                    quantity = quantity,
//                    subtotal = proRatedFee
//                )
//            }
//        }
//
//        val invoiceForPeriod = MonthlyInvoiceSchedule(
//            contractId = contract.contractId,
//            invoicePeriodStart = sdf.format(periodStart),
//            invoicePeriodEnd = sdf.format(periodEnd),
//            paymentDueDate = sdf.format(periodEnd),
//            fixedUtilityFees = fixedUtilityDetails,
//            songayo = actualStayDays,
//            variableUtilityFees = variableFees.map { fee ->
//                com.ph32395.staynow.TaoHopDong.com.ph32395.staynow.TaoHopDong.com.ph32395.staynow.TaoHopDong.UtilityFeeDetail(
//                    name = fee.name,
//                    unitPrice = fee.amount,
//                    unit = fee.unit,
//                    quantity = 0,
//                    subtotal = 0.0
//                )
//            },
//            roomPrice = proRatedRoomPrice,
//            totalAmount = proRatedRoomPrice + fixedUtilityDetails.sumOf { it.subtotal }
//        )
//
//        invoiceSchedules.add(invoiceForPeriod)
//
//        // Cập nhật cho kỳ tiếp theo
//        calendar.time = periodEnd
//    }
//
//
//    // Chỉ log khi cần thiết, và có thể sử dụng debug build
//    Log.d("ContractViewModel", "===== BẢNG LỊCH THANH TOÁN CHI TIẾT =====")
//    invoiceSchedules.forEachIndexed { index, invoice ->
//        Log.d("ContractViewModel", "\n📅 KỲ THANH TOÁN ${index + 1}:")
//
//        // Log thông tin hợp đồng
//        Log.d("ContractViewModel", "THÔNG TIN HỢP ĐỒNG:")
//        Log.d("ContractViewModel", "- Mã HĐ: ${invoice.contractId}")
//        Log.d("ContractViewModel", "- Số người ở: ${contract.peopleCount}")
//        Log.d("ContractViewModel", "- Số phòng: 1")
//
//        // Log thời gian
//        Log.d("ContractViewModel", "\n⏰ THỜI GIAN:")
//        Log.d("ContractViewModel", "- Ngày bắt đầu: ${invoice.invoicePeriodStart}")
//        Log.d("ContractViewModel", "- Ngày kết thúc: ${invoice.invoicePeriodEnd}")
//        Log.d("ContractViewModel", "- Ngày thanh toán: ${invoice.paymentDueDate}")
//        Log.d("ContractViewModel", "- Số ngày ở thực tế: ${invoice.songayo}")
//
//        // Chi tiết tính toán tiền phòng
//        Log.d("ContractViewModel", "\n🏠 TÍNH TOÁN TIỀN PHÒNG:")
//        Log.d(
//            "ContractViewModel",
//            "- Giá phòng/tháng: ${contract.financialInfo.monthlyRent}"
//        )
//
//
//        // Chi tiết phí dịch vụ cố định
//        Log.d("ContractViewModel", "\n💡 PHÍ DỊCH VỤ CỐ ĐỊNH:")
//        invoice.fixedUtilityFees.forEach { fee ->
//            Log.d("ContractViewModel", "- ${fee.name}:")
//            Log.d("ContractViewModel", "  • Đơn giá gốc: ${fee.unitPrice}")
//            Log.d("ContractViewModel", "  • Đơn vị tính: ${fee.unit}")
//            Log.d("ContractViewModel", "  • Số lượng: ${fee.quantity}")
//            Log.d("ContractViewModel", "  • Tổng tiền theo ngày: ${fee.subtotal}")
//        }
//
//        // Chi tiết phí dịch vụ biến động
//        Log.d("ContractViewModel", "\n💧 PHÍ DỊCH VỤ BIẾN ĐỘNG:")
//        invoice.variableUtilityFees.forEach { fee ->
//            Log.d("ContractViewModel", "- ${fee.name}:")
//            Log.d("ContractViewModel", "  • Đơn vị tính: ${fee.unit}")
//            Log.d("ContractViewModel", "  • Chưa nhập chỉ số")
//        }
//
//        // Tổng kết
//        Log.d("ContractViewModel", "\n💰 TỔNG HỢP:")
//        Log.d("ContractViewModel", "- Tiền phòng: ${invoice.roomPrice}")
//        Log.d(
//            "ContractViewModel",
//            "- Tổng phí cố định: ${invoice.fixedUtilityFees.sumOf { it.subtotal }}"
//        )
//        Log.d("ContractViewModel", "- Tổng tiền thanh toán: ${invoice.totalAmount}")
//
//        Log.d("ContractViewModel", "\n----------------------------")
//    }
//
//
//
//    return invoiceSchedules
//}
//
///**
// * Lưu lịch hóa đơn vào Firestore
// */
//fun saveInvoiceSchedule(
//    contractId: String,
//    invoiceSchedules: List<MonthlyInvoiceSchedule>,
//    onSuccess: () -> Unit,
//    onFailure: (Exception) -> Unit
//) {
//    viewModelScope.launch {
//        try {
//            val batch = FirebaseFirestore.getInstance().batch()
//
//            invoiceSchedules.forEach { schedule ->
//                val invoiceRef = db.collection("MonthlyInvoiceSchedules").document()
//                batch.set(invoiceRef, schedule)
//            }
//
//            batch.commit().await()
//            onSuccess()
//        } catch (e: Exception) {
//            onFailure(e)
//        }
//    }
//}
//
///**
// * Cập nhật chi phí biến động cho một hóa đơn cụ thể
// */
//fun updateVariableFeesForInvoice(
//    invoiceId: String,
//    variableFees: List<com.ph32395.staynow.TaoHopDong.com.ph32395.staynow.TaoHopDong.com.ph32395.staynow.TaoHopDong.UtilityFeeDetail>,
//    onSuccess: () -> Unit,
//    onFailure: (Exception) -> Unit
//) {
//    viewModelScope.launch {
//        try {
//            val invoiceRef = db.collection("MonthlyInvoiceSchedules").document(invoiceId)
//
//            // Lấy hóa đơn hiện tại
//            val currentInvoice = invoiceRef.get().await().toObject(MonthlyInvoiceSchedule::class.java)
//
//            // Tính toán lại tổng số tiền
//            val updatedTotalAmount = currentInvoice?.let { invoice ->
//                invoice.roomPrice +
//                        invoice.fixedUtilityFees.sumOf { it.subtotal } +
//                        variableFees.sumOf { it.subtotal }
//            } ?: 0.0
//
//            // Cập nhật hóa đơn
//            invoiceRef.update(
//                mapOf(
//                    "variableUtilityFees" to variableFees,
//                    "totalAmount" to updatedTotalAmount,
//                    "status" to com.ph32395.staynow.TaoHopDong.com.ph32395.staynow.TaoHopDong.com.ph32395.staynow.TaoHopDong.InvoiceStatus.PENDING.name
//                )
//            ).await()
//
//            onSuccess()
//        } catch (e: Exception) {
//            onFailure(e)
//        }
//    }
//}