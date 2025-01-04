package com.ph32395.staynow_datn.CCCD

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.ph32395.staynow_datn.R
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class CCCD : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var chooseImageButton: Button
    private lateinit var cameraExecutor: ExecutorService

    //khai báo viewmodel
    private val viewModel: CccdViewModel by viewModels()

    // Biến kiểm tra trạng thái quét mã QR
    private var isScanning = true

    private val SECRET_KEY = "MySecretKey12345"

    private val firestore = FirebaseFirestore.getInstance()
    private val mAuth = FirebaseAuth.getInstance()

    // Đối tượng xử lý kết quả chọn ảnh từ thư viện
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { processImageFromUri(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cccd)

        //khởi tạo viewmodel


        previewView = findViewById(R.id.cameraPreview)
        chooseImageButton = findViewById(R.id.chooseImageButton)

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Kiểm tra quyền truy cập camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission()
        } else {
            startCamera()
        }

        // Chọn ảnh từ thư viện
        chooseImageButton.setOnClickListener {
            getContent.launch("image/*")
        }
    }

    private fun requestCameraPermission() {
        val permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    startCamera()
                } else {
                    Toast.makeText(this, "Quyền truy cập camera bị từ chối", Toast.LENGTH_SHORT).show()
                }
            }
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        val preview = androidx.camera.core.Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor) { imageProxy -> processImageProxy(imageProxy) }
            }

        val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
    }

    private fun processImageProxy(imageProxy: ImageProxy) {
        if (!isScanning) {
            imageProxy.close() // Nếu không cần quét, đóng hình ảnh ngay lập tức
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient()

            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        barcode.rawValue?.let {
                            Log.d("QR Code", "Dữ liệu mã QR: $it")
                            isScanning = false
                            handleQRCodeData(it)
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("MLKit", "Lỗi khi quét mã QR: ${it.message}")
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun processImageFromUri(uri: Uri) {
        try {
            val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val scanner = BarcodeScanning.getClient()

            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        barcode.rawValue?.let {
                            Log.d("QR Code", "Dữ liệu mã QR từ ảnh: $it")
                            handleQRCodeData(it)
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("MLKit", "Lỗi khi quét mã QR từ ảnh: ${it.message}")
                }
        } catch (e: Exception) {
            Log.e("Error", "Lỗi xử lý ảnh: ${e.message}")
            Toast.makeText(this, "Không thể quét mã QR từ ảnh", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleQRCodeData(data: String) {
        val cccd = parseQRCodeData(data)["So_cccd"]
        cccd?.let {
            // Kiểm tra xem CCCD đã tồn tại trong Firestore chưa
            checkIfCccdExists(it) { exists ->
                if (exists) {
                    SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Thông báo")
                        .setContentText("CCCD đã tồn tại trong hệ thống!")
                        .setConfirmText("OK")
                        .setConfirmClickListener { dialog ->
                            dialog.dismissWithAnimation()
                        }
                        .show()
                } else {
                    SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Thông báo")
                        .setContentText("Thông tin: $data\nBạn có muốn lưu không?")
                        .setConfirmText("Lưu")
                        .setCancelText("Hủy")
                        .setConfirmClickListener { dialog ->
                            dialog.dismissWithAnimation()
                            saveToFirestore(data) // Lưu vào Firestore nếu CCCD chưa tồn tại
                        }
                        .show()
                }
            }
        }
    }

    private fun saveToFirestore(qrData: String) {
        val cccdData = parseQRCodeData(qrData)
        val userID = mAuth.currentUser?.uid ?: ""

        // Mã hóa dữ liệu trước khi lưu
        val encryptedData = cccdData.mapValues { (_, value) ->
            encrypt(value, SECRET_KEY)
        }

        firestore.collection("CCCD").document(userID)
            .set(encryptedData)
            .addOnSuccessListener {
                updateTrangThaiCCCD(userID, true)
                finish()
            }
            .addOnFailureListener { e ->
                println("Lỗi khi lưu CCCD: ${e.message}")
            }
    }

    private fun parseQRCodeData(qrData: String): Map<String, String> {
        val parts = qrData.split("|")
        return if (parts.size >= 7) {
            mapOf(
                "So_cccd" to parts[0],
                "Hovaten" to parts[2],
                "Ngaysinh" to formatDate(parts[3]),
                "Gioitinh" to parts[4],
                "Điachi" to parts[5],
                "Ngaycap" to formatDate(parts[6])
            )
        } else {
            emptyMap()
        }
    }

    private fun formatDate(date: String): String {
        return if (date.length == 8) {
            "${date.substring(0, 2)}/${date.substring(2, 4)}/${date.substring(4)}"
        } else {
            date
        }
    }
    private fun encrypt(data: String, secretKey: String): String {
        try {
            val keySpec = SecretKeySpec(secretKey.toByteArray(Charsets.UTF_8), "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("Encryption", "Lỗi mã hóa: ${e.message}")
            throw e
        }
    }



    private fun updateTrangThaiCCCD(userId: String, CCCDstatus: Boolean) {
        // Tham chiếu đến Realtime Database
        val database = FirebaseDatabase.getInstance().reference

        // Dữ liệu cần cập nhật
        val updates = mapOf<String, Any>(
            "trangThaiCCCD" to CCCDstatus
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


    fun checkIfCccdExists(cccd: String, onResult: (Boolean) -> Unit) {
        firestore.collection("CCCD")
            .get() // Lấy tất cả các tài liệu trong collection
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    var cccdFound = false
                    for (document in querySnapshot) {
                        val encryptedCccd = document.getString("So_cccd") // Giả sử trường CCCD là "So_cccd"
                        if (encryptedCccd != null) {
                            try {
                                // Giải mã CCCD
                                val decryptedCccd = viewModel.decrypt(encryptedCccd, SECRET_KEY)
                                if (decryptedCccd == cccd) {
                                    cccdFound = true
                                    break // Nếu tìm thấy thì dừng vòng lặp
                                }
                            } catch (e: Exception) {
                                Log.e("Decryption", "Lỗi giải mã CCCD: ${e.message}")
                            }
                        }
                    }
                    onResult(cccdFound) // Trả kết quả cho callback
                } else {
                    // Nếu không có tài liệu nào
                    onResult(false)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Lỗi kiểm tra CCCD: ${exception.message}")
                onResult(false) // Nếu có lỗi thì cho là không tìm thấy
            }
    }



    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
