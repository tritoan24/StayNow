package com.ph32395.staynow.QuanLyPhongTro

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.ph32395.staynow.R
import com.ph32395.staynow.fragment.home.HomeViewModel

class QuanLyPhongTroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quan_ly_phong_tro)

        findViewById<ImageView>(R.id.imgBackQLPhong).setOnClickListener {
            finish() //Quay lai man hinh truoc
        }

        val viewPagerQLPhong: ViewPager2 = findViewById(R.id.viewPagerQLPhong)
        val tabLayoutQLPhong: TabLayout = findViewById(R.id.tabLayoutQLPhong)

//        Cai dat Adapter
        viewPagerQLPhong.adapter = ViewPagerQLPhongAdapter(this)

//        Ket noiTabLayout voi viewPager
        TabLayoutMediator(tabLayoutQLPhong, viewPagerQLPhong) { tab, position ->
            tab.text = when (position) {
                0 -> "Phòng đã đăng"
                1 -> "Đang lưu"
                2 -> "Chờ duyệt"
                3 -> "Đã bị hủy"
                4 -> "Đã cho thuê"
                else -> null
            }
        }.attach()

    }
}