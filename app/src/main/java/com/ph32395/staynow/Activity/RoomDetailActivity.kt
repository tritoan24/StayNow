package com.ph32395.staynow.Activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.google.firebase.dynamiclinks.internal.FirebaseDynamicLinksImpl.createDynamicLink
import com.ph32395.staynow.Adapter.ChiTietThongTinAdapter
import com.ph32395.staynow.Adapter.ImagePagerAdapter
import com.ph32395.staynow.Adapter.ImageRecyclerViewAdapter
import com.ph32395.staynow.Adapter.NoiThatAdapter
import com.ph32395.staynow.Adapter.PhiDichVuAdapter
import com.ph32395.staynow.Adapter.SpacingItemDecoration
import com.ph32395.staynow.Adapter.TienNghiAdapter
import com.ph32395.staynow.BaoMat.ThongTinNguoiDung
import com.ph32395.staynow.CCCD.CCCD
import com.ph32395.staynow.CapNhatViTriPhong.CapNhatViTri
import com.ph32395.staynow.ChucNangChung.LoadingUtil
import com.ph32395.staynow.QuanLyPhongTro.QuanLyPhongTroActivity
import com.ph32395.staynow.QuanLyPhongTro.UpdateRoom.UpdateRoomActivity
import com.ph32395.staynow.QuanLyPhongTro.UpdateRoom.UpdateRoomModel
import com.ph32395.staynow.QuanLyPhongTro.custom.CustomConfirmationDialog
import com.ph32395.staynow.MainActivity
import com.ph32395.staynow.R
import com.ph32395.staynow.ViewModel.RoomDetailViewModel
import com.ph32395.staynow.fragment.home.HomeViewModel
import com.ph32395.staynow.fragment.showWarningDialog
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

    private lateinit var viewmodelHome:HomeViewModel

    //khai báo loading animation
    private lateinit var loadingUtil: LoadingUtil


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_detail)

        findViewById<ImageView>(R.id.iconBack).setOnClickListener {
            finish()
        }

//        Khởi tạo LoadingUtil
        loadingUtil = LoadingUtil(this)

//        Khoi tao viewModel
        viewModel = ViewModelProvider(this)[RoomDetailViewModel::class.java]

//
        viewmodelHome = ViewModelProvider(this)[HomeViewModel::class.java]

