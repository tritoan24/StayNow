package com.ph32395.staynow_datn.QuanLyNhaTro

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.databinding.ActivityQuanLyNhaTroBinding

class QuanLyNhaTroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuanLyNhaTroBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val nhaTroRef = firestore.collection("NhaTro")
    private val TAG = "ZZZQuanLyNhaTroActivityZZZ"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityQuanLyNhaTroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idUser = FirebaseAuth.getInstance().currentUser?.uid

        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        binding.addNhaTro.setOnClickListener {
            val bottomSheetCreateAndUpdateNhaTro = BottomSheetCreateAndUpdateNhaTro(null)
            bottomSheetCreateAndUpdateNhaTro.show(
                this.supportFragmentManager,
                bottomSheetCreateAndUpdateNhaTro.tag
            )
        }


        //Lay danh sach nha tro
//        fetchNhaTro(idUser)

        val tabLayout = binding.tabLayoutQuanLyNhaTro
        val viewPager2 = binding.viewPagerQuanLyNhaTro

        //configTabLayout
        configTabLayoutViewPager(tabLayout, viewPager2)


    }

    @SuppressLint("SetTextI18n")
    private fun configTabLayoutViewPager(tabLayout: TabLayout, viewPager2: ViewPager2) {
        viewPager2.adapter = ViewPagerAdapterNhaTro(this)
        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            val tabTitles = arrayOf("Đang hoạt động", "Dừng hoạt động")
            val customTab = LayoutInflater.from(this).inflate(R.layout.custom_tab_layout, null)

//            Cai dat tieu de cho tung tabLayout
            val tabTitle = customTab.findViewById<TextView>(R.id.tabTitle)
            val tabCount = customTab.findViewById<TextView>(R.id.tabCount)

            tabTitle.text = tabTitles[position]
            tabCount.visibility = View.GONE

//            Gan layout tuy chinh vao Tab
            tab.customView = customTab

        }.attach()

    }

}