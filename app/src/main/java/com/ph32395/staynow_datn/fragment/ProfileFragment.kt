package com.ph32395.staynow_datn.fragment

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.ph32395.staynow_datn.BaoMat.CaiDat
import com.ph32395.staynow_datn.BaoMat.PhanHoi
import com.ph32395.staynow_datn.BaoMat.ThongTinNguoiDung
import com.ph32395.staynow_datn.DangKiDangNhap.DangNhap
import com.ph32395.staynow_datn.MainActivity
import com.ph32395.staynow_datn.PhongTroDaXem.PhongTroDaXemActivity
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.fragment.contract_tenant.ContractFragment
import com.ph32395.staynow_datn.hieunt.database.db.AppDatabase
import com.ph32395.staynow_datn.hieunt.service.NotificationService
import com.ph32395.staynow_datn.hieunt.view.feature.manage_schedule_room.TenantManageScheduleRoomActivity
import com.ph32395.staynow_datn.hieunt.widget.launchActivity
import com.ph32395.staynow_datn.hieunt.widget.tap
import com.ph32395.staynow_datn.quanlyhoadon.BillManagementActivity

class ProfileFragment : Fragment() {

    private lateinit var userNameTextView: TextView
    private lateinit var userPhoneTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var logoutButton: LinearLayout
    private lateinit var llScheduleRoom: LinearLayout
    private lateinit var llContract: LinearLayout
    private lateinit var llBill: LinearLayout
    private lateinit var nextDoiMK: LinearLayout
    private lateinit var nextUpdate: CardView
    private lateinit var nextPhanhoi: LinearLayout
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private lateinit var btnPhongTroDaXem: LinearLayout
    private lateinit var btnBaiDangYeuThich: LinearLayout
    private lateinit var seperatedLichsu: View
    private lateinit var seperatedHoadon: View
    private lateinit var seperatedHopdong: View


    @SuppressLint("MissingInflatedId")
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
        nextPhanhoi = view.findViewById(R.id.phanhoiButton)
        llScheduleRoom = view.findViewById(R.id.ll_schedule_room)
        llContract = view.findViewById(R.id.ll_hopdong)
        llBill = view.findViewById(R.id.ll_hoadon)
        btnPhongTroDaXem = view.findViewById(R.id.btnPhongTroDaXem)
        seperatedLichsu = view.findViewById(R.id.viewlichsu)
        seperatedHoadon = view.findViewById(R.id.viewhoadon)
        seperatedHopdong = view.findViewById(R.id.viewhopdong)


        btnPhongTroDaXem.setOnClickListener {
            launchActivity(PhongTroDaXemActivity::class.java)
        }


        // Khởi tạo FirebaseAuth và DatabaseReference
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

        // Lấy UID của người dùng đã đăng nhập
        val userId = mAuth.currentUser?.uid
        Log.d("UID", "UID: $userId")

        // Lấy thông tin người dùng từ SharedPreferences

        // Lấy thông tin người dùng từ Firebase nếu có UID
        if (userId != null) {
            mDatabase.child("NguoiDung").child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {

                            val maNguoiDung = snapshot.child("maNguoiDung").value.toString()

                            // Lưu maNguoiDung vào SharedPreferences
                            val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("maNguoiDung", maNguoiDung)
                            editor.apply()


                            val name = snapshot.child("hoTen").value.toString()
                            val phone = snapshot.child("sdt").value.toString()
                            val img = snapshot.child("anhDaiDien").value.toString()

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
                        val accountType =
                            snapshot.child("loaiTaiKhoan").getValue(String::class.java)
                                ?: "NguoiThue" // Mặc định là "NguoiThue"
                        val registerLayout =
                            view.findViewById<LinearLayout>(R.id.viewDK) // Thay ID cho đúng
                        val scheduleRoom = view.findViewById<LinearLayout>(R.id.ll_schedule_room)
                        if (registerLayout != null) {
                            if ("NguoiChoThue" == accountType) {
                                registerLayout.visibility = View.GONE
                                scheduleRoom.visibility = View.GONE
                                llBill.visibility = View.GONE
                                llContract.visibility = View.GONE
                                seperatedLichsu.visibility = View.GONE
                                seperatedHoadon.visibility = View.GONE
                                seperatedHopdong.visibility = View.GONE

                            } else {
                                registerLayout.visibility = View.VISIBLE
                                scheduleRoom.visibility = View.VISIBLE
                            }
                        } else {
                            Log.e("ProfileFragment", "LinearLayout viewDK không tìm thấy.")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ProfileFragment", "Lỗi khi lấy dữ liệu người dùng: ${error.message}")
                    }
                })
        }


        // Xử lý sự kiện nhấn nút đăng xuất
        logoutButton.setOnClickListener {
            AppDatabase.getInstance(requireContext()).notificationDao().deleteAllNotification()
            setUserOffline() // Đánh dấu trạng thái offline nếu cần
            mAuth.signOut() // Đăng xuất Firebase
            requireActivity().stopService(Intent(requireContext(), NotificationService::class.java))
            // Đăng xuất tài khoản Google
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
            googleSignInClient.signOut().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Lưu trạng thái đã đăng nhập vào SharedPreferences
                    val prefs = requireActivity().getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                    prefs.edit().apply {
                        putBoolean("is_logged_in", false)
                        apply()
                    }

                    // Chuyển về màn hình đăng nhập
                    val intent = Intent(requireActivity(), DangNhap::class.java)
                    startActivity(intent)
                    requireActivity().finish() // Kết thúc hoạt động hiện tại
                } else {
                    // Xử lý lỗi nếu có (tuỳ chọn)
                    Log.e("LogoutError", "Google sign-out failed.")
                }
            }
        }


        nextUpdate.setOnClickListener {
            val intent = Intent(requireActivity(), ThongTinNguoiDung::class.java)
            intent.putExtra("idUser", FirebaseAuth.getInstance().currentUser?.uid)
            startActivity(intent)
        }
        nextDoiMK.setOnClickListener {
            startActivity(Intent(requireActivity(), CaiDat::class.java))
        }
        nextPhanhoi.setOnClickListener {
            startActivity(Intent(requireActivity(), PhanHoi::class.java))
        }

        llScheduleRoom.tap {
            launchActivity(TenantManageScheduleRoomActivity::class.java)
        }
        llContract.tap {
            replaceFragment(ContractFragment())
        }
        llBill.tap {
            launchActivity(BillManagementActivity::class.java)
        }
        return view
    }


    private fun setUserOffline() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("NguoiDung").child(uid)
            userRef.child("trangThai").setValue("offline").addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("MainActivity", "User status set to offline")
                } else {
                    Log.e("MainActivity", "Failed to set user status to offline: ${task.exception}")
                }
            }
            userRef.child("thoiGianKichHoatCuoiCung").setValue(ServerValue.TIMESTAMP)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        if (context is androidx.fragment.app.FragmentActivity) {
            val activity = context as androidx.fragment.app.FragmentActivity

            activity.supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    fragment
                ) // fragment_container là ID của ViewGroup chứa Fragment
                .addToBackStack(null) // Để quay lại màn hình trước
                .commit()

            // Ẩn Bottom Navigation
            if (activity is MainActivity) {
                activity.setBottomNavigationVisibility(false)
            }
        } else {
            Toast.makeText(context, "Không thể chuyển Fragment", Toast.LENGTH_SHORT).show()
        }
    }


}