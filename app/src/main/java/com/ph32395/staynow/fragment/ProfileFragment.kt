package com.ph32395.staynow.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.ph32395.staynow.BaoMat.CaiDat
import com.ph32395.staynow.BaoMat.CapNhatThongTin
import com.ph32395.staynow.BaoMat.ThongTinNguoiDung
import com.ph32395.staynow.DangKiDangNhap.DangNhap
import com.ph32395.staynow.R
import com.ph32395.staynow.hieunt.view.feature.manage_schedule_room.TenantManageScheduleRoomActivity
import com.ph32395.staynow.hieunt.widget.launchActivity
import com.ph32395.staynow.hieunt.widget.tap

class ProfileFragment : Fragment() {

    private lateinit var userNameTextView: TextView
    private lateinit var userPhoneTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var logoutButton: LinearLayout
    private lateinit var llScheduleRoom: LinearLayout
    private lateinit var nextDoiMK: LinearLayout
    private lateinit var nextUpdate: ImageButton
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private lateinit var prefs: SharedPreferences


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
        nextDoiMK = view.findViewById(R.id.next_caidat)
        nextUpdate = view.findViewById(R.id.next_UpdateInfor)
        llScheduleRoom = view.findViewById(R.id.ll_schedule_room)

        // Khởi tạo FirebaseAuth và DatabaseReference
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

        // Lấy UID của người dùng đã đăng nhập
        val userId = mAuth.currentUser?.uid
        Log.d("UID", "UID: $userId")

        // Lấy thông tin người dùng từ SharedPreferences

        // Lấy thông tin người dùng từ Firebase nếu có UID
        if (userId != null) {
            mDatabase.child("NguoiDung").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val name = snapshot.child("ho_ten").value.toString()
                        val phone = snapshot.child("sdt").value.toString()
                        val img = snapshot.child("anh_daidien").value.toString()

                        // gắn cho tôi vào textview
                        userNameTextView.text = name
                        userPhoneTextView.text = phone

                        // Tải ảnh đại diện bằng Glide
                        // Glide with null check to ensure fragment is attached to activity
                        if (isAdded) {
                            Glide.with(requireContext())
                                .load(img)
                                .circleCrop()
                                .placeholder(R.drawable.ic_user)
                                .into(profileImageView)
                        } else {
                            // Handle the case where fragment is not yet attached
                            profileImageView.setImageResource(R.drawable.ic_user)
                        }

                    } else {
                        Log.d("ProfileFragment", "Người dùng không tồn tại")
                    }
                    val accountType = snapshot.child("loai_taikhoan").getValue(String::class.java) ?: "NguoiThue" // Mặc định là "NguoiThue"
                    val registerLayout = view.findViewById<LinearLayout>(R.id.viewDK) // Thay ID cho đúng
                    if (registerLayout != null) {
                        if ("ChuNha".equals(accountType)) {
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
            setUserOffline()
            mAuth.signOut() // Đăng xuất Firebase
            // Lưu trạng thái đã đăng nhập vào SharedPreferences
            prefs = requireActivity().getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putBoolean("is_logged_in", false)
            editor.apply()
            startActivity(Intent(requireActivity(), DangNhap::class.java)) // Quay lại màn hình đăng nhập
            requireActivity().finish() // Kết thúc hoạt động hiện tại
            activity?.finish()
        }

        nextUpdate.setOnClickListener{
            startActivity(Intent(requireActivity(),ThongTinNguoiDung::class.java))
            requireActivity().finish()
        }
        nextDoiMK.setOnClickListener {
            startActivity(Intent(requireActivity(),CaiDat::class.java))
            requireActivity().finish()
        }

        llScheduleRoom.tap {
            launchActivity(TenantManageScheduleRoomActivity::class.java)
        }
        return view
    }


    private fun setUserOffline() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("NguoiDung").child(uid)
            userRef.child("status").setValue("offline").addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("MainActivity", "User status set to offline")
                } else {
                    Log.e("MainActivity", "Failed to set user status to offline: ${task.exception}")
                }
            }
            userRef.child("lastActiveTime").setValue(ServerValue.TIMESTAMP)
        }
    }

}