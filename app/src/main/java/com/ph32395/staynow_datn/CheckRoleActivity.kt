package com.ph32395.staynow_datn

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ph32395.staynow_datn.DangKiDangNhap.ChonLoaiTK

class CheckRoleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getUserRoleFromDatabase()
    }

    //    Ham lay vai tro nguoi dung tu Firebase Realtime Database
    private fun getUserRoleFromDatabase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val database = FirebaseDatabase.getInstance().getReference("NguoiDung").child(userId)
            database.get().addOnSuccessListener { snapshot ->
                val role = snapshot.child("loai_taikhoan").getValue(String::class.java)
                Log.d("MainActivity", "Vai tro nguoi dung tu Firebase: $role")
                if (role != null) {
//                    Kiem tra loai tai khoan de hien thi
                    when (role) {
                        "NguoiChoThue", "NguoiThue" -> {
                            navigateToMainActivity(role)
                        }
                        "ChuaChon" -> {
                            Log.d("MainActivity", "Dieu huong den man hinh Chọn Loai Tai Khoan")
//                            Dieu huong den man hinh chon loai tai khoan
                            val  intent = Intent(this, ChonLoaiTK::class.java)
                            startActivity(intent)
                            finish() //Ket thuc activity hien tai de ngan quay lai
                        }
                        else -> {
                            Log.e("MainActivity", "Vai trò không hợp lệ: $role")
                        }
                    }
                } else {
                    Log.e("MainActivity", "Không lấy được vai trò người dùng.")
                }
            }.addOnFailureListener {
                Log.e("MainActivity", "Lỗi khi lấy vai trò từ Firebase: ${it.message}")
            }
        } else {
            Log.e("MainActivity", "Không tìm thấy userId.")
        }
    }

    private fun navigateToMainActivity(userRole: String) {
        val intent = Intent(this@CheckRoleActivity, MainActivity::class.java)
        intent.putExtra("USER_ROLE", userRole)
        startActivity(intent)
        finish()
    }


}