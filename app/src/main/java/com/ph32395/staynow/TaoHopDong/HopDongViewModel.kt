package com.ph32395.staynow.TaoHopDong

import ContractStatus
import FinancialInfo
import HopDong
import Invoice
import PersonInfo
import RoomDetail
import UtilityFee
import UtilityFeeDetail
import UtilityFeeUiState
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.UserInfo
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.jetbrains.annotations.Contract

class HopDongViewModel {
    private val db = FirebaseFirestore.getInstance()
    private val contractsCollection = db.collection("HopDong")
    private val roomsCollection = db.collection("PhongTro")
    private val appointmentsCollection = db.collection("DatPhong")


    /**
     * Lưu hợp đồng mới hoặc cập nhật hợp đồng hiện có.
     * @param contract Hợp đồng cần lưu.
     * @param appointmentId ID của lịch hẹn cần xóa
     * @param onSuccess Hàm callback khi lưu thành công.
     * @param onFailure Hàm callback khi lưu thất bại.
     */
    suspend fun saveContract(
        contract: HopDong,
        appointmentId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("HopDongViewModel", "Starting saveContract with appointmentId: $appointmentId")

                if (appointmentId.isEmpty()) {
                    onFailure(Exception("appointmentId is empty"))
                    return@withContext
                }

                val contractData = createContractDataMap(contract)

                withTimeout(20000) { // Set timeout 20 giây cho transaction
                    db.runTransaction { transaction ->
                        // Kiểm tra xem lịch hẹn có tồn tại không
                        val appointmentRef = appointmentsCollection.document(appointmentId)
                        val appointmentDoc = transaction.get(appointmentRef)
                        if (!appointmentDoc.exists()) {
                            throw Exception("Lịch hẹn không tồn tại")
                        }

                        // Lưu hợp đồng
                        val contractRef = if (contract.contractId.isEmpty()) {
                            val newDoc = contractsCollection.document()
                            val newContractId = newDoc.id
                            contractData["maHopDong"] = newContractId

                            // Tạo một bản sao của hóa đơn với ID hợp đồng được cập nhật
                            val updatedInvoice = contract.invoice.copy(idHopdong = newContractId)

                            // Lưu hóa đơn vào subcollection của hợp đồng
                            val invoiceRef = newDoc.collection("hoaDonhopdong").document()
                            val invoiceData = createBillMap(updatedInvoice)
                            transaction.set(invoiceRef, invoiceData)

                            newDoc.set(contractData)
                            newDoc

                        } else {
                            contractsCollection.document(contract.contractId)
                        }

                        transaction.set(contractRef, contractData, SetOptions.merge())

                        // Cập nhật trạng thái phòng
                        val roomRef = roomsCollection.document(contract.roomInfo.roomId)
                        transaction.update(roomRef, "Trang_thaiphong", true)

                        // Xóa lịch hẹn
                        Log.d("HopDongViewModel", "Attempting to delete appointment: $appointmentId")
                        transaction.delete(appointmentRef)
                    }.await()
                }

                Log.d("HopDongViewModel", "Transaction completed successfully")
                onSuccess()
            } catch (e: Exception) {
                Log.e("HopDongViewModel", "Error in saveContract: ${e.message}")
                onFailure(e)
            }
        }
    }



    /**
     * Tạo Map dữ liệu từ RentalContract để lưu vào Firestore.
     */
    private fun createContractDataMap(contract: HopDong): HashMap<String, Any> {
        return hashMapOf(
            "maHopDong" to contract.contractId,
            "ngayTao" to contract.createdDate,
            "trangThai" to contract.status.name,
            "ngayBatDau" to contract.startDate,
            "ngayKetThuc" to contract.endDate,
            "thoiHanThue" to contract.rentDuration,
            "ngayThanhToan" to contract.paymentDay,
            "maPhong" to contract.roomInfo.roomId,
            "tenPhong" to contract.roomInfo.roomName,
            "diaChiPhong" to contract.roomInfo.address,
            "dienTich" to contract.roomInfo.area,
            "thongTinChiTiet" to createRoomDetailsMap(contract.roomInfo.details),
            "chuNha" to createLandlordInfoMap(contract.landlordInfo),
            "nguoiThue" to createTenantInfoMap(contract.tenantInfo),
            "thongTinTaiChinh" to createFinancialInfoMap(contract.financialInfo),
            "tienNghi" to contract.amenities,
            "noiThat" to contract.furniture,
            "hoaDonhopdong" to createBillMap(contract.invoice),
            "dieuKhoan" to contract.terms
        )
    }

    private fun createRoomDetailsMap(details: List<RoomDetail>): List<HashMap<String, Any>> {
        return details.map { detail ->
            hashMapOf(
                "ten" to detail.name,
                "giaTri" to detail.value,
                "donVi" to detail.unit
            )
        }
    }

    private fun createLandlordInfoMap(landlord: PersonInfo): HashMap<String, Any> {
        return hashMapOf(
            "maNguoiDung" to landlord.userId,
            "hoTen" to landlord.fullName,
            "soCCCD" to landlord.idNumber,
            "ngaySinh" to landlord.dateOfBirth,
            "gioiTinh" to landlord.gender,
            "soDienThoai" to landlord.phone,
            "diaChi" to landlord.address,
            "ngayCapCCCD" to landlord.idIssueDate
        )
    }

    private fun createTenantInfoMap(tenant: PersonInfo): HashMap<String, Any> {
        return hashMapOf(
            "maNguoiDung" to tenant.userId,
            "hoTen" to tenant.fullName,
            "soCCCD" to tenant.idNumber,
            "ngaySinh" to tenant.dateOfBirth,
            "gioiTinh" to tenant.gender,
            "soDienThoai" to tenant.phone,
            "diaChi" to tenant.address,
            "ngayCapCCCD" to tenant.idIssueDate
        )
    }

    private fun createFinancialInfoMap(financialInfo: FinancialInfo): HashMap<String, Any> {
        return hashMapOf(
            "giaThue" to financialInfo.monthlyRent,
            "tienCoc" to financialInfo.deposit,
            "phuongThucThanhToan" to financialInfo.paymentMethod,
            "soNuocht" to financialInfo.sonuocht,
            "soDienht" to financialInfo.sodienht,
            "soNguoio" to financialInfo.songuoio,
            "phiDichVu" to financialInfo.utilities.map { fee ->
                hashMapOf(
                    "tenDichVu" to fee.name,
                    "giaTien" to fee.amount,
                    "donVi" to fee.unit,
                    "batBuoc" to fee.isRequired
                )
            }
        )
    }
    private fun createBillMap(ivoices: Invoice): HashMap<String, Any> {
        return hashMapOf(
            "idHoaDon" to ivoices.idHoadon,
            "idNguoigui" to ivoices.idnguoigui,
            "idNguoinhan" to ivoices.idnguoinhan,
            "tenKhachHang" to ivoices.customerName,
            "tenPhong" to ivoices.roomName,
            "ngayLap" to ivoices.invoiceDate,
            "kyHoaDon" to ivoices.dueDate,
            "idHopDong" to ivoices.idHopdong,
            "kieuHoadon" to ivoices.type,
            "tongTien" to ivoices.totalAmount,
            "trangThai" to ivoices.status,
            "tienPhong" to ivoices.roomprice,
            "tienCoc" to ivoices.depositAmount,
            "tongTienDichVu" to ivoices.totalFeeService,
            "phiCoDinh" to ivoices.feeDefault.map { fee ->
                hashMapOf(
                    "tenDichVu" to fee.name,
                    "giaTien" to fee.unitPrice,
                    "donVi" to fee.unit,
                    "soLuong" to fee.quantity,
                    "thanhTien" to fee.subtotal
                )
            },
            "phiBienDong" to ivoices.feeVariable.map { fee ->
                hashMapOf(
                    "tenDichVu" to fee.name,
                    "giaTien" to fee.unitPrice,
                    "donVi" to fee.unit,
                    "soLuong" to fee.quantity,
                    "thanhTien" to fee.subtotal
                )
            }
        )
    }

    /**
     * Lấy thông tin hợp đồng theo ID.
     */
    suspend fun getContract(contractId: String): HopDong? {
        return try {
            val document = contractsCollection.document(contractId).get().await()
            document.toObject(HopDong::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Lấy danh sách hợp đồng theo người thuê.
     */
    suspend fun getContractsByTenant(tenantId: String): List<HopDong> {
        return getContracts("nguoiThue.maNguoiDung", tenantId)
    }

    /**
     * Lấy danh sách hợp đồng theo chủ nhà.
     */
    suspend fun getContractsByLandlord(landlordId: String): List<HopDong> {
        return getContracts("chuNha.maNguoiDung", landlordId)
    }

    private suspend fun getContracts(field: String, value: String): List<HopDong> {
        return try {
            contractsCollection
                .whereEqualTo(field, value)
                .get()
                .await()
                .toObjects(HopDong::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Cập nhật trạng thái hợp đồng.
     */
    suspend fun updateContractStatus(
        contractId: String,
        newStatus: ContractStatus,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            contractsCollection.document(contractId)
                .update("trangThai", newStatus.name)
                .await()
            onSuccess()
        } catch (e: Exception) {
            onFailure(e)
        }
    }

}
class ContractViewModel : ViewModel() {
    private val contractRepository = HopDongViewModel()
    private val _saveResult = MutableLiveData<Result<Unit>>()
    val saveResult: LiveData<Result<Unit>> = _saveResult

    //hóa đơn hợp đồng
    private val _uiState = MutableStateFlow(UtilityFeeUiState())
    val uiState: StateFlow<UtilityFeeUiState> = _uiState.asStateFlow()

    fun saveContract(contract: HopDong, appointmentId: String) {
        viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
            try {
                withTimeout(30000) { // Set timeout 30 giây
                    contractRepository.saveContract(
                        contract = contract,
                        appointmentId = appointmentId,
                        onSuccess = {
                            _saveResult.postValue(Result.success(Unit))
                            Log.d("ContractViewModel", "Lưu hợp đồng thành công")
                        },
                        onFailure = { exception ->
                            _saveResult.postValue(Result.failure(exception))
                            Log.e("ContractViewModel", "Lỗi khi lưu hợp đồng: ${exception.message}")
                        }
                    )
                }
            } catch (e: Exception) {
                _saveResult.postValue(Result.failure(e))
                Log.e("ContractViewModel", "Lỗi không mong muốn: ${e.message}")
            }
        }
    }


    fun extractVariableFees(utilityFees: List<UtilityFee>): List<UtilityFeeDetail> {
        return utilityFees
            .filter { it.name in listOf("Điện", "Nước") && it.unit in listOf("Số", "Khối") }
            .map { fee ->
                UtilityFeeDetail(
                    name = fee.name,
                    unitPrice = fee.amount,
                    unit = fee.unit,
                    quantity = 1, // Có thể thay đổi dựa trên thực tế
                    subtotal = fee.amount // Có thể thay đổi dựa trên số lượng
                )
            }
    }

    fun extractFixedFees(
        utilityFees: List<UtilityFee>,
        peopleCount: Int,
    ): Pair<Double, List<UtilityFeeDetail>> {
        val feeDetails = utilityFees
            .filter {
                (it.name !in listOf("Điện", "Nước") || it.unit !in listOf("Số", "Khối")) &&
                        (it.unit in listOf("Người", "Phòng") || it.unit == null)
            }
            .map { fee ->
                val quantity = when (fee.unit) {
                    "Người" -> peopleCount
                    "Phòng" -> 1
                    else -> 1
                }
                val subtotal = fee.amount * quantity

                UtilityFeeDetail(
                    name = fee.name,
                    unitPrice = fee.amount,
                    unit = fee.unit,
                    quantity = quantity,
                    subtotal = subtotal
                )
            }

        val totalFee = feeDetails.sumOf { it.subtotal }
        return Pair(totalFee, feeDetails)
    }


}