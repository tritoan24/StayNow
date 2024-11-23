package com.ph32395.staynow.Activity

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.ph32395.staynow.Adapter.ChiTietThongTinAdapter
import com.ph32395.staynow.Adapter.ImagePagerAdapter
import com.ph32395.staynow.Adapter.ImageRecyclerViewAdapter
import com.ph32395.staynow.Adapter.NoiThatAdapter
import com.ph32395.staynow.Adapter.PhiDichVuAdapter
import com.ph32395.staynow.Adapter.SpacingItemDecoration
import com.ph32395.staynow.Adapter.TienNghiAdapter
import com.ph32395.staynow.R
import com.ph32395.staynow.ViewModel.RoomDetailViewModel
import com.ph32395.staynow.hieunt.helper.Default.IntentKeys.ROOM_DETAIL
import com.ph32395.staynow.hieunt.helper.Default.IntentKeys.ROOM_ID
import com.ph32395.staynow.hieunt.view.feature.schedule_room.ScheduleRoomActivity
import com.ph32395.staynow.hieunt.widget.launchActivity

class RoomDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: RoomDetailViewModel
    private lateinit var viewPagerAdapter: ImagePagerAdapter
    private lateinit var recyclerViewAdapter: ImageRecyclerViewAdapter
    private lateinit var chiTietAdapter: ChiTietThongTinAdapter
    private lateinit var phiDichVuAdapter: PhiDichVuAdapter
    private lateinit var noiThatAdapter: NoiThatAdapter
    private lateinit var tienNghiAdapter: TienNghiAdapter
    private var ManHome = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_detail)

        findViewById<ImageView>(R.id.iconBack).setOnClickListener {
            finish() //Quay lai man hinh truoc
        }



//        Khoi tao viewModel
        viewModel = ViewModelProvider(this)[RoomDetailViewModel::class.java]

//        Nhan du lieu tu Intent
        val maPhongTro = intent.getStringExtra("maPhongTro") ?: ""
         ManHome = intent.getStringExtra("ManHome") ?: ""


        findViewById<LinearLayout>(R.id.ll_schedule_room).setOnClickListener {
            launchActivity(
                Bundle().apply {
                    putSerializable(ROOM_DETAIL, viewModel.room.value)
                    putString(ROOM_ID, maPhongTro)
                },
                ScheduleRoomActivity::class.java
            )
        }
//        khoi tao Adapter
        chiTietAdapter = ChiTietThongTinAdapter(emptyList())
        phiDichVuAdapter = PhiDichVuAdapter(emptyList())
        noiThatAdapter = NoiThatAdapter(emptyList())
        tienNghiAdapter = TienNghiAdapter(emptyList())

        setupRecyclerViewNoiThat()
        setupImage()
        observeViewModel()
        setupRecyclerView()
        setupListPhiDichVu()
        setupRecyViewTienNghi()

//        Lay du lieu chi tiet thong tin phong tro
        viewModel.fetchChiTietThongTin(maPhongTro)

        viewModel.fetchPhiDichVu(maPhongTro)
        viewModel.fetchNoiThat(maPhongTro)
        viewModel.fetchTienNghi(maPhongTro)


//        Tai du lieu tu Firebase
        viewModel.fetchRoomDetail(maPhongTro)
    }

    //    Danh sacch thng tin chi tiet
    private fun setupRecyclerView() {
        findViewById<RecyclerView>(R.id.recyclerViewChiTietThongTin).apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
//            layoutManager = GridLayoutManager(context, 4)
            adapter = chiTietAdapter

            // Thêm SpacingItemDecoration để tạo khoảng cách đều giữa các item
            addItemDecoration(SpacingItemDecoration(10))  // 16dp là khoảng cách giữa các item
        }
    }

    //    Danh sach tien nghi
    private fun setupRecyViewTienNghi() {
        findViewById<RecyclerView>(R.id.recyclerViewTienNghi).apply {
            layoutManager = GridLayoutManager(context, 4)
            adapter = tienNghiAdapter

            addItemDecoration(SpacingItemDecoration(4))
        }
    }

    //    Danh sach noi that
    private fun setupRecyclerViewNoiThat() {
        findViewById<RecyclerView>(R.id.recyclerViewNoiThat).apply {
            layoutManager = GridLayoutManager(context, 4)
            adapter = noiThatAdapter

            addItemDecoration(SpacingItemDecoration(16))
        }
    }

    //    danh sach phi dich vu
    private fun setupListPhiDichVu() {
        findViewById<RecyclerView>(R.id.recyclerViewPhiDichVu).apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = phiDichVuAdapter

//            addItemDecoration(SpacingItemDecoration(1))
        }
    }

    private fun setupImage() {
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
    }

    private fun observeViewModel() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBarDetail)


