package com.ph32395.staynow_datn.BaoMat

import android.app.Activity
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

class ToCaoPhongTro : AppCompatActivity() {

    private lateinit var imgBackToCaoPhong: ImageButton
    private lateinit var imgToCaoPhong: ImageView
    private lateinit var rcToCaoPhong: RecyclerView
    private lateinit var editTenPhongTro: EditText
    private lateinit var editVanDePhong: EditText
    private lateinit var btnToCaoPhong: Button
    private lateinit var mDatabase: DatabaseReference

    private val imageUriList = mutableListOf<Uri>() // Lưu URL của ảnh đã tải lên Firebase Storage
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var maNguoiDung: String? = null // Biến để lưu mã người dùng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_to_cao_phong_tro)

        // Ánh xạ View
        imgBackToCaoPhong = findViewById(R.id.backScreenToCao)
        imgToCaoPhong = findViewById(R.id.tocao_avatarPhong)
        rcToCaoPhong = findViewById(R.id.RcToCaoPhong)
        editTenPhongTro = findViewById(R.id.tocao_Tenphong)
        editVanDePhong = findViewById(R.id.tocao_VandePhong)
        btnToCaoPhong = findViewById(R.id.btnToCaoPhong)

        // Cài đặt RecyclerView
        rcToCaoPhong.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val adapter = ToCaoTaiKhoanAdapter(imageUriList)
        rcToCaoPhong.adapter = adapter

        // Mở thư viện khi click imgToCaoPhong
        imgToCaoPhong.setOnClickListener {
            selectImageFromGallery()
        }


        // Nhận maPhongTro từ Intent và truy vấn Firebase
        val maPhongTro = intent.getStringExtra("maPhongTro") ?: ""
        if (maPhongTro.isNotEmpty()) {
            // Truy vấn Firebase hoặc Firestore để lấy tên phòng
            firestore.collection("PhongTro").document(maPhongTro).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val tenPhong = document.getString("tenPhongTro") ?: "Chưa cập nhật"
                        editTenPhongTro.setText(tenPhong)

                        // Có thể thêm các dữ liệu khác nếu cần
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("ToCaoPhongTro", "Lỗi khi lấy tên phòng: ${exception.message}")
                }
        } else {
            Log.e("ToCaoPhongTro", "Không có maPhongTro trong Intent")
        }
        mDatabase = FirebaseDatabase.getInstance().getReference()

        // Nhận userId từ Intent và truy vấn Firebase
        val userId = intent.getStringExtra("idUser")
        if (userId != null) {
            Log.d("ToCaoPhongTro", "User ID: $userId")
            mDatabase.child("NguoiDung").child(userId).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val name = snapshot.child("hoTen").value.toString().trim()
                        editTenPhongTro.setText(name.ifEmpty { "Chưa cập nhật" })

                        // Lấy mã người dùng từ Firebase
                        maNguoiDung = snapshot.child("maNguoiDung").value.toString()

                        // Làm cho trường không thể chỉnh sửa
                        editTenPhongTro.isFocusable = false
                        editTenPhongTro.isFocusableInTouchMode = false
                        editTenPhongTro.isClickable = false
                    } else {
                        Log.e("ToCaoPhongTro", "Người dùng không tồn tại trong cơ sở dữ liệu.")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ToCaoPhongTro", "Lỗi khi lấy dữ liệu người dùng: ${error.message}")
                }
            })
        } else {
            Log.e("ToCaoPhongTro", "Không có userId trong Intent")
        }

        imgBackToCaoPhong.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val maNguoiDung = sharedPreferences.getString("maNguoiDung", null)



        // Lưu dữ liệu khi click btnToCaoPhong
        btnToCaoPhong.setOnClickListener {
            val tenPhongTro = editTenPhongTro.text.toString().trim()
            val vanDePhong = editVanDePhong.text.toString().trim()

            // Kiểm tra các trường không được để trống
            if (tenPhongTro.isEmpty()) {
                Toast.makeText(this, "Tên phòng trọ không được để trống", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (vanDePhong.isEmpty()) {
                Toast.makeText(this, "Vấn đề phòng trọ không được để trống", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (imageUriList.isEmpty()) {
                Toast.makeText(this, "Hãy chọn ít nhất một hình ảnh để tố cáo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Nếu tất cả các trường hợp lệ, tiến hành lưu dữ liệu
            val toCaoPhongData = hashMapOf(
                "tenPhongTro" to tenPhongTro,
                "vanDePhong" to vanDePhong,
                "images" to imageUriList.map { it.toString() }, // Lưu URL từ Firebase Storage
                "maNguoiBiToCao" to userId,
                "maPhongTro" to maPhongTro,
                "maNguoiToCao" to maNguoiDung, // Gửi mã người dùng vào Firestore
                "trangThai" to ""
            )

            firestore.collection("ToCaoPhongTro").add(toCaoPhongData).addOnSuccessListener {
                Toast.makeText(this, "Tố cáo phòng trọ được gửi thành công", Toast.LENGTH_SHORT).show()
                editTenPhongTro.text.clear()
                editVanDePhong.text.clear()
                imageUriList.clear()
                rcToCaoPhong.adapter?.notifyDataSetChanged()

                // Hiển thị lại nút imgToCaoPhong nếu cần
                imgToCaoPhong.visibility = ImageView.VISIBLE
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
                    rcToCaoPhong.adapter?.notifyDataSetChanged()

                    // Ẩn imgToCaoPhong sau khi tải lên thành công
                    imgToCaoPhong.visibility = ImageView.GONE

                    Toast.makeText(this, "Tải ảnh lên thành công!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Tải ảnh lên thất bại: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}