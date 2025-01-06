package com.ph32395.staynow_datn.BaoMat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.ph32395.staynow_datn.Adapter.ToCaoTaiKhoanAdapter
import com.ph32395.staynow_datn.R

class ToCaoTaiKhoan : AppCompatActivity() {

    private lateinit var imgToCao: ImageView
    private lateinit var rcToCao: RecyclerView
    private lateinit var editTenPhong: EditText
    private lateinit var editTenChuTro: EditText
    private lateinit var editVanDe: EditText
    private lateinit var btnToCao: Button

    private val imageUriList = mutableListOf<Uri>()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_to_cao_tai_khoan)

        // Ánh xạ View
        imgToCao = findViewById(R.id.tocao_avatar)
        rcToCao = findViewById(R.id.RcToCao)
        editTenPhong = findViewById(R.id.tocao_Tenphong)
        editTenChuTro = findViewById(R.id.tocao_Tennguoi)
        editVanDe = findViewById(R.id.tocao_Vande)
        btnToCao = findViewById(R.id.btnToCao)

        // Setup RecyclerView
        rcToCao.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val adapter = ToCaoTaiKhoanAdapter(imageUriList)
        rcToCao.adapter = adapter

        // Mở thư viện khi click imgToCao
        imgToCao.setOnClickListener {
            selectImageFromGallery()
        }

        // Nhận userId từ Intent và truy vấn Firestore
        val userId = intent.getStringExtra("idUser")
        userId?.let {
            firestore.collection("NguoiDung").document(it).get().addOnSuccessListener { document ->
                val user = document.toObject<User>()
                user?.let { editTenChuTro.setText(it.hoTen) }
            }
        }

        // Lưu dữ liệu khi click btnToCao
        btnToCao.setOnClickListener {
            val toCaoData = hashMapOf(
                "tenPhong" to editTenPhong.text.toString(),
                "tenChuTro" to editTenChuTro.text.toString(),
                "vanDe" to editVanDe.text.toString(),
                "images" to imageUriList.map { it.toString() }
            )
            firestore.collection("ToCaoTaiKhoan").add(toCaoData).addOnSuccessListener {
                Toast.makeText(this, "Tố cáo được gửi thành công", Toast.LENGTH_SHORT).show()

                // Xóa dữ liệu trên các ô nhập liệu
                editTenPhong.text.clear()
                editTenChuTro.text.clear()
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
                            uri?.let { imageUriList.add(it) }
                        }
                    } else {
                        // Nếu chỉ chọn một ảnh
                        val uri = data.data
                        uri?.let { imageUriList.add(it) }
                    }
                    rcToCao.adapter?.notifyDataSetChanged()
                    imgToCao.visibility = ImageView.GONE
                }
            }
        }


    data class User(val hoTen: String? = null)
}
