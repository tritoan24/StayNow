package com.ph32395.staynow.fragment

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ph32395.staynow.DangKiDangNhap.DangNhap
import com.ph32395.staynow.R

class ProfileFragment : Fragment() {

    private lateinit var userNameTextView: TextView
    private lateinit var userPhoneTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var logoutButton: ImageButton
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Khởi tạo các thành phần giao diện
        userNameTextView = view.findViewById(R.id.user_name)
        userPhoneTextView = view.findViewById(R.id.user_phone)
        profileImageView = view.findViewById(R.id.profile_image)
        logoutButton = view.findViewById(R.id.LogoutButton)

        // Khởi tạo FirebaseAuth và DatabaseReference
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

        // Lấy UID của người dùng đã đăng nhập
        val userId = mAuth.currentUser?.uid
        Log.d("UID", "UID: $userId")

        // Lấy thông tin người dùng từ SharedPreferences
        val prefs = requireActivity().getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val accountType = prefs.getString("AccountType", "NguoiThue") // Mặc định là "NguoiThue"
        // Lấy thông tin người dùng từ Firebase nếu có UID
        if (userId != null) {
            mDatabase.child("NguoiDung").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val name = prefs.getString("Name", null)
                        val phone = prefs.getString("Phone", null)
                        val image = prefs.getString("Image", null)
                        Log.d("ProfileFragment", "Name: $name, Phone: $phone, Image: $image")

                        // Cập nhật giao diện với thông tin mới từ Firebase
                        userNameTextView.text = name ?: "Người dùng"
                        userPhoneTextView.text = phone ?: "Chưa có"

                        // Tải ảnh đại diện bằng Glide
                        if (!image.isNullOrEmpty()) {
                            Glide.with(this@ProfileFragment)
                                .load(image)
                                .circleCrop()
                                .placeholder(R.drawable.ic_user)
                                .into(profileImageView)
                        } else {
                            profileImageView.setImageResource(R.drawable.ic_user)
                        }
                    } else {
                        Log.d("ProfileFragment", "Người dùng không tồn tại")
                    }

                    val registerLayout = view.findViewById<LinearLayout>(R.id.viewDK) // Thay ID cho đúng
                    if (registerLayout != null) {
                        if ("Chủ nhà".equals(accountType, ignoreCase = true)) {
                            registerLayout.visibility = View.GONE
                        } else {
                            registerLayout.visibility = View.VISIBLE
                        }
                    } else {
                        Log.e("ProfileFragment", "LinearLayout viewDK không tìm thấy.");
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileFragment", "Lỗi khi lấy dữ liệu người dùng: ${error.message}")
                }
            })
        }

        // Xử lý sự kiện nhấn nút đăng xuất
        logoutButton.setOnClickListener {
            mAuth.signOut() // Đăng xuất Firebase
            startActivity(Intent(requireActivity(), DangNhap::class.java)) // Quay lại màn hình đăng nhập
            requireActivity().finish() // Kết thúc hoạt động hiện tại
        }

        return view
    }
}
