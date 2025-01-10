package com.ph32395.staynow_datn.DangKiDangNhap

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ph32395.staynow_datn.MainActivity
import com.ph32395.staynow_datn.databinding.ActivityChonLoaiTkBinding

class ChonLoaiTK : AppCompatActivity() {
    private lateinit var binding: ActivityChonLoaiTkBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Khởi tạo binding
        binding = ActivityChonLoaiTkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo Firebase Database và Auth
        val database = FirebaseDatabase.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = database.getReference("NguoiDung/$userId")

        // Xử lý khi nhấn nút "Chủ nhà trọ"
        binding.nguoichothueButton.setOnClickListener {
            userRef.child("loaiTaiKhoan").setValue("NguoiChoThue").addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Đã cập nhật loại tài khoản: Chủ nhà trọ")
                        // Chuyển sang màn home
                        val intent = Intent(this, DangNhap::class.java)
                        startActivity(intent)
                         finish()
                } else {
                    showToast("Lỗi cập nhật loại tài khoản!")
                }
            }
        }

        // Xử lý khi nhấn nút "Người thuê trọ"
        binding.nguoithueButton.setOnClickListener {
            userRef.child("loaiTaiKhoan").setValue("NguoiThue").addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Đã cập nhật loại tài khoản: Người thuê trọ")
                    val intent = Intent(this, DangNhap::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    showToast("Lỗi cập nhật loại tài khoản!")
                }
            }
        }
    }

    // Hàm hiển thị thông báo
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
