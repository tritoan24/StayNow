package com.ph32395.staynow.TaoHoaDon
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.ph32395.staynow.TaoHopDong.Invoice

class InvoiceViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val invoiceCollection = firestore.collection("HoaDon")

    // LiveData để quan sát danh sách hóa đơn
    private val _invoices = MutableLiveData<List<Invoice>>()
    val invoices: LiveData<List<Invoice>> get() = _invoices

    // Thêm một hóa đơn mới
    fun addInvoice(invoice: Invoice, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val invoiceId = invoiceCollection.document().id // Tạo ID tự động
        val invoiceWithId = invoice.copy(idHoaDon = invoiceId)

        invoiceCollection.document(invoiceId).set(invoiceWithId)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Lấy danh sách hóa đơn từ Firestore
    fun fetchInvoices() {
        invoiceCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val invoicesList = querySnapshot.documents.mapNotNull { it.toObject<Invoice>() }
                _invoices.value = invoicesList
            }
            .addOnFailureListener { exception ->
                _invoices.value = emptyList()
                // Bạn có thể xử lý lỗi tại đây nếu cần
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
