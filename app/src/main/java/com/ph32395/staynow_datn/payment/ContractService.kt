package com.ph32395.staynow_datn.payment

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.ph32395.staynow_datn.TaoHopDong.HopDong

class ContractService {

    private val firestore = FirebaseFirestore.getInstance()

    fun updatePaymentStatusAndContractStatus(
        contractId: String,
        invoiceId: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        // Lấy tham chiếu đến tài liệu của hợp đồng
        val contractRef: DocumentReference = firestore.collection("HopDong").document(contractId)

        // Bắt đầu giao dịch
        firestore.runTransaction { transaction ->
            // Lấy thông tin hợp đồng
            val contractSnapshot = transaction.get(contractRef)

            // Kiểm tra nếu hợp đồng tồn tại
            if (!contractSnapshot.exists()) {
                throw Exception("Không tìm thấy hợp đồng")
            }

            // Lấy đối tượng hợp đồng từ snapshot
            val contractData = contractSnapshot.toObject(HopDong::class.java)

            // Kiểm tra nếu hoaDonHopDong trong hợp đồng có tồn tại và có idHoaDon trùng với invoiceId
            val hoaDon = contractData?.hoaDonHopDong
            if (hoaDon != null && hoaDon.idHoaDon == invoiceId) {
                // Cập nhật trạng thái thanh toán của hóa đơn thành PAID
                transaction.update(contractRef, "hoaDonHopDong.trangThai", "PAID")
                transaction.set(
                    contractRef,
                    mapOf(
                        "hoaDonHopDong" to mapOf(
                            "trangThai" to "PAID",
                            "paymentDate" to FieldValue.serverTimestamp()
                        ),
                        "trangThai" to "ACTIVE",
                        "updateAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
                // Cập nhật trạng thái của hợp đồng thành ACTIVE
                transaction.update(contractRef, "trangThai", "ACTIVE")
                transaction.update(
                    contractRef,
                    "updatedAt",
                    FieldValue.serverTimestamp()
                ) // Cập nhật thời gian

                // Giao dịch thành công
                return@runTransaction true
            } else {
                throw Exception("Không tìm thấy hóa đơn với ID: $invoiceId trong hợp đồng.")
            }
        }
            .addOnSuccessListener {
                // Thành công
                onComplete(true, "Cập nhật thành công!")
            }
            .addOnFailureListener { e ->
                // Lỗi
                Log.e("ContractService", "Lỗi cập nhật trạng thái: ${e.message}")
                onComplete(false, "Cập nhật thất bại: ${e.message}")
            }
    }
}
