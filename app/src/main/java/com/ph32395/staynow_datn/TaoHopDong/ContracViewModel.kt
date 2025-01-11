package com.ph32395.staynow_datn.TaoHopDong

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow_datn.Model.NguoiDungModel
import com.ph32395.staynow_datn.TaoPhongTro.PhiDichVu
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

    private val _updateYCResult = MutableLiveData<Boolean>()
    val updateYCResult: LiveData<Boolean> get() = _updateYCResult

    private val _activeContracts = MutableLiveData<List<HopDong>>()
    val activeContracts: LiveData<List<HopDong>> get() = _activeContracts

    private val _pendingContracts = MutableLiveData<List<HopDong>>()
    val pendingContracts: LiveData<List<HopDong>> get() = _pendingContracts

    private val _expiredContracts = MutableLiveData<List<HopDong>>()
    val expiredContracts: LiveData<List<HopDong>> get() = _expiredContracts

    private val _cancelledContracts = MutableLiveData<List<HopDong>>()
    val cancelledContracts: LiveData<List<HopDong>> get() = _cancelledContracts

    private val _terminatedContracts = MutableLiveData<List<HopDong>>()
    val terminatedContracts: LiveData<List<HopDong>> get() = _terminatedContracts

    private val _terminatedProcessingContracts = MutableLiveData<List<HopDong>>()
    val terminatedProcessingContracts: LiveData<List<HopDong>> get() = _terminatedProcessingContracts

    private val _processingContracts = MutableLiveData<List<HopDong>>()
    val processingContracts: LiveData<List<HopDong>> get() = _processingContracts

    private val _allContracts = MutableLiveData<List<HopDong>>()
    val allContracts: LiveData<List<HopDong>> get() = _allContracts

    private val _userRoleLiveData = MutableLiveData<String>()
    val userRoleLiveData: LiveData<String> get() = _userRoleLiveData

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading


    private val _isElectricityInputVisible = MutableLiveData<Boolean>()
    val isElectricityInputVisible: LiveData<Boolean> = _isElectricityInputVisible

    private val _isWaterInputVisible = MutableLiveData<Boolean>()
    val isWaterInputVisible: LiveData<Boolean> = _isWaterInputVisible


    private val contractStatus = MutableLiveData<String>()
    private val errorMessage = MutableLiveData<String>()


    // Phương thức để kiểm tra và cập nhật trạng thái hiển thị
    fun updateUtilityInputVisibility(utilityFees: List<PhiDichVu>) {
        // Kiểm tra phí điện
        val electricityFee = utilityFees.find {
            it.tenDichVu == "Điện" && it.donVi == "Số"
        }
        _isElectricityInputVisible.value = electricityFee != null

        // Kiểm tra phí nước
        val waterFee = utilityFees.find {
            it.tenDichVu == "Nước" && it.donVi == "Khối"
        }
        _isWaterInputVisible.value = waterFee != null
    }

    // Phương thức hỗ trợ để kiểm tra xem một phí có phải là phí biến động không
    fun isDynamicFee(fee: UtilityFeeDetail): Boolean {
        return when (fee.tenDichVu) {
            "Điện" -> fee.donVi == "Số"
            "Nước" -> fee.donVi == "Khối"
            else -> false
        }
    }

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
                            Log.e(
                                "com.ph32395.staynow.TaoHopDong.ContractViewModel",
                                "Lỗi khi lưu hợp đồng: ${exception.message}"
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _saveResult.postValue(Result.failure(e))
                Log.e(
                    "com.ph32395.staynow.TaoHopDong.ContractViewModel",
                    "Lỗi không mong muốn: ${e.message}"
                )
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
                Log.e(
                    "com.ph32395.staynow.TaoHopDong.ContractViewModel",
                    "Lỗi khi lấy chi tiết hóa đơn",
                    exception
                )
            }
    }


    //lấy contract tenant theo userID và theo 1 hoặc nhiều trạng thái
