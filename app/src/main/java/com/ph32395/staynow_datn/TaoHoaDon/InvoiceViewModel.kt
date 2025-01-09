package com.ph32395.staynow_datn.TaoHoaDon

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.ph32395.staynow_datn.TaoHopDong.InvoiceStatus

@Suppress("DEPRECATION")
class InvoiceViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val invoiceCollection = firestore.collection("HoaDon")

    // LiveData để quan sát danh sách hóa đơn
    private val _invoices = MutableLiveData<List<InvoiceMonthlyModel>>()
    val invoices: LiveData<List<InvoiceMonthlyModel>> get() = _invoices

    private val _invoice = MutableLiveData<InvoiceMonthlyModel?>()
    val invoice: LiveData<InvoiceMonthlyModel?> get() = _invoice

    // Thêm một hóa đơn mới
    fun addInvoice(
        invoice: InvoiceMonthlyModel,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val invoiceId = invoiceCollection.document().id // Tạo ID tự động
        val invoiceWithId = invoice.copy(idHoaDon = invoiceId)

        invoiceCollection.document(invoiceId).set(invoiceWithId)
            .addOnSuccessListener {
                onSuccess(invoiceId)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun fetchInvoiceById(invoiceId: String) {
        FirebaseFirestore.getInstance()
            .collection("HoaDon")
            .document(invoiceId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val invoice = documentSnapshot.toObject<InvoiceMonthlyModel>()
                    _invoice.value = invoice
                } else {
                    _invoice.value = null
                    Log.e("InvoiceViewModel", "Invoice not found for ID: $invoiceId")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("InvoiceViewModel", "Error fetching invoice by ID: ", exception)
                _invoice.value = null
            }
    }

    fun fetchInvoicesForUser(type: String, userId: String, trangThai: InvoiceStatus) {
        invoiceCollection
            .whereEqualTo("trangThai", trangThai)
            .whereEqualTo(type, userId)
            .addSnapshotListener { querySnapshot, exception ->
                if (exception != null) {
                    _invoices.value = emptyList()  // Trả về danh sách rỗng nếu có lỗi
                    // Bạn có thể xử lý lỗi tại đây nếu cần
                    Log.e("InvoiceViewModel", "Error fetching invoices: ", exception)
                    return@addSnapshotListener
                }

                // Nếu không có lỗi, xử lý dữ liệu
                querySnapshot?.let {
                    val invoicesList = it.documents.mapNotNull { document ->
                        document.toObject<InvoiceMonthlyModel>()
                    }
                    _invoices.value = invoicesList  // Cập nhật dữ liệu vào LiveData
                }
            }
    }

    fun fetchInvoicesByContractIdAndStatus(contractId: String, trangThai: InvoiceStatus?) {
        // Tạo truy vấn cơ bản dựa trên contractId
        var query = invoiceCollection.whereEqualTo("idHopDong", contractId)

        // Thêm điều kiện trạng thái nếu có
        if (trangThai != null) {
            query = query.whereEqualTo("trangThai", trangThai)
        }

        // Sử dụng addSnapshotListener để lắng nghe thay đổi
        query.addSnapshotListener { querySnapshot, exception ->
            if (exception != null) {
                _invoices.value = emptyList() // Trả về danh sách rỗng nếu xảy ra lỗi
                // Xử lý lỗi nếu cần
                Log.e("InvoiceViewModel", "Error fetching invoices: ", exception)
                return@addSnapshotListener
            }

            // Nếu thành công, cập nhật LiveData với danh sách hóa đơn
            val invoicesList =
                querySnapshot?.documents?.mapNotNull { it.toObject<InvoiceMonthlyModel>() }
            _invoices.value = invoicesList ?: emptyList()
        }
    }

    fun updateInvoiceStatus(invoiceId: String, newStatus: InvoiceStatus) {
        // Lấy tham chiếu đến Firestore collection chứa hóa đơn
        val invoiceRef = invoiceCollection.document(invoiceId)

        // Cập nhật trạng thái hóa đơn
        invoiceRef.update("trangThai", newStatus)
            .addOnSuccessListener {
                // Nếu thành công, có thể log hoặc thông báo thành công
                Log.d("InvoiceViewModel", "Invoice status updated successfully")
            }
            .addOnFailureListener { exception ->
                // Nếu có lỗi, log lỗi
                Log.e("InvoiceViewModel", "Error updating invoice status: ", exception)
            }
    }

    // Xóa một hóa đơn
    fun deleteInvoice(invoiceId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        invoiceCollection.document(invoiceId).delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}
