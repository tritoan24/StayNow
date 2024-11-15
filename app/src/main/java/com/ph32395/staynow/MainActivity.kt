package com.ph32395.staynow

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ph32395.staynow.ChucNangTimKiem.SearchActivity
import com.ph32395.staynow.databinding.ActivityMainBinding
import com.ph32395.staynow.fragment.home.HomeFragment
import com.ph32395.staynow.fragment.MessageFragment
import com.ph32395.staynow.fragment.NotificationFragment
import com.ph32395.staynow.fragment.ProfileFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val homeFragment = HomeFragment()
    private val notificationFragment = NotificationFragment()
    private val messageFragment = MessageFragment()
    private val profileFragment = ProfileFragment()
    private var activeFragment: Fragment = homeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo tất cả các Fragment và thêm HomeFragment làm mặc định
        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragment_container, profileFragment, "PROFILE").hide(profileFragment)
            add(R.id.fragment_container, messageFragment, "MESSAGE").hide(messageFragment)
            add(R.id.fragment_container, notificationFragment, "NOTIFICATION").hide(notificationFragment)
            add(R.id.fragment_container, homeFragment, "HOME")
        }.commit()

        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
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

                else -> false
            }
        }
        binding.fabSearch.setOnClickListener {
            startActivity(Intent(this,SearchActivity::class.java))
        }
    }

    private fun showFragment(fragment: Fragment) {
        if (fragment != activeFragment) {
            supportFragmentManager.beginTransaction().hide(activeFragment).show(fragment).commit()
            activeFragment = fragment
        }
    }
}
