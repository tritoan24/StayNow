package com.ph32395.staynow_datn.QuanLyPhongTro

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.ph32395.staynow_datn.MainActivity
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.fragment.home.HomeViewModel

class QuanLyPhongTroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quan_ly_phong_tro)

        findViewById<ImageView>(R.id.imgBackQLPhong).setOnClickListener {
            startActivity(Intent(this@QuanLyPhongTroActivity, MainActivity::class.java))
        }

        val viewPagerQLPhong: ViewPager2 = findViewById(R.id.viewPagerQLPhong)
        val tabLayoutQLPhong: TabLayout = findViewById(R.id.tabLayoutQLPhong)

//        Cai dat Adapter
        viewPagerQLPhong.adapter = ViewPagerQLPhongAdapter(this)

//        Ket noiTabLayout voi viewPager

        TabLayoutMediator(tabLayoutQLPhong, viewPagerQLPhong) {tab, position ->
            val tabTitles = arrayOf("Phòng đã đăng", "Đang lưu", "Chờ duyệt", "Đã bị hủy", "Đã cho thuê")
            val customTab = LayoutInflater.from(this).inflate(R.layout.custom_tab_layout, null)

//            Cai dat tieu de cho tung tabLayout
            val tabTitle = customTab.findViewById<TextView>(R.id.tabTitle)
            val tabCount = customTab.findViewById<TextView>(R.id.tabCount)

            tabTitle.text = tabTitles[position]
            tabCount.text = "(0)"

//            Gan layout tuy chinh vao Tab
            tab.customView = customTab
        }.attach()

//        Khoi tao ViewModel
        val viewModel: HomeViewModel by viewModels()
        viewModel.loadRoomByStatus(FirebaseAuth.getInstance().currentUser?.uid ?: "")

        updateTabCount(viewModel, tabLayoutQLPhong)

    }

    private fun updateTabCount(viewModel: HomeViewModel, tabLayout: TabLayout) {
        viewModel.phongDaDang.observe(this) {roomList ->
            val customTab = tabLayout.getTabAt(0)?.customView
            val tabCount = customTab?.findViewById<TextView>(R.id.tabCount)
            tabCount?.text = "(${roomList.size})"
        }

        viewModel.phongDangLuu.observe(this) {roomList ->
            val customTab = tabLayout.getTabAt(1)?.customView
            val tabCount = customTab?.findViewById<TextView>(R.id.tabCount)
            tabCount?.text = "(${roomList.size})"
        }

        viewModel.phongChoDuyet.observe(this) {roomList ->
            val customTab = tabLayout.getTabAt(2)?.customView
            val tabCount = customTab?.findViewById<TextView>(R.id.tabCount)
            tabCount?.text = "(${roomList.size})"
        }

        viewModel.phongDaHuy.observe(this) {roomList ->
            val customTab = tabLayout.getTabAt(3)?.customView
            val tabCount = customTab?.findViewById<TextView>(R.id.tabCount)
            tabCount?.text = "(${roomList.size})"
        }

        viewModel.phongDaChoThue.observe(this) {roomList ->
            val customTab = tabLayout.getTabAt(4)?.customView
            val tabCount = customTab?.findViewById<TextView>(R.id.tabCount)
            tabCount?.text = "(${roomList.size})"
        }
    }
}