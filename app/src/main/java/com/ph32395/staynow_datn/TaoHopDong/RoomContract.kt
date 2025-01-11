package com.ph32395.staynow_datn.TaoHopDong

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class RoomContract {
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
                            val invoiceRef = newDoc.collection("hoaDonHopDong").document()
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
                        val currentDate = Timestamp.now()

                        transaction.update(
                            roomRef, mapOf(
                                "trangThaiPhong" to true,
                                "ngayDuocThue" to currentDate
                            )
                        )


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
            "yeuCauChamDut" to contract.yeuCauChamDut,
            "daTaoHoaDonChamDut" to contract.daTaoHoaDonChamDut,
            "ngayBatDau" to contract.ngayBatDau,
            "ngayKetThuc" to contract.ngayKetThuc,
            "thoiHanThue" to contract.thoiHanThue,
            "ngayThanhToan" to contract.ngayThanhToan,
            "thongtinphong" to createRoomInfoMap(contract.thongtinphong),
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
            "soDienCu" to contract.soDienCu,
            "soNuocCu" to contract.soNuocCu

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

    private fun createRoomDetailsMap(details: List<RoomDetail?>): List<HashMap<String, Any>> {
        return details.map { detail ->
            hashMapOf(
                "ten" to (detail?.ten ?: ""),
                "giaTri" to (detail?.giaTri ?: ""),
                "donVi" to (detail?.donVi ?: "")
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
            "soDienCu" to ivoices.soDienCu,
            "soNuocCu" to ivoices.soNuocCu,
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
     * Lấy danh sách hợp đồng theo chủ trọ và trạng thái
     */
    fun getContractsByLandlord(
        landlordId: String,
        statuses: Set<ContractStatus>? = null, // Hỗ trợ nhiều trạng thái
        onContractsChanged: (List<HopDong>) -> Unit
    ) {
        getContracts("chuNha.maNguoiDung", landlordId, statuses, onContractsChanged)
    }

    /**
     * Lấy danh sách hợp đồng theo người thuê và trạng thái
     */

    fun getContractsByTenant(
        tenantId: String,
        statuses: Set<ContractStatus>? = null, // Hỗ trợ nhiều trạng thái
        onContractsChanged: (List<HopDong>) -> Unit
    ) {
        getContracts("nguoiThue.maNguoiDung", tenantId, statuses, onContractsChanged)
    }

    suspend fun updateContractTerminationRequest(
        contractId: String,
        reason: String?,
        status: TerminationStatus,
        onResult: (Boolean) -> Unit
    ) {
        if (contractId.isEmpty()) {
            onResult(false)
            return
        }

        val updates = mutableMapOf<String, Any>(
            "yeuCauChamDut" to status
        )

        // Nếu có lý do, thêm vào bản đồ cập nhật
        reason?.let {
            updates["lyDoChamDut"] = it
        }

        if (status == TerminationStatus.APPROVED) {
            val contractSnapshot = contractsCollection.document(contractId).get().await()
            val roomId = contractSnapshot.getString("maPhong")

            if (roomId != null) {
                // Cập nhật trạng thái phòng trọ
                roomsCollection.document(roomId)
                    .update(
                        mapOf(
                            "trangThaiPhong" to false,
                        )
                    )
                    .await()
            }
        }

        contractsCollection.document(contractId)
            .update(updates)
            .addOnSuccessListener {
                Log.d("HopDongRepository", "Fields updated successfully for $contractId")
                onResult(true)
            }
            .addOnFailureListener { e ->
                Log.e("HopDongRepository", "Error updating fields: ${e.message}")
                onResult(false)
            }
    }

    fun updateIsCreateBillContract(
        contractId: String,
        onResult: (Boolean) -> Unit,
    ) {
        if (contractId.isEmpty()) {
            onResult(false)
            return
        }

        val updates = mutableMapOf<String, Any>(
            "daTaoHoaDonChamDut" to true
        )

        contractsCollection.document(contractId)
            .update(updates)
            .addOnSuccessListener {
                Log.d("HopDongRepository", "Fields updated successfully for $contractId")
                onResult(true)
            }
            .addOnFailureListener { e ->
                Log.e("HopDongRepository", "Error updating fields: ${e.message}")
                onResult(false)
            }
    }

    private fun getContracts(
        field: String,
        value: String,
        statuses: Set<ContractStatus>? = null, // Hỗ trợ nhiều trạng thái
        onContractsChanged: (List<HopDong>) -> Unit
    ) {
        try {
            var query = contractsCollection.whereEqualTo(field, value)

            // Nếu danh sách trạng thái được cung cấp, thêm điều kiện lọc theo trạng thái
            statuses?.let {
                if (it.isNotEmpty()) {
                    val statusNames =
                        it.map { status -> status.name } // Lấy danh sách tên trạng thái
                    query =
                        query.whereIn("trangThai", statusNames) // whereIn để lọc nhiều trạng thái
                }
            }

            // Lắng nghe sự thay đổi dữ liệu trong Firestore
            query.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.w(
                        "com.ph32395.staynow.TaoHopDong.ContractViewModel",
                        "Listen failed.",
                        exception
                    )
                    return@addSnapshotListener
                }

                // Parse dữ liệu và gọi callback
                val contracts = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(HopDong::class.java)
                } ?: emptyList()

                // Gọi callback để cập nhật danh sách hợp đồng trong UI
                onContractsChanged(contracts)
            }
        } catch (e: Exception) {
            Log.e("com.ph32395.staynow.TaoHopDong.ContractViewModel", "Error fetching contracts", e)
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

            if (newStatus == ContractStatus.TERMINATED || newStatus == ContractStatus.CANCELLED) {
                // Lấy thông tin hợp đồng để tìm mã phòng
                val contractSnapshot = contractsCollection.document(contractId).get().await()
                val roomId = contractSnapshot.getString("maPhong")

                if (roomId != null) {
                    // Cập nhật trạng thái phòng trọ
                    roomsCollection.document(roomId)
                        .update(
                            mapOf(
                                "trangThaiPhong" to false,
                            )
                        )
                        .await()
                }
            }

            onSuccess()
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    suspend fun getAllContractsByUser(userId: String): List<HopDong> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val querySnapshot = db.collection("HopDong")
                .whereEqualTo("chuNha.maNguoiDung", userId)
                .get()
                .await() // Sử dụng await() để chờ kết quả của Firestore
            querySnapshot.documents.mapNotNull { document ->
                document.toObject(HopDong::class.java)
            }
        } catch (exception: Exception) {
            Log.e("RoomContract", "Lỗi khi lấy hợp đồng: ${exception.message}")
            emptyList() // Trả về danh sách rỗng nếu có lỗi
        }
    }


}
