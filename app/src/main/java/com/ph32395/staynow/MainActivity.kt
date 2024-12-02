package com.ph32395.staynow

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.ph32395.staynow.ChucNangTimKiem.SearchActivity
import com.ph32395.staynow.TaoPhongTro.TaoPhongTro
import com.ph32395.staynow.databinding.ActivityMainBinding
import com.ph32395.staynow.fragment.MessageFragment
import com.ph32395.staynow.fragment.ProfileFragment
import com.ph32395.staynow.fragment.RoomManagementFragment
import com.ph32395.staynow.fragment.home.HomeFragment
import com.ph32395.staynow.fragment.home_chu_tro.HomeNguoiChoThueFragment


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val homeFragment = HomeFragment()
    private val roomManagementFragment = RoomManagementFragment() //Man quan ly cua chu tro
    private val homeNguoiChoThueFragment = HomeNguoiChoThueFragment() //Nguoi cho thue
    private val messageFragment = MessageFragment()
    private val profileFragment = ProfileFragment()
    private var activeFragment: Fragment = homeFragment

    private val mDatabase = FirebaseDatabase.getInstance().reference
    private val currentUser = FirebaseAuth.getInstance().currentUser

    private val PREFS_NAME: String = "MyAppPrefs"
    private var userRole: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        })

        // Khởi tạo tất cả các Fragment và thêm HomeFragment làm mặc định
        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragment_container, profileFragment, "PROFILE").hide(profileFragment)
            add(R.id.fragment_container, messageFragment, "MESSAGE").hide(messageFragment)
//            add(R.id.fragment_container, notificationFragment, "NOTIFICATION").hide(notificationFragment)
            add(R.id.fragment_container, homeFragment, "HOME").hide(homeFragment)
        }.commit()

//        Nhan vai tro tu Intent
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        userRole = prefs.getString("check", "").toString()


//        Cap nhat giao dien theo vai tro
        updateUIForRole()

        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            Log.d("MainActivity", "Selected item: ${item.itemId}")
            when (item.itemId) {
                R.id.bottom_home -> {
                    showFragment(homeFragment)
                    true
                }

//                R.id.bottom_notification -> {
//                    showFragment(notificationFragment)
//                    true
//                }

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

    //    Ham cap nhat giao dien dua tren vai tro nguoi dung
    private fun updateUIForRole() {
        supportFragmentManager.beginTransaction().apply {
            // Nếu là NgườiChoThue
            if (userRole == "NguoiChoThue") {
                // Cập nhật BottomNavigationView cho NgườiChoThue
                binding.bottomNavigation.menu.clear()
                binding.bottomNavigation.inflateMenu(R.menu.bottom_menu_nguoi_chothue)

//                Cap nhat chuc nang FloatingActionButton
                binding.fabSearch.setImageResource(R.drawable.icon_add_room) //Thay doi Icon
                binding.fabSearch.setOnClickListener {
//                    chuyen sang man hinh them phong tro
                    startActivity(Intent(this@MainActivity, TaoPhongTro::class.java))
                }

                // Khởi tạo Fragment nếu chưa được thêm
                if (!roomManagementFragment.isAdded) {
                    add(R.id.fragment_container, roomManagementFragment, "ROOM_MANAGEMENT").hide(
                        roomManagementFragment
                    )
                }
                if (!homeNguoiChoThueFragment.isAdded) {
                    add(
                        R.id.fragment_container,
                        homeNguoiChoThueFragment,
                        "HOME_NGUOICHOTHUE"
                    ).hide(homeNguoiChoThueFragment)
                }

                // Hiển thị Fragment mặc định cho NguoiChoThue
                show(homeNguoiChoThueFragment)
                activeFragment = homeNguoiChoThueFragment

            }
            // Nếu là NgườiThue
            else if (userRole == "NguoiThue") {
                binding.bottomNavigation.menu.clear()
                binding.bottomNavigation.inflateMenu(R.menu.bottom_menu)

//                Cap nhat FAB search
                binding.fabSearch.setImageResource(R.drawable.icon_search_bottom)
                binding.fabSearch.setOnClickListener {
                    startActivity(Intent(this@MainActivity, SearchActivity::class.java))
                }

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


    override fun onStart() {
        super.onStart()
        setUserOnline()
    }

    override fun onStop() {
        super.onStop()
        setUserOffline()
    }

    override fun onPause() {
        super.onPause()
        setUserOffline()
    }

    override fun onDestroy() {
        super.onDestroy()
        setUserOffline()
    }


    private fun setUserOnline() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("NguoiDung").child(uid)
            userRef.child("status").setValue("online")
            userRef.child("lastActiveTime").setValue(ServerValue.TIMESTAMP)
        }
    }

    private fun setUserOffline() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("NguoiDung").child(uid)
            userRef.child("status").setValue("offline")
            userRef.child("lastActiveTime").setValue(ServerValue.TIMESTAMP)
        }
    }

    //nếu sủ dụng back của android thì phải kiểm tra xem có fragment nào trc đó không đã
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    fun setBottomNavigationVisibility(isVisible: Boolean) {
        val bottomNavigationView = binding.bottomNavigation
        val fabSearch = binding.fabSearch

        if (isVisible) {
            bottomNavigationView.visibility = View.VISIBLE
            fabSearch.visibility = View.VISIBLE // Hiện FAB khi cần
        } else {
            bottomNavigationView.visibility = View.GONE
            fabSearch.visibility = View.GONE // Ẩn FAB khi cần
        }
    }

}
