package com.ph32395.staynow

import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ph32395.staynow.databinding.ActivityMainBinding
import com.ph32395.staynow.fragment.HomeNguoiChoThueFragment
import com.ph32395.staynow.fragment.home.HomeFragment
import com.ph32395.staynow.fragment.MessageFragment
import com.ph32395.staynow.fragment.NotificationFragment
import com.ph32395.staynow.fragment.ProfileFragment
import com.ph32395.staynow.fragment.RoomManagementFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val homeFragment = HomeFragment()
    private val notificationFragment = NotificationFragment()
    private val roomManagementFragment = RoomManagementFragment() //Man quan ly cua chu tro
    private val homeNguoiChoThueFragment = HomeNguoiChoThueFragment() //Nguoi cho thue
    private val messageFragment = MessageFragment()
    private val profileFragment = ProfileFragment()
    private var activeFragment: Fragment = homeFragment

    private lateinit var userRole: String //Luu vai tro nguoi dung

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this,object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        })

        // Khởi tạo tất cả các Fragment và thêm HomeFragment làm mặc định
        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragment_container, profileFragment, "PROFILE").hide(profileFragment)
            add(R.id.fragment_container, messageFragment, "MESSAGE").hide(messageFragment)
            add(R.id.fragment_container, notificationFragment, "NOTIFICATION").hide(notificationFragment)
            add(R.id.fragment_container, roomManagementFragment, "ROOM_MANAGEMENT").hide(roomManagementFragment)
            add(R.id.fragment_container, homeNguoiChoThueFragment, "HOME_NGUOICHOTHUE").hide(homeNguoiChoThueFragment)
            add(R.id.fragment_container, homeFragment, "HOME").hide(homeFragment)
        }.commit()

        //        lay vai tro nguoi dung tu Realtime Database
        getUserRoleFromDatabase()

        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            Log.d("MainActivity", "Selected item: ${item.itemId}")
            when (item.itemId) {
                R.id.bottom_home -> {
                    showFragment(homeFragment)
                    true
                }

                R.id.bottom_notification -> {
                    showFragment(notificationFragment)
                    true
                }

                R.id.bottom_message -> {
                    showFragment(messageFragment)
                    true
                }

                R.id.bottom_profile -> {
                    showFragment(profileFragment)
                    true
                }


                R.id.bottom_management_room -> {
                    showFragment(roomManagementFragment)
                    true
                }

                R.id.bottom_home_nguoichothue -> {
                    showFragment(homeNguoiChoThueFragment)
                    true
                }

                else -> false
            }
        }
    }


//    Ham lay vai tro nguoi dung tu Firebase Realtime Database
    private fun getUserRoleFromDatabase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val database = FirebaseDatabase.getInstance().getReference("NguoiDung").child(userId)
            database.get().addOnSuccessListener { snapshot ->
                val role = snapshot.child("loai_taikhoan").getValue(String::class.java)
                if (role != null) {
                    userRole = role
                    // Cập nhật giao diện sau khi lấy vai trò
                    updateUIForRole()
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

//    Ham cap nhat giao dien dua tren vai tro nguoi dung
    private fun updateUIForRole() {
        supportFragmentManager.beginTransaction().apply {
            // Nếu là NgườiChoThue
            if (userRole == "NguoiChoThue") {
                // Cập nhật BottomNavigationView cho NgườiChoThue
                binding.bottomNavigation.menu.clear()
                binding.bottomNavigation.inflateMenu(R.menu.bottom_menu_nguoi_chothue)

                // Khởi tạo Fragment nếu chưa được thêm
                if (!roomManagementFragment.isAdded) {
                    add(R.id.fragment_container, roomManagementFragment, "ROOM_MANAGEMENT").hide(roomManagementFragment)
                }
                if (!homeNguoiChoThueFragment.isAdded) {
                    add(R.id.fragment_container, homeNguoiChoThueFragment, "HOME_NGUOICHOTHUE").hide(homeNguoiChoThueFragment)
                }

                // Ẩn toàn bộ các Fragment
                hide(homeFragment)
                hide(notificationFragment)
                hide(messageFragment)
                hide(profileFragment)
                hide(roomManagementFragment)

                // Hiển thị Fragment mặc định cho NguoiChoThue
                show(homeNguoiChoThueFragment)
                activeFragment = homeNguoiChoThueFragment
            }
            // Nếu là NgườiThue
            else if (userRole == "NguoiThue") {
                binding.bottomNavigation.menu.clear()
                binding.bottomNavigation.inflateMenu(R.menu.bottom_menu)

                // Ẩn toàn bộ các Fragment
                hide(homeNguoiChoThueFragment)
                hide(roomManagementFragment)

                // Hiển thị Fragment mặc định cho NguoiThue
                show(homeFragment)
                activeFragment = homeFragment
            }
        }.commitAllowingStateLoss() // Sử dụng commitAllowingStateLoss để tránh lỗi trạng thái
    }

    private fun showFragment(fragment: Fragment) {
        if (fragment != activeFragment) {
            supportFragmentManager.beginTransaction().apply {
                hide(activeFragment) // Ẩn Fragment hiện tại
                show(fragment)       // Hiển thị Fragment mới
            }.commit()
            activeFragment = fragment
        }
    }

}