//        Nhan du lieu tu Intent
        val maPhongTro = intent.getStringExtra("maPhongTro") ?: ""
        ManHome = intent.getStringExtra("ManHome") ?: ""


        if (ManHome == "ManND") {
            viewmodelHome.incrementRoomViewCount(maPhongTro)
        }

        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener { pendingDynamicLinkData ->
                val deepLink = pendingDynamicLinkData?.link
                if (deepLink != null) {
                    val roomType = deepLink.getQueryParameter("roomType")
                    Log.e("RoomDetailActivity", "RoomType: $roomType")

                    if (roomType != null) {
                        viewModel.fetchChiTietThongTin(roomType)
                        viewModel.fetchPhiDichVu(roomType)
                        viewModel.fetchNoiThat(roomType)
                        viewModel.fetchTienNghi(roomType)
                        viewModel.fetchRoomDetail(roomType)

                        findViewById<ImageView>(R.id.iconBack).setOnClickListener {
                            startActivity(Intent(this@RoomDetailActivity, MainActivity::class.java))
                        }
                    }else {

                    }
                }else {
                    viewModel.fetchChiTietThongTin(maPhongTro)
                    viewModel.fetchPhiDichVu(maPhongTro)
                    viewModel.fetchNoiThat(maPhongTro)
                    viewModel.fetchTienNghi(maPhongTro)

                    // Tải dữ liệu từ Firebase
                    viewModel.fetchRoomDetail(maPhongTro)

                    findViewById<ImageView>(R.id.iconBack).setOnClickListener {
                        finish() // Quay lại màn hình trước
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("RoomDetailActivity", "Error retrieving dynamic link", e)
            }


        findViewById<LinearLayout>(R.id.ll_schedule_room).setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            // Lấy dữ liệu từ Firebase Realtime Database
            val database = FirebaseDatabase.getInstance().reference
            val userRef = database.child("NguoiDung").child(userId)
            userRef.get().addOnSuccessListener { snapshot ->
                val statusCCCD = snapshot.child("StatusCCCD").value as? Boolean ?: false
                val statusPTTT = snapshot.child("StatusPttt").value as? Boolean ?: false
                Log.d("RoomManagementFragment", "statusCCCD: $statusCCCD")
                Log.d("RoomManagementFragment", "StatusPttt: $statusPTTT")

                // Kiểm tra trạng thái CCCD và PTTT
                if (!statusCCCD) {
                    showWarningDialog(
                        context = this,
                        title = "Bạn chưa cập nhật CCCD",
                        content = "Hãy cập nhật CCCD để tiếp tục",
                        confirmAction = { navigateToUpdateCCCD() }
                    )
                } else {
                    launchActivity(
                        Bundle().apply {
                            putSerializable(ROOM_DETAIL, viewModel.room.value)
                            putString(ROOM_ID, maPhongTro)
                        },
                        ScheduleRoomActivity::class.java
                    )
                }
            }.addOnFailureListener { exception ->
                // Xử lý lỗi nếu có
                Log.e("RoomManagementFragment", "Error fetching user data", exception)
            }
        }
        findViewById<AppCompatButton>(R.id.viewInfor).setOnClickListener{
            val intent = Intent(this@RoomDetailActivity, ThongTinNguoiDung::class.java)
            viewModel.userId.observe(this) { (ma_NguoiDung, hoTen) ->
                intent.putExtra("idUser",ma_NguoiDung)
            }
            startActivity(intent)
            finish()
        }

        findViewById<ImageView>(R.id.shareRoom).setOnClickListener{
            viewModel.userId.observe(this) { (ma_NguoiDung, hoTen) ->
                intent.putExtra("idUser", ma_NguoiDung)

                // Tạo dynamic link
                val roomDetailLink = "https://staynowshare.page.link/roomDetail?roomType=${maPhongTro}"
                createDynamicLink(roomDetailLink) { dynamicLink ->
                    shareLink(dynamicLink)
                }
            }
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

    }
    private fun shareLink(dynamicLink: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain" // Định dạng chia sẻ là văn bản
            putExtra(Intent.EXTRA_TEXT, dynamicLink) // Thêm dynamic link vào Intent
        }

        // Hiển thị các ứng dụng có thể chia sẻ link
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ qua"))
    }

    private fun createDynamicLink(roomDetailLink: String, onComplete: (String) -> Unit) {
        val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse(roomDetailLink)) // Liên kết đích
            .setDomainUriPrefix("https://staynowshare.page.link") // Miền Dynamic Link
            .setAndroidParameters(
                DynamicLink.AndroidParameters.Builder("com.ph32395.staynow") // Tên gói Android
                    .setFallbackUrl(Uri.parse("https://fallback.url")) // URL dự phòng (tùy chọn)
                    .build()
            )
            .setSocialMetaTagParameters(
                DynamicLink.SocialMetaTagParameters.Builder()
                    .setTitle("Chi tiết phòng trọ") // Tùy chỉnh tiêu đề
                    .setDescription("Xem phòng trọ chi tiết tại StayNow")
                    .setImageUrl(Uri.parse("https://link.to/image.png")) // Ảnh minh họa (tùy chọn)
                    .build()
            )
            .buildDynamicLink()

        onComplete(dynamicLink.uri.toString())
    }

    //    Danh sacch thng tin chi tiet
    private fun setupRecyclerView() {
        findViewById<RecyclerView>(R.id.recyclerViewChiTietThongTin).apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = GridLayoutManager(context, 4)
            adapter = chiTietAdapter
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
            if (ManHome == "ManND") {
                findViewById<CardView>(R.id.cardViewChucNangPhongTrenHone).visibility = View.VISIBLE
            } else if (ManHome == "ManCT") {
                if(trangThaiDuyet == "DaDuyet" && trangThaiLuu == false && trangThaiPhong == false) {
                    findViewById<CardView>(R.id.cardViewChucNangPhongDangDang).visibility = View.VISIBLE
                }
                else if(trangThaiLuu == true) {
                    findViewById<CardView>(R.id.cardViewChucNangPhongDangLuu).visibility = View.VISIBLE
                }else if(trangThaiDuyet == "BiHuy" && trangThaiLuu == false && trangThaiPhong == false) {
                    findViewById<CardView>(R.id.cardViewChucNangPhongDaBiHuy).visibility = View.VISIBLE
                }
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
//                val dialog = CustomConfirmationDialog(
//                    message = "Bạn có chắc chắn muốn đăng phòng không?",
//                    onConfirm = {
////                        Nguoi dung nhan xac nhan
////                        viewModel.updateRoomStatus(roomId, "DaDuyet", false)
//
//                        // Hiển thị thông báo
//                        Toast.makeText(this, "Phòng trọ đã được đăng!", Toast.LENGTH_SHORT).show()
//                        // Chuyển đến Fragment "Phòng Đang Lưu"
//                        val intent = Intent(this, QuanLyPhongTroActivity::class.java)
//                        startActivity(intent)
//                        finish() // Đóng màn hình hiện tại
//                    },
//                    onCancel = {
//
//                    }
//                )
//                dialog.show(supportFragmentManager, "CustomConfirmationDialog")
                val intent = Intent(this, CapNhatViTri::class.java)
                intent.putExtra("check", "a")
                startActivity(intent)
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

        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                loadingUtil.show()
            }
            else{
                loadingUtil.hide()
            }
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
    private fun navigateToUpdateCCCD() {
        val intent = Intent(this, CCCD::class.java)
        startActivity(intent)
    }
}