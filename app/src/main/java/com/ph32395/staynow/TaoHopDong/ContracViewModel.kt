import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow.TaoHopDong.ContractStatus
import com.ph32395.staynow.TaoHopDong.HopDong
import com.ph32395.staynow.TaoHopDong.RoomContract
import com.ph32395.staynow.TaoHopDong.Invoice
import com.ph32395.staynow.TaoHopDong.UtilityFee
import com.ph32395.staynow.TaoHopDong.UtilityFeeDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class ContractViewModel : ViewModel() {
    // Thêm ViewModel để xử lý logic liên quan đến hợp đồng
    private val contractRepository = RoomContract()
    private val _saveResult = MutableLiveData<Result<Unit>>()
    val saveResult: LiveData<Result<Unit>> = _saveResult
    // Thêm StateFlow để theo dõi việc
    private val _navigateToContractDetail = MutableStateFlow<String?>(null)
    val navigateToContractDetail: StateFlow<String?> = _navigateToContractDetail.asStateFlow()
    // Thêm LiveData để theo dõi dữ liệu hóa đơn
    private val _invoiceDetails = MutableLiveData<Invoice>()
    val invoiceDetails: LiveData<Invoice> = _invoiceDetails


    private val _updateResult = MutableLiveData<Result<Unit>>()
    val updateResult: LiveData<Result<Unit>> = _updateResult

    private val _activeContracts = MutableLiveData<List<HopDong>>()
    val activeContracts: LiveData<List<HopDong>> get() = _activeContracts

    private val _pendingContracts = MutableLiveData<List<HopDong>>()
    val pendingContracts: LiveData<List<HopDong>> get() = _pendingContracts

    private val _expiredContracts = MutableLiveData<List<HopDong>>()
    val expiredContracts: LiveData<List<HopDong>> get() = _expiredContracts

    private val _terminatedContracts = MutableLiveData<List<HopDong>>()
    val terminatedContracts: LiveData<List<HopDong>> get() = _terminatedContracts

    private val _allContracts = MutableLiveData<List<HopDong>>()
    val allContracts: LiveData<List<HopDong>> get() = _allContracts

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    val contractStatus = MutableLiveData<String>()
    val errorMessage = MutableLiveData<String>()

    fun saveContract(contract: HopDong, appointmentId: String) {
        viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
            try {
                withTimeout(30000) { // Set timeout 30 giây
                    contractRepository.saveContract(
                        contract = contract,
                        appointmentId = appointmentId,
                        onSuccess = { contractId ->
                            // Thay vì startActivity, cập nhật LiveData/StateFlow
                            _navigateToContractDetail.value = contractId
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
            .filter { it.tenDichVu in listOf("Điện", "Nước") && it.donVi in listOf("Số", "Khối") }
            .map { fee ->
                UtilityFeeDetail(
                    tenDichVu = fee.tenDichVu,
                    giaTien = fee.giaTien,
                    donVi = fee.donVi,
                    soLuong = 1, // Có thể thay đổi dựa trên thực tế
                    thanhTien = fee.giaTien // Có thể thay đổi dựa trên số lượng
                )
            }
    }

    fun extractFixedFees(
        utilityFees: List<UtilityFee>,
        peopleCount: Int,
    ): Pair<Double, List<UtilityFeeDetail>> {
        val feeDetails = utilityFees
            .filter {
                (it.tenDichVu !in listOf("Điện", "Nước") || it.donVi !in listOf("Số", "Khối")) &&
                        (it.donVi in listOf("Người", "Phòng") || it.donVi == null)
            }
            .map { fee ->
                val quantity = when (fee.donVi) {
                    "Người" -> peopleCount
                    "Phòng" -> 1
                    else -> 1
                }
                val subtotal = fee.giaTien * quantity

                UtilityFeeDetail(
                    tenDichVu = fee.tenDichVu,
                    giaTien = fee.giaTien,
                    donVi = fee.donVi ?: "",
                    soLuong = quantity,
                    thanhTien = subtotal
                )
            }

        val totalFee = feeDetails.sumOf { it.thanhTien }
        return Pair(totalFee, feeDetails)
    }

    // Thêm phương thức resetNavigation
    fun resetNavigation() {
        _navigateToContractDetail.value = null
    }

    // Thêm phương thức fetchInvoiceDetails
    fun fetchInvoiceDetails(contractId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("HopDong")
            .document(contractId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val hopDong = documentSnapshot.toObject(HopDong::class.java)
                hopDong?.hoaDonHopDong?.let { invoice ->
                    _invoiceDetails.value = invoice
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ContractViewModel", "Lỗi khi lấy chi tiết hóa đơn", exception)
            }
    }
    fun fetchContractsByTenant(userId: String, status: ContractStatus) {
        contractRepository.getContractsByTenant(userId, status) { contracts ->
            // Khi có thay đổi, cập nhật vào LiveData
            when (status) {
                ContractStatus.ACTIVE -> _activeContracts.postValue(contracts)
                ContractStatus.PENDING -> _pendingContracts.postValue(contracts)
                ContractStatus.EXPIRED -> _expiredContracts.postValue(contracts)
                ContractStatus.TERMINATED -> _terminatedContracts.postValue(contracts)
            }
        }
    }

    // Hàm cập nhật trạng thái hợp đồng
    fun updateContractStatus(
        contractId: String,
        newStatus: ContractStatus
    ) {
        viewModelScope.launch {
            try {
                contractRepository.updateContractStatus(
                    contractId = contractId,
                    newStatus = newStatus,
                    onSuccess = {
                        _updateResult.postValue(Result.success(Unit)) // Cập nhật trạng thái thành công
                        Log.d("ContractViewModel", "Cập nhật trạng thái hợp đồng thành công")
                    },
                    onFailure = { exception ->
                        _updateResult.postValue(Result.failure(exception)) // Cập nhật trạng thái thất bại
                        Log.e(
                            "ContractViewModel",
                            "Lỗi khi cập nhật trạng thái hợp đồng: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _updateResult.postValue(Result.failure(e)) // Xử lý lỗi ngoài ý muốn
                Log.e(
                    "ContractViewModel",
                    "Lỗi không mong muốn khi cập nhật trạng thái: ${e.message}"
                )
            }
        }
    }
    //hàm lấy tất cả hợp đồng theo người dùng
    fun fetchAllContractsByUser(userId: String) {
        viewModelScope.launch {
            _loading.value = true // Hiển thị trạng thái đang tải
            try {
                val contracts = contractRepository.getAllContractsByUser(userId)
                _allContracts.value = contracts // Cập nhật LiveData
            } catch (e: Exception) {
                Log.e("ContractViewModel", "Lỗi khi lấy hợp đồng: ${e.message}", e)
                _allContracts.value = emptyList() // Đảm bảo UI hiển thị danh sách rỗng
            } finally {
                _loading.value = false // Ẩn trạng thái đang tải
            }
        }
    }


    // Lưu thông tin nhắn tin vào bảng StatusMessages
    fun saveMessageStatus(tenantId: String, landlordId: String) {
        val contractMessagesRef = FirebaseFirestore.getInstance().collection("StatusMessages")

        val contractMessageStatusData = hashMapOf(
            "tenantId" to tenantId,
            "landlordId" to landlordId,
        )

        // Lưu thông tin vào bảng StatusMessages
        contractMessagesRef.add(contractMessageStatusData)
            .addOnSuccessListener {
                // Lưu thành công
                contractStatus.value = "Thông tin hợp đồng đã được lưu thành công"
            }
            .addOnFailureListener { e ->
                // Lỗi khi lưu thông tin
                errorMessage.value = "Có lỗi khi lưu thông tin hợp đồng: ${e.message}"
            }
    }

    // Kiểm tra xem người thuê và người cho thuê đã có thông tin trong bảng contract_messages hay chưa
    fun checkMessageStatus(tenantId: String, landlordId: String) {
        val contractMessagesRef = FirebaseFirestore.getInstance().collection("contract_messages")
            .whereEqualTo("tenantId", tenantId)
            .whereEqualTo("landlordId", landlordId)
            .get()

        contractMessagesRef.addOnCompleteListener { task ->
            if (task.isSuccessful && !task.result.isEmpty) {
                // Nếu tìm thấy thông tin nhắn tin giữa người thuê và người cho thuê
                contractStatus.value = "Bạn có thể nhắn tin vì đã có thông tin hợp đồng"
            } else {
                // Không tìm thấy thông tin nhắn tin, thông báo hợp đồng chưa ký
                contractStatus.value = "Không có thông tin nhắn tin nào, vui lòng ký hợp đồng trước"
            }
        }
    }

}