//        Quan sat chi tiet phong tro chinh
        viewModel.room.observe(this) { room ->
//            Cap nhat giao dien thong tin phong tro
            findViewById<TextView>(R.id.txtTenPhongTro).text = room.Ten_phongtro
            findViewById<TextView>(R.id.txtDiaChi).text = room.Dia_chi
            findViewById<TextView>(R.id.txtGiaThue).text =
                "${String.format("%,.0f", room.Gia_phong)} VND"
            findViewById<TextView>(R.id.txtChiTietThem).text = room.Mota_chitiet

            val trangThaiDuyet = room.Trang_thaiduyet
            val trangThaiLuu = room.Trang_thailuu
            val trangThaiPhong = room.Trang_thaiphong

            Log.d("RoomDetailActivity", "Trang thai duyet: $trangThaiDuyet")
            Log.d("RoomDetailActivity", "Trang thai luu: $trangThaiLuu")
            Log.d("RoomDetailActivity", "Trang thai phong: $trangThaiPhong")


            Log.d("RoomDetailActivity", "Home: $ManHome")
            //tritoan code dựa vào 3 trạng thái này để hiển thị botton của phòng trọ
            if(ManHome == "ManCT" && trangThaiDuyet == "DaDuyet" && trangThaiLuu == false && trangThaiPhong == false) {
                findViewById<CardView>(R.id.cardViewChucNangPhongDangDang).visibility = View.VISIBLE
            }else if(ManHome == "ManND") {
                findViewById<CardView>(R.id.cardViewChucNangPhongTrenHone).visibility = View.VISIBLE
            }else if(trangThaiLuu == true) {
                findViewById<CardView>(R.id.cardViewChucNangPhongDangLuu).visibility = View.VISIBLE
            }else if(trangThaiDuyet == "BiHuy" && trangThaiLuu == false && trangThaiPhong == false) {
                findViewById<CardView>(R.id.cardViewChucNangPhongDaBiHuy).visibility = View.VISIBLE
            }

//            Cap nhat hinh anh
            room.imageUrls?.let {
                viewPagerAdapter.setImages(it)
                recyclerViewAdapter.setImages(it)
            }
        }

        // Hiển thị ProgressBar khi bắt đầu tải dữ liệu
        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

//        Quan sat thong tin gioi tinh
        viewModel.genderInfo.observe(this) { (imgUrlGioiTinh, tenGioiTinh) ->
            findViewById<TextView>(R.id.txtGioiTinh).text = tenGioiTinh
            Glide.with(this).load(imgUrlGioiTinh).into(findViewById(R.id.img_gender))
        }

//        Quan sat thong tin loai phong
        viewModel.roomType.observe(this) { roomType ->
            findViewById<TextView>(R.id.txtLoaiPhong).text = roomType
        }

//        Quan sat thong tin nguoi dung
        viewModel.userInfo.observe(this) { (anhDaiDien, hoTen) ->
            findViewById<TextView>(R.id.txtTenChuTro).text = hoTen
            Glide.with(this).load(anhDaiDien).into(findViewById(R.id.imgAvatarChuTro))
        }

//        Quan sat chi tiet thong tin
        viewModel.chiTietList.observe(this) { chiTietList ->
            chiTietAdapter = ChiTietThongTinAdapter(chiTietList)
            findViewById<RecyclerView>(R.id.recyclerViewChiTietThongTin).adapter = chiTietAdapter
        }

//        Quan sat du lieu phi dich vu
        viewModel.phiDichVuList.observe(this) { phiDichVuList ->
            phiDichVuAdapter = PhiDichVuAdapter(phiDichVuList)
            findViewById<RecyclerView>(R.id.recyclerViewPhiDichVu).adapter = phiDichVuAdapter
        }

//        Quan sat du lieu noi that
        viewModel.noiThatList.observe(this) { noiThatList ->
            noiThatAdapter = NoiThatAdapter(noiThatList)
            findViewById<RecyclerView>(R.id.recyclerViewNoiThat).adapter = noiThatAdapter
        }

//        Quan sat du lieu tien nghi
        viewModel.tienNghiList.observe(this) { tienNghiList ->
            tienNghiAdapter = TienNghiAdapter(tienNghiList)
            findViewById<RecyclerView>(R.id.recyclerViewTienNghi).adapter = tienNghiAdapter
        }

    }
}