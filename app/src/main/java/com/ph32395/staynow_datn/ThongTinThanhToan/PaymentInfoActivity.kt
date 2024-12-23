package com.ph32395.staynow_datn.ThongTinThanhToan
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.ph32395.staynow_datn.R
import java.util.UUID

class PaymentInfoActivity : AppCompatActivity() {

    private lateinit var etPhoneNumber: EditText
    private lateinit var etAccountName: EditText
    private lateinit var ivQrCode: ImageView
    private lateinit var btnUploadQr: Button
    private lateinit var btnSavePaymentInfo: Button
    private lateinit var mAuth : FirebaseAuth


    private var qrCodeUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_info)

        // Ánh xạ view
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        etAccountName = findViewById(R.id.etAccountName)
        ivQrCode = findViewById(R.id.ivQrCode)
        btnUploadQr = findViewById(R.id.btnUploadQr)
        btnSavePaymentInfo = findViewById(R.id.btnSavePaymentInfo)
        mAuth = FirebaseAuth.getInstance()

        // Xử lý tải mã QR lên
        btnUploadQr.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            startActivityForResult(intent, REQUEST_QR_CODE)
        }

        // Lưu thông tin thanh toán
        btnSavePaymentInfo.setOnClickListener {
            val phone = etPhoneNumber.text.toString()
            val accountName = etAccountName.text.toString()

            if (phone.isEmpty() || accountName.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Thực hiện lưu thông tin lên server
            savePaymentInfo(phone, accountName, qrCodeUri)
        }
    }

    // Kết quả chọn mã QR
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_QR_CODE && resultCode == Activity.RESULT_OK) {
            qrCodeUri = data?.data
            ivQrCode.setImageURI(qrCodeUri) // Hiển thị QR đã chọn
        }
    }
    private fun saveQrCodeToFirebaseStorage(uri: Uri, callback: (String?) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
        val qrCodeRef = storageRef.child("qr_codes/${UUID.randomUUID()}.jpg") // Tạo tên ngẫu nhiên cho file

        val uploadTask = qrCodeRef.putFile(uri)
        uploadTask.addOnSuccessListener {
            qrCodeRef.downloadUrl.addOnSuccessListener { downloadUri ->
                callback(downloadUri.toString()) // Trả về URL của ảnh QR đã upload
            }.addOnFailureListener {
                Toast.makeText(this, "Không thể lấy URL mã QR!", Toast.LENGTH_SHORT).show()
                callback(null)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Lỗi khi tải mã QR lên Firebase Storage!", Toast.LENGTH_SHORT).show()
            callback(null)
        }
    }

    private fun savePaymentInfo(phone: String, accountName: String, qrUri: Uri?) {
        val userId = mAuth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show()
            return
        }

        if (qrUri != null) {
            // Lưu ảnh QR lên Firebase Storage trước
            saveQrCodeToFirebaseStorage(qrUri) { qrUrl ->
                if (qrUrl != null) {
                    // Tiếp tục lưu thông tin thanh toán
                    savePaymentInfoToFirestore(userId, phone, accountName, qrUrl)
                } else {
                    Toast.makeText(this, "Lưu thông tin thanh toán thất bại!", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Nếu không có mã QR, lưu thông tin thanh toán không có URL QR
            savePaymentInfoToFirestore(userId, phone, accountName, null)
        }
    }

    private fun savePaymentInfoToFirestore(userId: String, phone: String, accountName: String, qrUrl: String?) {
        val firestore = FirebaseFirestore.getInstance()
        val paymentInfo = hashMapOf(
            "Sotaikhoan" to phone,
            "Tentaikhoan" to accountName,
            "maQR" to qrUrl
        )

        firestore.collection("ThongTinTT").document(userId)
            .set(paymentInfo)
            .addOnSuccessListener {
                updateTrangThaiPTTT(userId, true)
               finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lưu thông tin thanh toán thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun updateTrangThaiPTTT(userId: String, paymentStatus: Boolean) {
        // Tham chiếu đến Realtime Database
        val database = FirebaseDatabase.getInstance().reference

        // Dữ liệu cần cập nhật
        val updates = mapOf<String, Any>(
            "statusPttt" to paymentStatus
        )

        // Cập nhật trạng thái thanh toán vào bảng NguoiDung
        database.child("NguoiDung").child(userId)
            .updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Cập nhật trạng thái thanh toán thành công!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Cập nhật trạng thái thanh toán thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    companion object {
        private const val REQUEST_QR_CODE = 1
    }
}