//    ----------------------------


    private fun fetchContractsForContractFragement(
        fetchFunction: (String, Set<ContractStatus>, (List<HopDong>) -> Unit) -> Unit,
        userId: String,
        statuses: Set<ContractStatus>
    ) {
        fetchFunction(userId, statuses) { contracts ->
            updateLiveDataByStatusForContractFragment(contracts, statuses)
        }
    }

    fun fetchContractsByTenantForContractFragment(userId: String, statuses: Set<ContractStatus>) {
        fetchContractsForContractFragement(
            contractRepository::getContractsByTenant,
            userId,
            statuses
        )
    }

    fun fetchContractsByLandlordForContractFragment(userId: String, statuses: Set<ContractStatus>) {
        fetchContractsForContractFragement(
            contractRepository::getContractsByLandlord,
            userId,
            statuses
        )
    }

    //gom các contract và xử lí livedata
    private fun updateLiveDataByStatusForContractFragment(
        contracts: List<HopDong>,
        statuses: Set<ContractStatus>
    ) {
        val groupedContracts = contracts.groupBy { it.trangThai }
        statuses.forEach { status ->
            val filteredContracts = groupedContracts[status] ?: emptyList()
            when (status) {
                ContractStatus.ACTIVE -> _activeContracts.postValue(filteredContracts)
                ContractStatus.PENDING -> _pendingContracts.postValue(filteredContracts)
                ContractStatus.EXPIRED -> _expiredContracts.postValue(filteredContracts)
                ContractStatus.CANCELLED -> _cancelledContracts.postValue(filteredContracts)
                ContractStatus.TERMINATED -> _terminatedContracts.postValue(filteredContracts)
                ContractStatus.TERMINATED_PROCESSING -> _terminatedProcessingContracts.postValue(filteredContracts)
                ContractStatus.PROCESSING -> _processingContracts.postValue(filteredContracts)
            }
        }
    }


    private fun fetchContractsForBillManagement(
        fetchFunction: (String, Set<ContractStatus>, (List<HopDong>) -> Unit) -> Unit,
        userId: String,
        statuses: Set<ContractStatus>
    ) {
        fetchFunction(userId, statuses) { contracts ->
            updateLiveDataByStatusForBillMangement(contracts, statuses)
        }
    }

    fun fetchContractsByTenantForBillManagement(userId: String, statuses: Set<ContractStatus>) {
        fetchContractsForBillManagement(contractRepository::getContractsByTenant, userId, statuses)
    }

    fun fetchContractsByLandlordForBillManagement(userId: String, statuses: Set<ContractStatus>) {
        fetchContractsForBillManagement(
            contractRepository::getContractsByLandlord,
            userId,
            statuses
        )
    }


    //gom các contract và xử lí livedata
    private fun updateLiveDataByStatusForBillMangement(
        contracts: List<HopDong>,
        statuses: Set<ContractStatus>
    ) {
        val allContracts = mutableListOf<HopDong>()

        // Hợp nhất các trạng thái
        statuses.forEach { status ->
            allContracts.addAll(contracts.filter { it.trangThai == status })
        }

        // Cập nhật LiveData
        _allContracts.postValue(allContracts)
    }


    fun getUserRole(userId: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("NguoiDung").child(userId)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(NguoiDungModel::class.java)
                if (user != null) {
                    _userRoleLiveData.value = user.loaiTaiKhoan // Cập nhật giá trị vào LiveData
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi nếu có
            }
        })
    }


