package com.ph32395.staynow.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
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
import com.ph32395.staynow.QuanLyPhongTro.QuanLyPhongTroActivity
import com.ph32395.staynow.QuanLyPhongTro.UpdateRoom.UpdateRoomActivity
import com.ph32395.staynow.QuanLyPhongTro.UpdateRoom.UpdateRoomModel
import com.ph32395.staynow.QuanLyPhongTro.custom.CustomConfirmationDialog
import com.ph32395.staynow.R
import com.ph32395.staynow.ViewModel.RoomDetailViewModel
import com.ph32395.staynow.fragment.home.HomeViewModel
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
            finish()
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
            addItemDecoration(SpacingItemDecoration(16))  // 16dp là khoảng cách giữa các item
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
            if(trangThaiDuyet == "DaDuyet" && trangThaiLuu == false && trangThaiPhong == false) {
                findViewById<CardView>(R.id.cardViewChucNangPhongDangDang).visibility = View.VISIBLE
                findViewById<CardView>(R.id.cardThongTinChuTro).visibility = View.GONE
            }
            else if(ManHome == "ManND") {
                findViewById<CardView>(R.id.cardViewChucNangPhongTrenHone).visibility = View.VISIBLE
            }else if(trangThaiLuu == true) {
                findViewById<CardView>(R.id.cardViewChucNangPhongDangLuu).visibility = View.VISIBLE
                findViewById<CardView>(R.id.cardThongTinChuTro).visibility = View.GONE
            }else if(trangThaiDuyet == "BiHuy" && trangThaiLuu == false && trangThaiPhong == false) {
                findViewById<CardView>(R.id.cardViewChucNangPhongDaBiHuy).visibility = View.VISIBLE
                findViewById<CardView>(R.id.cardThongTinChuTro).visibility = View.GONE
            }

//            Chuc nang Cap nhat thong tin phong
            findViewById<LinearLayout>(R.id.btnSuaPhong).setOnClickListener {
//                lay thong tin tu ViewModel chuyen doi sang UpdateRoomViewModel
                val updateRoomModel = UpdateRoomModel(
                    Ten_phongtro = viewModel.room.value?.Ten_phongtro ?: "",
                    Dia_chi = viewModel.room.value?.Dia_chi ?: "",
                    Loai_phong = viewModel.roomType.value ?: "",
                    Gioi_tinh = viewModel.genderInfo.value?.second ?: "",
                    Url_image = ArrayList(viewModel.room.value?.imageUrls ?: emptyList()),
                    Gia_phong = viewModel.room.value?.Gia_phong ?: 0.0,
                    Chi_tietthongtin = ArrayList(viewModel.chiTietList.value ?: emptyList()),
                    Dich_vu = ArrayList(viewModel.phiDichVuList.value ?: emptyList()),
                    Noi_that = ArrayList(viewModel.noiThatList.value ?: emptyList()),
                    Tien_nghi = ArrayList(viewModel.tienNghiList.value ?: emptyList()),
                    Chi_tietthem = viewModel.room.value?.Mota_chitiet ?: ""
                )
                //                    Truyen du lieu qua Intent
                val intent = Intent(this@RoomDetailActivity, UpdateRoomActivity::class.java)
                intent.putExtra("updateRoomModel", updateRoomModel)
                startActivity(intent)
            }

