package com.ph32395.staynow_datn.BaoMat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.ph32395.staynow_datn.Adapter.ToCaoTaiKhoanAdapter
import com.ph32395.staynow_datn.R
import java.util.*

class ToCaoNguoiDung : AppCompatActivity() {

    private lateinit var imgBackToCaoNguoiDung: ImageButton
    private lateinit var imgToCaoNguoiDung: ImageView
    private lateinit var rcToCaoNguoiDung: RecyclerView
    private lateinit var editTenNguoiDung: EditText
    private lateinit var editVanDeNguoiDung: EditText
    private lateinit var btnToCaoNguoiDung: Button

    private val imageUriList = mutableListOf<Uri>() // Lưu URL của ảnh đã tải lên Firebase Storage
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_to_cao_nguoi_dung)

        // Initialize views
        imgBackToCaoNguoiDung = findViewById(R.id.backScreenToCao)
        imgToCaoNguoiDung = findViewById(R.id.tocao_avatarNguoiDung)
        rcToCaoNguoiDung = findViewById(R.id.RcToCaoNguoiDung)
        editTenNguoiDung = findViewById(R.id.tocao_TenNguoiDung)
        editVanDeNguoiDung = findViewById(R.id.tocao_VandeNguoiDung)
        btnToCaoNguoiDung = findViewById(R.id.btnToCaoNguoiDung)

        // Set up RecyclerView for images
        rcToCaoNguoiDung.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val adapter = ToCaoTaiKhoanAdapter(imageUriList)
        rcToCaoNguoiDung.adapter = adapter

        // Click to select image
        imgToCaoNguoiDung.setOnClickListener {
            selectImageFromGallery()
        }

        // Go back to previous screen
        imgBackToCaoNguoiDung.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Handle report submission
        btnToCaoNguoiDung.setOnClickListener {
            val toCaoData = hashMapOf(
                "tenNguoiDung" to editTenNguoiDung.text.toString(),
                "vanDe" to editVanDeNguoiDung.text.toString(),
                "images" to imageUriList.map { it.toString() } // Save image URLs to Firebase
            )

            firestore.collection("ToCaoNguoiDung").add(toCaoData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Tố cáo được gửi thành công", Toast.LENGTH_SHORT).show()
                    clearInputFields()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Lỗi khi gửi tố cáo, vui lòng thử lại!", Toast.LENGTH_SHORT).show()
                }
        }

        // Handle click event on editTenNguoiDung
        editTenNguoiDung.setOnClickListener {
            showNameWarningDialog()
        }
    }

    // Function to show a dialog when the user clicks on the name field
    private fun showNameWarningDialog() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Cảnh báo")
        builder.setMessage("Nếu bạn nhập sai tên người cần tố cáo, admin sẽ không thể hỗ trợ bạn. Hãy nhập thông tin chính xác.")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss() // Close the dialog
        }
        builder.setCancelable(false) // Prevent the dialog from being dismissed by clicking outside
        builder.show()
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // Allow selecting multiple images
        selectImageLauncher.launch(intent)
    }

    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                data?.let {
                    if (data.clipData != null) {
                        // Multiple images selected
                        val clipData = data.clipData
                        for (i in 0 until (clipData?.itemCount ?: 0)) {
                            val uri = clipData?.getItemAt(i)?.uri
                            uri?.let {
                                uploadImageToFirebase(it)
                            }
                        }
                    } else {
                        // Single image selected
                        val uri = data.data
                        uri?.let {
                            uploadImageToFirebase(it)
                        }
                    }
                }
            }
        }

    private fun uploadImageToFirebase(uri: Uri) {
        val storageRef = storage.reference.child("images/${UUID.randomUUID()}.jpg")

        storageRef.putFile(uri)
            .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot ->
                // Get the download URL
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    imageUriList.add(downloadUri) // Add URL to the list
                    rcToCaoNguoiDung.adapter?.notifyDataSetChanged()

                    // Hide the image select button
                    imgToCaoNguoiDung.visibility = ImageView.GONE

                    Toast.makeText(this, "Tải ảnh lên thành công!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Tải ảnh lên thất bại: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearInputFields() {
        editTenNguoiDung.text.clear()
        editVanDeNguoiDung.text.clear()
        imageUriList.clear()
        rcToCaoNguoiDung.adapter?.notifyDataSetChanged()
        imgToCaoNguoiDung.visibility = ImageView.VISIBLE
    }
}
