package com.ph32395.staynow.TaoHopDong

import ContractStatus
import FinancialInfo
import HopDong
import PersonInfo
import RoomDetail
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.UserInfo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HopDongViewModel {
    private val db = FirebaseFirestore.getInstance()
    private val contractsCollection = db.collection("HopDong")
    private val roomsCollection = db.collection("PhongTro")

    /**
     * Lưu hợp đồng mới hoặc cập nhật hợp đồng hiện có.
     * @param contract Hợp đồng cần lưu.
     * @param onSuccess Hàm callback khi lưu thành công.
     * @param onFailure Hàm callback khi lưu thất bại.
     */
    suspend fun saveContract(
        contract: HopDong,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            // Tạo dữ liệu hợp đồng
            val contractData = createContractDataMap(contract)

            // Bắt đầu transaction để đảm bảo tính nhất quán của dữ liệu
            db.runTransaction { transaction ->
                // Lưu hợp đồng
                val contractRef = if (contract.contractId.isEmpty()) {
                    val newDoc = contractsCollection.document()
                    contractData["maHopDong"] = newDoc.id
                    newDoc
                } else {
                    contractsCollection.document(contract.contractId)
                }

                transaction.set(contractRef, contractData, SetOptions.merge())

                // Cập nhật trạng thái phòng
                val roomRef = roomsCollection.document(contract.roomInfo.roomId)
                transaction.update(roomRef, "Trang_thaiphong", true)
            }.await()

            onSuccess()
        } catch (e: Exception) {
            onFailure(e)
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

    fun saveContract(contract: HopDong) {
        viewModelScope.launch {
            try {
                contractRepository.saveContract(
                    contract = contract,
                    onSuccess = {
                        Log.d("HopDongViewModel", "Lưu hợp đồng và cập nhật trạng thái phòng thành công")
                    },
                    onFailure = { exception ->
                        Log.e("HopDongViewModel", "Lỗi khi lưu hợp đồng: ${exception.message}")
                    }
                )
            } catch (e: Exception) {
                Log.e("HopDongViewModel", "Lỗi không mong muốn: ${e.message}")
            }
        }
    }
}