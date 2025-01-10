package com.ph32395.staynow_datn.BaoMat

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.ph32395.staynow_datn.Adapter.ToCaoTaiKhoanAdapter
import com.ph32395.staynow_datn.R
import java.util.*

class ToCaoTaiKhoan : AppCompatActivity() {

    private lateinit var imgBackToCao: ImageButton
    private lateinit var imgToCao: ImageView
    private lateinit var rcToCao: RecyclerView
    private lateinit var editTenChuTro: EditText
    private lateinit var editVanDe: EditText
    private lateinit var btnToCao: Button
    private lateinit var mDatabase: DatabaseReference

    private val imageUriList = mutableListOf<Uri>() // Lưu URL của ảnh đã tải lên Firebase Storage
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var maNguoiDung: String? = null // Biến để lưu mã người dùng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_to_cao_tai_khoan)

        // Ánh xạ View
        imgBackToCao = findViewById(R.id.backScreenToCao)
        imgToCao = findViewById(R.id.tocao_avatar)
        rcToCao = findViewById(R.id.RcToCao)
        editTenChuTro = findViewById(R.id.tocao_Tennguoi)
        editVanDe = findViewById(R.id.tocao_Vande)
        btnToCao = findViewById(R.id.btnToCao)

        // Cài đặt RecyclerView
        rcToCao.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val adapter = ToCaoTaiKhoanAdapter(imageUriList)
        rcToCao.adapter = adapter

        // Mở thư viện khi click imgToCao
        imgToCao.setOnClickListener {
            selectImageFromGallery()
        }


        mDatabase = FirebaseDatabase.getInstance().getReference()

        // Nhận userId từ Intent và truy vấn Firebase
        val userId = intent.getStringExtra("idUser")
        if (userId != null) {
            Log.d("ToCaoTaiKhoan", "User ID: $userId")
            mDatabase.child("NguoiDung").child(userId).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val name = snapshot.child("hoTen").value.toString().trim()
                        editTenChuTro.setText(name.ifEmpty { "Chưa cập nhật" })

                        // Lấy mã người dùng từ Firebase
                        maNguoiDung = snapshot.child("maNguoiDung").value.toString()

                        // Làm cho trường không thể chỉnh sửa
                        editTenChuTro.isFocusable = false
                        editTenChuTro.isFocusableInTouchMode = false
                        editTenChuTro.isClickable = false
                    } else {
                        Log.e("ToCaoTaiKhoan", "Người dùng không tồn tại trong cơ sở dữ liệu.")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ToCaoTaiKhoan", "Lỗi khi lấy dữ liệu người dùng: ${error.message}")
                }
            })
        } else {
            Log.e("ToCaoTaiKhoan", "Không có userId trong Intent")
        }

        imgBackToCao.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val maNguoiDung = sharedPreferences.getString("maNguoiDung", null)

        // Lưu dữ liệu khi click btnToCao
        btnToCao.setOnClickListener {
            val toCaoData = hashMapOf(
                "tenChuTro" to editTenChuTro.text.toString(),
                "vanDe" to editVanDe.text.toString(),
                "images" to imageUriList.map { it.toString() }, // Lưu URL từ Firebase Storage
                "maNguoiBiToCao" to userId,
                "maNguoiToCao" to maNguoiDung // Gửi mã người dùng vào Firestore
            )
            firestore.collection("ToCaoTaiKhoan").add(toCaoData).addOnSuccessListener {
                Toast.makeText(this, "Tố cáo được gửi thành công", Toast.LENGTH_SHORT).show()
                editTenChuTro.text.clear()
                // Xóa dữ liệu trên các ô nhập liệu
                editVanDe.text.clear()
                // Xóa danh sách ảnh và cập nhật RecyclerView
                imageUriList.clear()
                rcToCao.adapter?.notifyDataSetChanged()

                // Hiển thị lại nút imgToCao nếu cần
                imgToCao.visibility = ImageView.VISIBLE
            }.addOnFailureListener {
                Toast.makeText(this, "Lỗi khi gửi tố cáo, vui lòng thử lại!", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // Cho phép chọn nhiều ảnh
        selectImageLauncher.launch(intent)
    }

    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                data?.let {
                    // Nếu chọn nhiều ảnh
                    if (data.clipData != null) {
                        val clipData = data.clipData
                        for (i in 0 until (clipData?.itemCount ?: 0)) {
                            val uri = clipData?.getItemAt(i)?.uri
                            uri?.let {
                                uploadImageToFirebase(it)
                            }
                        }
                    } else {
                        // Nếu chỉ chọn một ảnh
                        val uri = data.data
                        uri?.let {
                            uploadImageToFirebase(it)
                        }
                    }
                }
            }
        }

    // Hàm tải ảnh lên Firebase Storage
    private fun uploadImageToFirebase(uri: Uri) {
        val userId = intent.getStringExtra("idUser") ?: UUID.randomUUID().toString()
        val storageRef = storage.reference.child("images/$userId/${UUID.randomUUID()}.jpg")

        storageRef.putFile(uri)
            .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot ->
                // Lấy URL từ Firebase Storage
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    imageUriList.add(downloadUri) // Thêm URL vào danh sách
                    rcToCao.adapter?.notifyDataSetChanged()

                    // Ẩn imgToCao sau khi tải lên thành công
                    imgToCao.visibility = ImageView.GONE

                    Toast.makeText(this, "Tải ảnh lên thành công!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Tải ảnh lên thất bại: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
