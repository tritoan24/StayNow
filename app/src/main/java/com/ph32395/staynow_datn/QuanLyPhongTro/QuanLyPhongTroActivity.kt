package com.ph32395.staynow_datn.QuanLyPhongTro

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow_datn.MainActivity
import com.ph32395.staynow_datn.QuanLyPhongTro.fragment.PhongDaDangFragment
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.fragment.home.HomeViewModel
import com.ph32395.staynow_datn.fragment.home.PhongTroAdapter


class QuanLyPhongTroActivity : AppCompatActivity() {
    private lateinit var viewModel: HomeViewModel
    private var maNhaTro: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quan_ly_phong_tro)

        // Lấy maNhaTro từ intent nếu đến từ danh sách tòa nhà
        maNhaTro = intent.getStringExtra("maNhaTro")

        caiDatGiaoDien()
        caiDatViewModel()
    }



    private fun caiDatGiaoDien() {
        // Nút quay lại
        findViewById<ImageView>(R.id.imgBackQLPhong).setOnClickListener {
            finish()
        }

        // Cài đặt ViewPager và TabLayout
        val viewPagerQLPhong: ViewPager2 = findViewById(R.id.viewPagerQLPhong)
        val tabLayoutQLPhong: TabLayout = findViewById(R.id.tabLayoutQLPhong)

        viewPagerQLPhong.adapter = ViewPagerQLPhongAdapter(this)

        // Cài đặt các tab
        TabLayoutMediator(tabLayoutQLPhong, viewPagerQLPhong) { tab, position ->
            val danhSachTab = arrayOf("Phòng đã đăng", "Đang lưu", "Chờ duyệt", "Đã bị hủy", "Đã cho thuê")
            val tabTuyChinh = LayoutInflater.from(this).inflate(R.layout.custom_tab_layout, null)

            val tieuDeTab = tabTuyChinh.findViewById<TextView>(R.id.tabTitle)
            val soLuongTab = tabTuyChinh.findViewById<TextView>(R.id.tabCount)

            tieuDeTab.text = danhSachTab[position]
            soLuongTab.text = "(0)"

            tab.customView = tabTuyChinh
        }.attach()
    }

    private fun caiDatViewModel() {
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (maNhaTro != null) {
            // Tải danh sách phòng của tòa nhà cụ thể
            viewModel.loadPhongTheoToaNha(userId, maNhaTro!!)
            // Cập nhật tiêu đề hiển thị tên tòa nhà
            capNhatTieuDeToaNha(userId, maNhaTro!!)
        } else {
            // Tải danh sách phòng đơn lẻ
            viewModel.loadPhongDonLe(userId)
        }

        // Cài đặt theo dõi số lượng tab
        caiDatTheoDoiSoLuongTab()
    }

    private fun capNhatTieuDeToaNha(userId: String, maNhaTro: String) {
        FirebaseFirestore.getInstance()
            .collection("NhaTro")
            .document(userId)
            .collection("DanhSachNhaTro")
            .document(maNhaTro)
            .get()
            .addOnSuccessListener { document ->
                document.getString("tenNhaTro")?.let { tenToaNha ->
                    findViewById<TextView>(R.id.txtTitleNhaTro)?.text = tenToaNha
                }
            }
    }

    private fun caiDatTheoDoiSoLuongTab() {
        val tabLayout: TabLayout = findViewById(R.id.tabLayoutQLPhong)

        // Theo dõi số lượng phòng trong mỗi trạng thái
        viewModel.phongDaDang.observe(this) { danhSachPhong ->
            capNhatSoLuongTab(tabLayout, 0, danhSachPhong.size)
        }
        viewModel.phongDangLuu.observe(this) { danhSachPhong ->
            capNhatSoLuongTab(tabLayout, 1, danhSachPhong.size)
        }
        viewModel.phongChoDuyet.observe(this) { danhSachPhong ->
            capNhatSoLuongTab(tabLayout, 2, danhSachPhong.size)
        }
        viewModel.phongDaHuy.observe(this) { danhSachPhong ->
            capNhatSoLuongTab(tabLayout, 3, danhSachPhong.size)
        }
        viewModel.phongDaChoThue.observe(this) { danhSachPhong ->
            capNhatSoLuongTab(tabLayout, 4, danhSachPhong.size)
        }
    }

    private fun capNhatSoLuongTab(tabLayout: TabLayout, viTri: Int, soLuong: Int) {
        val tabTuyChinh = tabLayout.getTabAt(viTri)?.customView
        val soLuongTab = tabTuyChinh?.findViewById<TextView>(R.id.tabCount)
        soLuongTab?.text = "($soLuong)"
    }
}