//-----------------------------------------------------------

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
                        Log.d(
                            "com.ph32395.staynow.TaoHopDong.ContractViewModel",
                            "Cập nhật trạng thái hợp đồng thành công"
                        )
                    },
                    onFailure = { exception ->
                        _updateResult.postValue(Result.failure(exception)) // Cập nhật trạng thái thất bại
                        Log.e(
                            "com.ph32395.staynow.TaoHopDong.ContractViewModel",
                            "Lỗi khi cập nhật trạng thái hợp đồng: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _updateResult.postValue(Result.failure(e)) // Xử lý lỗi ngoài ý muốn
                Log.e(
                    "com.ph32395.staynow.TaoHopDong.ContractViewModel",
                    "Lỗi không mong muốn khi cập nhật trạng thái: ${e.message}"
                )
            }
        }
    }

    fun updateIsCreateBillContract(
        contractId: String
    ) {
        viewModelScope.launch {
            try {
                contractRepository.updateIsCreateBillContract(contractId) {
                    _updateResult.postValue(Result.success(Unit))
                }
            } catch (e: Exception) {
                _updateResult.postValue(Result.failure(e)) // Xử lý lỗi ngoài ý muốn
                Log.e(
                    "com.ph32395.staynow.TaoHopDong.ContractViewModel",
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
                Log.e(
                    "com.ph32395.staynow.TaoHopDong.ContractViewModel",
                    "Lỗi khi lấy hợp đồng: ${e.message}",
                    e
                )
                _allContracts.value = emptyList() // Đảm bảo UI hiển thị danh sách rỗng
            } finally {
                _loading.value = false // Ẩn trạng thái đang tải
            }
        }
    }

    // Lưu thông tin nhắn tin vào bảng StatusMessages
    fun saveMessageStatus(tenantId: String, landlordId: String) {
        val contractMessagesRef = FirebaseFirestore.getInstance().collection("DieuKienChat")

        val contractMessageStatusData = hashMapOf(
            "maNguoiThue" to tenantId,
            "maNguoiChoThue" to landlordId,
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

    fun fetchPreviousUtilities(contractId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("HopDong")
            .document(contractId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {

                    val soDienCu = documentSnapshot.getLong("soDienCu")?.toInt() ?: 0
                    val soNuocCu = documentSnapshot.getLong("soNuocCu")?.toInt() ?: 0
                    // Log hoặc cập nhật LiveData nếu cần
                    Log.d("ContractViewModel", "Số điện cũ: $soDienCu, Số nước cũ: $soNuocCu")

                    // Bạn có thể dùng LiveData hoặc MutableLiveData để đẩy giá trị ra UI
                    _previousUtilities.value = Pair(soDienCu, soNuocCu)
                } else {
                    Log.e("ContractViewModel", "Hợp đồng không tồn tại.")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "ContractViewModel",
                    "Lỗi khi lấy số điện và nước cũ: ${exception.message}",
                    exception
                )
            }
    }

    fun updatePreviousUtilities(contractId: String, newSoDienCu: Int, newSoNuocCu: Int) {
        // Log toàn bộ thông tin để kiểm tra
        Log.d("ContractViewModel", "Original ContractId: $contractId")

        // Kiểm tra xem contractId có chứa "/" không
        val actualContractId = if (contractId.contains("/")) {
            contractId.split("/").last()
        } else {
            contractId
        }

        Log.d("ContractViewModel", "Processed ContractId: $actualContractId")

        val db = FirebaseFirestore.getInstance()

        try {
            val contractRef = db.collection("HopDong").document(actualContractId)

            // Tạo map chứa các trường cần cập nhật
            val updates = mapOf(
                "soDienCu" to newSoDienCu,
                "soNuocCu" to newSoNuocCu
            )

            // Cập nhật các trường soDienCu và soNuocCu
            contractRef.update(updates)
                .addOnSuccessListener {
                    Log.d("ContractViewModel", "Cập nhật số điện cũ và số nước cũ thành công")
                    contractStatus.value = "Cập nhật số điện và nước cũ thành công"
                }
                .addOnFailureListener { exception ->
                    Log.e(
                        "ContractViewModel",
                        "Lỗi khi cập nhật số điện và nước cũ: ${exception.message}",
                        exception
                    )
                    contractStatus.value =
                        "Có lỗi khi cập nhật số điện và nước cũ: ${exception.message}"
                }
        } catch (e: Exception) {
            Log.e("ContractViewModel", "Lỗi khi tạo document reference: ${e.message}", e)
            contractStatus.value = "Lỗi: ${e.message}"
        }
    }

    fun updateContractTerminationRequest(
        contractId: String,
        reason: String?,
        status: TerminationStatus
    ) {
        viewModelScope.launch {
            try {
                val success = contractRepository.updateContractTerminationRequest(contractId, reason, status)
                _updateYCResult.postValue(success)
            } catch (e: Exception) {
                Log.e("ContractViewModel", "Error: ${e.message}")
                _updateYCResult.postValue(false)
            }
        }
    }
    // LiveData để lưu trữ kết quả
    private val _previousUtilities = MutableLiveData<Pair<Int, Int>>()
    val previousUtilities: LiveData<Pair<Int, Int>> get() = _previousUtilities

}