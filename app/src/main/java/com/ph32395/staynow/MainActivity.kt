package com.ph32395.staynow

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ph32395.staynow.databinding.ActivityMainBinding
import com.ph32395.staynow.fragment.home.HomeFragment
import com.ph32395.staynow.fragment.MessageFragment
import com.ph32395.staynow.fragment.NotificationFragment
import com.ph32395.staynow.fragment.ProfileFragment
import com.ph32395.staynow.fragment.home.HomeTabFragment
import com.ph32395.staynow.fragment.home.OnTabSelectedListener

class MainActivity : AppCompatActivity(),OnTabSelectedListener{

    private lateinit var binding: ActivityMainBinding
    // Implement phương thức của interface OnTabSelectedListener
    override fun onTabSelected(loaiPhongTro: String) {
        Log.d("MainActivity", "Received tab selection: $loaiPhongTro")
        // Chuyển giao dữ liệu cho HomeTabFragment
        val homeTabFragment = supportFragmentManager.findFragmentByTag("HomeTabFragment") as? HomeTabFragment
        homeTabFragment?.onTabSelected(loaiPhongTro)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadFragment(HomeFragment())

        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.bottom_notification -> {
                    loadFragment(NotificationFragment())
                    true
                }
                R.id.bottom_message -> {
                    loadFragment(MessageFragment())
                    true
                }
                R.id.bottom_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }



}