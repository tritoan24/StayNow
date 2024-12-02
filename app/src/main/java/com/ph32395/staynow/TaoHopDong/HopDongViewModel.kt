package com.ph32395.staynow.TaoHopDong

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import gun0912.tedimagepicker.util.ToastUtil.context
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
import java.text.NumberFormat
import java.util.Locale

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
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            var idHopDong: String = ""
            try {
                Log.d(
                    "HopDongViewModel",
                    "Starting saveContract with appointmentId: $appointmentId"
                )

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
                        val contractRef = if (contract.maHopDong.isEmpty()) {
                            val newDoc = contractsCollection.document()
                            val newContractId = newDoc.id
                            contractData["maHopDong"] = newContractId

                            // Tạo một bản sao của hóa đơn với ID hợp đồng được cập nhật
                            val updatedInvoice =
                                contract.hoaDonHopDong.copy(idHopDong = newContractId)

                            //chuyển sang màn chi tiết hóa đơn hợp đồng
                            idHopDong = newContractId

//                            //chuyển màn
//                            val intent = Intent(requireContext(), ChiTietHoaDon::class.java)
//                            intent.putExtra("idHopDong",idHopDong)
//                            startActivity(intent)



                            // Lưu hóa đơn vào subcollection của hợp đồng
                            val invoiceRef = newDoc.collection("hoaDonhopdong").document()
                            val invoiceData = createBillMap(updatedInvoice)
                            transaction.set(invoiceRef, invoiceData)

                            newDoc.set(contractData)
                            newDoc

                        } else {
                            val existingDoc = contractsCollection.document(contract.maHopDong)
                            idHopDong = contract.maHopDong
                            existingDoc
                        }

                        transaction.set(contractRef, contractData, SetOptions.merge())

                        // Cập nhật trạng thái phòng
                        val roomRef = roomsCollection.document(contract.thongtinphong.maPhongTro)
                        transaction.update(roomRef, "Trang_thaiphong", true)

                        // Xóa lịch hẹn
                        Log.d(
                            "HopDongViewModel",
                            "Attempting to delete appointment: $appointmentId"
                        )
                        transaction.delete(appointmentRef)
                    }.await()
                }

                Log.d("HopDongViewModel", "Transaction completed successfully")
                onSuccess(idHopDong)
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
            "maHopDong" to contract.maHopDong,
            "ngayTao" to contract.ngayTao,
            "trangThai" to contract.trangThai.name,
            "ngayBatDau" to contract.ngayBatDau,
            "ngayKetThuc" to contract.ngayKetThuc,
            "thoiHanThue" to contract.thoiHanThue,
            "ngayThanhToan" to contract.ngayThanhToan,
            "thongTinPhong" to createRoomInfoMap(contract.thongtinphong),
            "maPhong" to contract.thongtinphong.maPhongTro,
            "chuNha" to createLandlordInfoMap(contract.chuNha),
            "nguoiThue" to createTenantInfoMap(contract.nguoiThue),
            "thongTinTaiChinh" to createFinancialInfoMap(contract.thongTinTaiChinh),
            "tienNghi" to contract.tienNghi,
            "noiThat" to contract.noiThat,
            "dieuKhoan" to contract.dieuKhoan,
            "soNguoiO" to contract.soNguoiO,
            "hoaDonHopDong" to createBillMap(contract.hoaDonHopDong),
            "ngayThanhToan" to contract.ngayThanhToan,
            "ghiChu" to contract.ghiChu,


            )
    }
    private fun createRoomInfoMap(roomInfo: RoomInfo): HashMap<String, Any> {
        return hashMapOf(
            "maPhongTro" to roomInfo.maPhongTro,
            "tenPhong" to roomInfo.tenPhong,
            "diaChiPhong" to roomInfo.diaChiPhong,
            "dienTich" to roomInfo.dienTich,
            "thongTinChiTiet" to createRoomDetailsMap(roomInfo.thongTinChiTiet) // Gọi hàm để xử lý danh sách RoomDetail
        )
    }

    private fun createRoomDetailsMap(details: List<RoomDetail>): List<HashMap<String, Any>> {
        return details.map { detail ->
            hashMapOf(
                "ten" to detail.ten,
                "giaTri" to detail.giaTri,
                "donVi" to detail.donVi
            )
        }

    }

    private fun createLandlordInfoMap(landlord: PersonInfo): HashMap<String, Any> {
        return hashMapOf(
            "maNguoiDung" to landlord.maNguoiDung,
            "hoTen" to landlord.hoTen,
            "soCCCD" to landlord.soCCCD,
            "ngaySinh" to landlord.ngaySinh,
            "gioiTinh" to landlord.gioiTinh,
            "soDienThoai" to landlord.soDienThoai,
            "diaChi" to landlord.diaChi,
            "ngayCapCCCD" to landlord.ngayCapCCCD
        )
    }

    private fun createTenantInfoMap(tenant: PersonInfo): HashMap<String, Any> {
        return hashMapOf(
            "maNguoiDung" to tenant.maNguoiDung,
            "hoTen" to tenant.hoTen,
            "soCCCD" to tenant.soCCCD,
            "ngaySinh" to tenant.ngaySinh,
            "gioiTinh" to tenant.gioiTinh,
            "soDienThoai" to tenant.soDienThoai,
            "diaChi" to tenant.diaChi,
            "ngayCapCCCD" to tenant.ngayCapCCCD
        )
    }

    private fun createFinancialInfoMap(financialInfo: FinancialInfo): HashMap<String, Any> {
        return hashMapOf(
            "giaThue" to financialInfo.giaThue,
            "tienCoc" to financialInfo.tienCoc,
            "phuongThucThanhToan" to financialInfo.phuongThucThanhToan,
            "soNuocht" to financialInfo.soNuocht,
            "soDienht" to financialInfo.soDienht,
            "soNguoio" to financialInfo.soNguoio,
            "phiDichVu" to financialInfo.phiDichVu.map { fee ->
                hashMapOf(
                    "tenDichVu" to fee.tenDichVu,
                    "giaTien" to fee.giaTien,
                    "donVi" to fee.donVi,
                    "batBuoc" to fee.batBuoc
                )
            }
        )
    }

    private fun createBillMap(ivoices: Invoice): HashMap<String, Any> {
        return hashMapOf(
            "idHoaDon" to ivoices.idHoaDon,
            "idNguoigui" to ivoices.idNguoigui,
            "idNguoinhan" to ivoices.idNguoinhan,
            "tenKhachHang" to ivoices.tenKhachHang,
            "tenPhong" to ivoices.tenPhong,
            "ngayLap" to ivoices.ngayLap,
            "kyHoaDon" to ivoices.kyHoaDon,
            "idHopDong" to ivoices.idHopDong,
            "kieuHoadon" to ivoices.kieuHoadon,
            "tongTien" to ivoices.tongTien,
            "trangThai" to ivoices.trangThai,
            "tienPhong" to ivoices.tienPhong,
            "tienCoc" to ivoices.tienCoc,
            "tongTienDichVu" to ivoices.tongTienDichVu,
            "phiCoDinh" to ivoices.phiCoDinh.map { fee ->
                hashMapOf(
                    "tenDichVu" to fee.tenDichVu,
                    "giaTien" to fee.giaTien,
                    "donVi" to fee.donVi,
                    "soLuong" to fee.soLuong,
                    "thanhTien" to fee.thanhTien
                )
            },
            "phiBienDong" to ivoices.phiBienDong.map { fee ->
                hashMapOf(
                    "tenDichVu" to fee.tenDichVu,
                    "giaTien" to fee.giaTien,
                    "donVi" to fee.donVi,
                    "soLuong" to fee.soLuong,
                    "thanhTien" to fee.thanhTien
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
     * Lấy danh sách hợp đồng theo chủ nhà.
     */
    suspend fun getContractsByLandlord(landlordId: String): List<HopDong> {
        return getContracts("chuNha.maNguoiDung", landlordId)
    }

    /**
     * Lấy danh sách hợp đồng theo người thuê và trạng thái
     */

    fun getContractsByTenant(
        tenantId: String,
        status: ContractStatus? = null,
        onContractsChanged: (List<HopDong>) -> Unit
    ) {
        getContracts("nguoiThue.maNguoiDung", tenantId, status, onContractsChanged)
    }

    private fun getContracts(
        field: String,
        value: String,
        status: ContractStatus? = null,
        onContractsChanged: (List<HopDong>) -> Unit
    ) {
        try {
            var query = contractsCollection.whereEqualTo(field, value)

            // Nếu status được cung cấp, thêm điều kiện lọc theo trạng thái
            status?.let {
                query = query.whereEqualTo(
                    "trangThai",
                    status.name
                )  // Giả sử bạn đang lưu trạng thái dưới dạng String
            }

            // Lắng nghe sự thay đổi dữ liệu trong Firestore
            query.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.w("ContractViewModel", "Listen failed.", exception)
                    return@addSnapshotListener
                }

                // Nếu dữ liệu thay đổi, parse và gọi callback
                val contracts = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(HopDong::class.java)
                } ?: emptyList()

                // Gọi callback để cập nhật danh sách hợp đồng trong UI
                onContractsChanged(contracts)
            }
        } catch (e: Exception) {
            Log.e("ContractViewModel", "Error fetching contracts", e)
            onContractsChanged(emptyList())
        }
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
    // Thêm ViewModel để xử lý logic liên quan đến hợp đồng
    private val contractRepository = HopDongViewModel()
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


}