//            Chuc ang go phong chuyen sang man dang luu
            findViewById<LinearLayout>(R.id.btnGoPhong).setOnClickListener {
                val roomId = intent.getStringExtra("maPhongTro") ?: return@setOnClickListener
                val viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

//                Hien thi Dialog xacs nhan
                val dialog = CustomConfirmationDialog(
                    message = "Bạn có chắc chắn muốn gỡ phòng không?",
                    onConfirm = {
//                        Nguoi dung nhan xac nhan
                        viewModel.updateRoomStatus(roomId, "", true)
                        // Hiển thị thông báo
                        Toast.makeText(this, "Phòng trọ đã được gỡ!", Toast.LENGTH_SHORT).show()
                        // Chuyển đến Fragment "Phòng Đang Lưu"
                        val intent = Intent(this, QuanLyPhongTroActivity::class.java)
                        startActivity(intent)
                        finish() // Đóng màn hình hiện tại
                    },
                    onCancel = {

                    }
                )
                dialog.show(supportFragmentManager, "CustomConfirmationDialog")
            }

            //            Chuc ang go phong chuyen sang man da  dang
            findViewById<LinearLayout>(R.id.btnDangPhong).setOnClickListener {
                val roomId = intent.getStringExtra("maPhongTro") ?: return@setOnClickListener
                val viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

//                Hien thi Dialog xacs nhan
                val dialog = CustomConfirmationDialog(
                    message = "Bạn có chắc chắn muốn đăng phòng không?",
                    onConfirm = {
//                        Nguoi dung nhan xac nhan
                        viewModel.updateRoomStatus(roomId, "DaDuyet", false)
                        // Hiển thị thông báo
                        Toast.makeText(this, "Phòng trọ đã được đăng!", Toast.LENGTH_SHORT).show()
                        // Chuyển đến Fragment "Phòng Đang Lưu"
                        val intent = Intent(this, QuanLyPhongTroActivity::class.java)
                        startActivity(intent)
                        finish() // Đóng màn hình hiện tại
                    },
                    onCancel = {

                    }
                )
                dialog.show(supportFragmentManager, "CustomConfirmationDialog")
            }

            //            Chuc ang go phong chuyen sang man huy phong
            findViewById<LinearLayout>(R.id.btnHuyPhong).setOnClickListener {
                val roomId = intent.getStringExtra("maPhongTro") ?: return@setOnClickListener
                val viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

//                Hien thi Dialog xacs nhan
                val dialog = CustomConfirmationDialog(
                    message = "Bạn có chắc chắn muốn hủy phòng không?",
                    onConfirm = {
//                        Nguoi dung nhan xac nhan
                        viewModel.updateRoomStatusHuyPhong(roomId, "BiHuy")
                        // Hiển thị thông báo
                        Toast.makeText(this, "Đã hủy phòng trọ!", Toast.LENGTH_SHORT).show()
                        // Chuyển đến Fragment "Phòng Đang Lưu"
                        val intent = Intent(this, QuanLyPhongTroActivity::class.java)
                        startActivity(intent)
                        finish() // Đóng màn hình hiện tại
                    },
                    onCancel = {

                    }
                )
                dialog.show(supportFragmentManager, "CustomConfirmationDialog")
            }

            //            Chuc nang go phong chuyen sang man luu phong tu man bi huy
            findViewById<LinearLayout>(R.id.btnLuuPhong).setOnClickListener {
                val roomId = intent.getStringExtra("maPhongTro") ?: return@setOnClickListener
                val viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

//                Hien thi Dialog xacs nhan
                val dialog = CustomConfirmationDialog(
                    message = "Bạn có chắc chắn muốn lưu phòng không?",
                    onConfirm = {
//                        Nguoi dung nhan xac nhan
                        viewModel.updateRoomStatus(roomId, "", true)
                        // Hiển thị thông báo
                        Toast.makeText(this, "Phòng trọ đã được lưu!", Toast.LENGTH_SHORT).show()
                        // Chuyển đến Fragment "Phòng Đang Lưu"
                        val intent = Intent(this, QuanLyPhongTroActivity::class.java)
                        startActivity(intent)
                        finish() // Đóng màn hình hiện tại
                    },
                    onCancel = {

                    }
                )
                dialog.show(supportFragmentManager, "CustomConfirmationDialog")
            }

            //            Chuc nang go phong chuyen sang man bi huy tu man luu phong
            findViewById<LinearLayout>(R.id.btnHuyPhongLuu).setOnClickListener {
                val roomId = intent.getStringExtra("maPhongTro") ?: return@setOnClickListener
                val viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

//                Hien thi Dialog xacs nhan
                val dialog = CustomConfirmationDialog(
                    message = "Bạn có chắc chắn muốn hủy phòng không?",
                    onConfirm = {
//                        Nguoi dung nhan xac nhan
                        viewModel.updateRoomStatus(roomId, "BiHuy", false)
                        // Hiển thị thông báo
                        Toast.makeText(this, "Phòng trọ đã được hủy!", Toast.LENGTH_SHORT).show()
                        // Chuyển đến Fragment "Phòng Đang Lưu"
                        val intent = Intent(this, QuanLyPhongTroActivity::class.java)
                        startActivity(intent)
                        finish() // Đóng màn hình hiện tại
                    },
                    onCancel = {

                    }
                )
                dialog.show(supportFragmentManager, "CustomConfirmationDialog")
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