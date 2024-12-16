package com.ph32395.staynow

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.messaging.FirebaseMessaging
import com.ph32395.staynow.ChucNangTimKiem.SearchActivity
import com.ph32395.staynow.PhongTroYeuThich.PhongTroYeuThichFragment
import com.ph32395.staynow.TaoPhongTro.TaoPhongTro
import com.ph32395.staynow.databinding.ActivityMainBinding
import com.ph32395.staynow.fragment.MessageFragment
import com.ph32395.staynow.fragment.ProfileFragment
import com.ph32395.staynow.fragment.RoomManagementFragment
import com.ph32395.staynow.fragment.home.HomeFragment
import com.ph32395.staynow.fragment.home_chu_tro.HomeNguoiChoThueFragment
import com.ph32395.staynow.hieunt.helper.Default.IntentKeys.OPEN_MANAGE_SCHEDULE_ROOM_BY_NOTIFICATION
import com.ph32395.staynow.hieunt.helper.SystemUtils
import com.ph32395.staynow.hieunt.service.NotificationService

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val homeFragment = HomeFragment()
    private val roomManagementFragment = RoomManagementFragment() //Man quan ly cua chu tro
    private val homeNguoiChoThueFragment = HomeNguoiChoThueFragment() //Nguoi cho thue
    private val messageFragment = MessageFragment()
    private val profileFragment = ProfileFragment()
    private val phongTroYeuThichFragment = PhongTroYeuThichFragment()
    private var activeFragment: Fragment = homeFragment

    private val mDatabase = FirebaseDatabase.getInstance().reference
    private val currentUser = FirebaseAuth.getInstance().currentUser

    private val PREFS_NAME: String = "MyAppPrefs"
    private var userRole: String = ""
    // Cong Add
    private lateinit var myApplication: MyApplication

    override fun onResume() {
        super.onResume()
        // Cong Add
        // Khi trở về màn chính, đảm bảo người dùng online
        myApplication.setOnlineStatus(true)
        if (!SystemUtils.isServiceRunning(this, NotificationService::class.java)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(this, NotificationService::class.java))
            } else {
                startService(Intent(this, NotificationService::class.java))
            }
        } else {
            Log.d("klklkl", "serviceIsRunning")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Cong Add
        myApplication = application as MyApplication
        myApplication.setOnlineStatus(true) // Đặt trạng thái online khi vào màn chính
        binding.bottomNavigation.itemIconTintList = null
        binding.bottomNavigation.itemActiveIndicatorColor = null
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        })
//        FirebaseMessaging.getInstance().getToken()
//            .addOnCompleteListener(
//                object : OnCompleteListener<String?> {
//                    override fun onComplete(task: Task<String?>) {
//                        if (!task.isSuccessful) {
//                            Log.w(
//                                ContentValues.TAG,
//                                "Fetching FCM registration token failed",
//                                task.exception
//                            )
//                            return
//                        }
//
//                        // Get new FCM registration token
//                        val token = task.result
//
//                        //lưu token này vào database
//                        if (currentUser != null) {
//
//                            Log.d("token", "tạo ra cái khỉ gió ở đây này ")
//                            mDatabase.child("NguoiDung").child(currentUser.getUid()).child("token")
//                                .setValue(token)
//
//
//                        }
//                        //nếu không có người dùng nào đăng nhập thì không lưu token
//
//                    }
//                })

        // Khởi tạo tất cả các Fragment và thêm HomeFragment làm mặc định
        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragment_container, profileFragment, "PROFILE").hide(profileFragment)
            add(R.id.fragment_container, messageFragment, "MESSAGE").hide(messageFragment)
            add(R.id.fragment_container, phongTroYeuThichFragment, "FAVORITE").hide(phongTroYeuThichFragment)
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

                R.id.bottom_notification -> {
                    showFragment(phongTroYeuThichFragment)
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

    //    Ham cap nhat giao dien dua tren vai tro nguoi dung
    private fun updateUIForRole() {
        supportFragmentManager.beginTransaction().apply {
            // Nếu là NgườiChoThue
            if (userRole == "NguoiChoThue") {
                // Cập nhật BottomNavigationView cho NgườiChoThue
                binding.bottomNavigation.menu.clear()
                binding.bottomNavigation.inflateMenu(R.menu.bottom_menu_nguoi_chothue)

//                Cap nhat chuc nang FloatingActionButton
                binding.fabSearch.setImageResource(R.drawable.add_room_2) //Thay doi Icon
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
                if (intent.getBooleanExtra(OPEN_MANAGE_SCHEDULE_ROOM_BY_NOTIFICATION,false)){
                    show(roomManagementFragment)
                    activeFragment = roomManagementFragment
                    binding.bottomNavigation.selectedItemId = R.id.bottom_management_room
                }else{
                    show(homeNguoiChoThueFragment)
                    activeFragment = homeNguoiChoThueFragment
                }

            }
            // Nếu là NgườiThue
            else if (userRole == "NguoiThue") {
                binding.bottomNavigation.menu.clear()
                binding.bottomNavigation.inflateMenu(R.menu.bottom_menu)

//                Cap nhat FAB search
                binding.fabSearch.setImageResource(R.drawable.search_svg)
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

//
    override fun onDestroy() {
        super.onDestroy()
        // Cong Add
        // Đặt trạng thái offline khi ứng dụng bị hủy
        myApplication.setOnlineStatus(false)
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
