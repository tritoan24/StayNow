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
import com.ph32395.staynow.fragment.home.PhongTroAdapter

class RoomDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: RoomDetailViewModel
    private lateinit var viewPagerAdapter: ImagePagerAdapter
    private lateinit var recyclerViewAdapter: ImageRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_detail)

//        Khoi tao viewModel
        viewModel = ViewModelProvider(this).get(RoomDetailViewModel::class.java)

//        Nhan du lieu tu Intent
        val maPhongTro = intent.getStringExtra("maPhongTro") ?: ""
        val tenPhongTro = intent.getStringExtra("tenPhongTro") ?: ""
        val giaThue = intent.getDoubleExtra("giaThue", 0.0)
        val diaChi = intent.getStringExtra("diaChi") ?: ""
        val dienTich = intent.getDoubleExtra("dienTich", 0.0)
        val tang = intent.getIntExtra("tang", 0)
        val soNguoi = intent.getIntExtra("soNguoi", 0)
        val tienCoc = intent.getDoubleExtra("tienCoc", 0.0)
        val motaChiTiet = intent.getStringExtra("motaChiTiet") ?: ""
        val danhSachAnh = intent.getStringArrayListExtra("danhSachAnh") ?: ArrayList()
        val gioiTinh = intent.getStringExtra("gioiTinh") ?: ""
        val trangThai = intent.getStringExtra("trangThai") ?: ""

//        Cap nhat thong tin ban dau
        viewModel.setInitialData(maPhongTro, tenPhongTro, giaThue, diaChi, dienTich, tang, soNguoi, tienCoc, motaChiTiet, ArrayList(danhSachAnh), gioiTinh, trangThai)

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
//                Cap nhat du lieu len View
                findViewById<TextView>(R.id.txtTenPhongTro).text = it.tenPhongTro
                findViewById<TextView>(R.id.txtTang).text = room.tang.toString()
                findViewById<TextView>(R.id.txtGiaThue).text = "${it.giaThue?.let { String.format("%,.0f", it) }} VND/ thang"
                findViewById<TextView>(R.id.txtDiaChi).text = it.diaChi
                findViewById<TextView>(R.id.txtDienTich).text = "${it.dienTich?.let { String.format("%.1f", it) }} m²"
                findViewById<TextView>(R.id.txtTienCoc).text = "${it.tienCoc?.let { String.format("%,.0f", it) }} VND"
                findViewById<TextView>(R.id.txtSoNguoi).text = room.soNguoi.toString()
                findViewById<TextView>(R.id.txtChiTietThem).text = room.motaChiTiet
                findViewById<TextView>(R.id.txtGioiTinh).text = room.gioiTinh
                findViewById<TextView>(R.id.txtTrangThai).text = room.trangThai
//                Cap nhat hinh anh
                recyclerViewAdapter.setImages(room.danhSachAnh)
                viewPagerAdapter.setImages(room.danhSachAnh)
            }
        })

//        Tai du lieu tu Firebase
        viewModel.fetchRoomDetail(maPhongTro)
    }
}