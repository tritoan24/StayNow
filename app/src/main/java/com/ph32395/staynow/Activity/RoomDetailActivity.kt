package com.ph32395.staynow.Activity

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.ph32395.staynow.Adapter.ImagePagerAdapter
import com.ph32395.staynow.Adapter.ImageRecyclerViewAdapter
import com.ph32395.staynow.R
import com.ph32395.staynow.ViewModel.RoomDetailViewModel

class RoomDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: RoomDetailViewModel
    private lateinit var viewPagerAdapter: ImagePagerAdapter
    private lateinit var recyclerViewAdapter: ImageRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_detail)

//        Khoi tao viewModel
        viewModel = ViewModelProvider(this).get(RoomDetailViewModel::class.java)

//        Thiet lap viewPager va RecyclerView cho anh
        val viewPager = findViewById<ViewPager>(R.id.viewPager)
        viewPagerAdapter = ImagePagerAdapter(viewPager)
        findViewById<ViewPager>(R.id.viewPager).adapter = viewPagerAdapter

        recyclerViewAdapter = ImageRecyclerViewAdapter { imageUrl ->
//            Su kien khi nhan anh nho tren recyclerView se hien thi len giao dien
            viewPagerAdapter.setCurrentImage(imageUrl)
        }

        findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = recyclerViewAdapter
        }

        // Quan sát dữ liệu từ ViewModel
        viewModel.room.observe(this, Observer { room ->
            room?.let {
                // Cập nhật UI với dữ liệu phòng trọ
                findViewById<TextView>(R.id.txtLoaiPhong).text = room.Loai_phongtro
                findViewById<TextView>(R.id.txtGioiTinh).text = room.Ma_gioitinh
                findViewById<TextView>(R.id.txtTenPhongTro).text = room.Ten_phongtro
                findViewById<TextView>(R.id.txtGiaThue).text = "${room.Gia_thue} VND/ tháng"
                findViewById<TextView>(R.id.txtDiaChi).text = room.Dia_chi
                findViewById<TextView>(R.id.txtTrangThai).text = room.Trang_thai
                findViewById<TextView>(R.id.txtTang).text = room.Tang.toString()
                findViewById<TextView>(R.id.txtSoNguoi).text = room.So_nguoi.toString()
                findViewById<TextView>(R.id.txtDienTich).text = room.Dien_tich
                findViewById<TextView>(R.id.txtTienCoc).text = "${room.Tien_coc} VND"
                findViewById<TextView>(R.id.txtChiTietThem).text = room.Chi_tietthem
                findViewById<TextView>(R.id.txtDanhGia).text = "${room.Danh_gia}/5"

                // Cập nhật adapter với danh sách ảnh
                viewPagerAdapter.setImages(room.Danh_sachanh)
                recyclerViewAdapter.setImages(room.Danh_sachanh)
            }
        })
    }
}