package com.ph32395.staynow_datn.Activity


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
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
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow_datn.Adapter.ChiTietThongTinAdapter
import com.ph32395.staynow_datn.Adapter.ImagePagerAdapter
import com.ph32395.staynow_datn.Adapter.ImageRecyclerViewAdapter
import com.ph32395.staynow_datn.Adapter.NoiThatAdapter
import com.ph32395.staynow_datn.Adapter.PhiDichVuAdapter
import com.ph32395.staynow_datn.Adapter.SpacingItemDecoration
import com.ph32395.staynow_datn.Adapter.TienNghiAdapter
import com.ph32395.staynow_datn.BaoMat.ThongTinNguoiDung
import com.ph32395.staynow_datn.CCCD.CCCD
import com.ph32395.staynow_datn.ChucNangChung.LoadingUtil
import com.ph32395.staynow_datn.QuanLyPhongTro.QuanLyPhongTroActivity
import com.ph32395.staynow_datn.QuanLyPhongTro.custom.CustomConfirmationDialog
import com.ph32395.staynow_datn.MainActivity
import com.ph32395.staynow_datn.QuanLyPhongTro.UpdateRoom.UpdateRoomActivity
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.ViewModel.RoomDetailViewModel
import com.ph32395.staynow_datn.fragment.RoomManagementFragment
import com.ph32395.staynow_datn.fragment.home.HomeViewModel
import com.ph32395.staynow_datn.hieunt.helper.Default.IntentKeys.ROOM_DETAIL
import com.ph32395.staynow_datn.hieunt.helper.Default.IntentKeys.ROOM_ID
import com.ph32395.staynow_datn.hieunt.helper.SharePrefUtils
import com.ph32395.staynow_datn.hieunt.view.dialog.TenantInterestDialog
import com.ph32395.staynow_datn.hieunt.view.feature.schedule_room.ScheduleRoomActivity
import com.ph32395.staynow_datn.hieunt.widget.launchActivity

class RoomDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: RoomDetailViewModel
    private lateinit var viewPagerAdapter: ImagePagerAdapter
    private lateinit var recyclerViewAdapter: ImageRecyclerViewAdapter
    private lateinit var chiTietAdapter: ChiTietThongTinAdapter
    private lateinit var phiDichVuAdapter: PhiDichVuAdapter
    private lateinit var noiThatAdapter: NoiThatAdapter
    private lateinit var tienNghiAdapter: TienNghiAdapter
    private var ManHome = ""
    private var isFavorite = false
    private lateinit var roomId: String
    private lateinit var favoriteIcon: ImageView
    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var viewmodelHome:HomeViewModel

    //khai báo loading animation
    private lateinit var loadingUtil: LoadingUtil

    private var maPhongTro = "68dNr0UV4fMRtq9Fhgrt"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_detail)

        findViewById<ImageView>(R.id.iconBack).setOnClickListener {
            finish()
        }

//        roomId = intent.getStringExtra("maPhongTro") ?: ""
        roomId = "68dNr0UV4fMRtq9Fhgrt"
        favoriteIcon = findViewById(R.id.iconFavorite)

//        fetch trang thai yeu thich tu Firestore
        fetchFavoriteStatus()
//        Nhan vao icon yeu thich
        favoriteIcon.setOnClickListener {
            toggleFavoriteStatus()
        }

//        Khởi tạo LoadingUtil
        loadingUtil = LoadingUtil(this)

//        Khoi tao viewModel
        viewModel = ViewModelProvider(this)[RoomDetailViewModel::class.java]

//
        viewmodelHome = ViewModelProvider(this)[HomeViewModel::class.java]

//        Nhan du lieu tu Intent
//        maPhongTro = intent.getStringExtra("maPhongTro") ?: ""
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
                val statusCCCD = snapshot.child("statusCCCD").value as? Boolean ?: false
                val statusPTTT = snapshot.child("statusPTTT").value as? Boolean ?: false
                Log.d("RoomManagementFragment", "statusCCCD: $statusCCCD")
                Log.d("RoomManagementFragment", "StatusPttt: $statusPTTT")

                // Kiểm tra trạng thái CCCD và PTTT
                if (!statusCCCD) {
                    RoomManagementFragment().showWarningDialog(
                        context = this,
                        title = "Bạn chưa cập nhật CCCD",
                        content = "Hãy cập nhật CCCD để tiếp tục",
                        confirmAction = { navigateToUpdateCCCD() }
                    )
                } else {
                    if (!SharePrefUtils(this@RoomDetailActivity).isReadTenantInterest){
                        TenantInterestDialog{
                            launchActivity(
                                Bundle().apply {
                                    putSerializable(ROOM_DETAIL, viewModel.room.value)
                                    putString(ROOM_ID, maPhongTro)
                                },
                                ScheduleRoomActivity::class.java
                            )
                        }.show(supportFragmentManager, javaClass.name)
                    } else {
                        launchActivity(
                            Bundle().apply {
                                putSerializable(ROOM_DETAIL, viewModel.room.value)
                                putString(ROOM_ID, maPhongTro)
                            },
                            ScheduleRoomActivity::class.java
                        )
                    }

                }
            }.addOnFailureListener { exception ->
                // Xử lý lỗi nếu có
                Log.e("RoomManagementFragment", "Error fetching user data", exception)
            }
        }
        findViewById<MaterialButton>(R.id.viewInfor).setOnClickListener{
            val intent = Intent(this@RoomDetailActivity, ThongTinNguoiDung::class.java)
            viewModel.userId.observe(this) { (maNguoiDung, hoTen) ->
                intent.putExtra("idUser",maNguoiDung)
            }
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.shareRoom).setOnClickListener{
            viewModel.userId.observe(this) { (maNguoiDung, hoTen) ->
                intent.putExtra("idUser", maNguoiDung)

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

//    Chuyen doi trang thai yei thich
    private fun toggleFavoriteStatus() {
        val firestore = FirebaseFirestore.getInstance()
//    Lay id nguoi dung hien tai
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

//    Kiem tra phong tro da  trong danh sach phong tro yeu thich chua
    firestore.collection("PhongTroYeuThich")
        .document("$userId-$roomId")
        .get()
        .addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
//                Neu phong co trong ds phong yeu thich thi xoa no di
                firestore.collection("PhongTroYeuThich")
                    .document("$userId-$roomId")
                    .delete()
                    .addOnSuccessListener {
                        isFavorite = false
//                        Cap nhat lai icon trai tim
                        updateFavoriteIcon()
                    }
            } else {
//                Neu phong tro chua co trong danh sach phong yeu thich thi them vao
                val favoriteData = hashMapOf(
                    "idNguoiDung" to userId,
                    "idPhongTro" to roomId,
                    "thoiGianYeuThich" to System.currentTimeMillis()
                )

                firestore.collection("PhongTroYeuThich")
                    .document("$userId-$roomId")
                    .set(favoriteData)
                    .addOnSuccessListener {
                        isFavorite = true
                        updateFavoriteIcon()
                    }
            }
        }
    }

//    Kiem tra trang thai yeu thich
    private fun fetchFavoriteStatus() {
//        lay id nguoi dung hien tai
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()

//    Kiem tra cos ton tai document yeu thich ben trong khong
        firestore.collection("PhongTroYeuThich")
            .document("$userId-$roomId")
            .get()
            .addOnSuccessListener { documentSnapshot ->
//                Neu document to tai thi danh dau yeu thich
                isFavorite = documentSnapshot.exists()
                updateFavoriteIcon()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Loi khi tai trang thai yeu thich: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    //    Cap nhat lai icon khi thay doi trang thai yeu thich
    private fun updateFavoriteIcon() {
        favoriteIcon.setImageResource(if (isFavorite) R.drawable.icon_heart_red else R.drawable.icon_favorite)
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
//            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = GridLayoutManager(context, 3)
            adapter = chiTietAdapter
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
            findViewById<TextView>(R.id.txtTenPhongTro).text = room.tenPhongTro
            findViewById<TextView>(R.id.txtDiaChi).text = room.diaChi
            findViewById<TextView>(R.id.txtGiaThue).text =
                "${String.format("%,.0f", room.giaPhong)} VND"
            findViewById<TextView>(R.id.txtChiTietThem).text = room.moTaChiTiet

            val trangThaiDuyet = room.trangThaiDuyet
            val trangThaiLuu = room.trangThaiLuu
            val trangThaiPhong = room.trangThaiPhong

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
                    findViewById<CardView>(R.id.cardThongTinChuTro).visibility = View.GONE
                }
                else if(trangThaiLuu == true) {
                    findViewById<CardView>(R.id.cardViewChucNangPhongDangLuu).visibility = View.VISIBLE
                    findViewById<CardView>(R.id.cardThongTinChuTro).visibility = View.GONE
                }else if(trangThaiDuyet == "BiHuy" && trangThaiLuu == false && trangThaiPhong == false) {
                    findViewById<CardView>(R.id.cardViewChucNangPhongDaBiHuy).visibility = View.VISIBLE
                    findViewById<CardView>(R.id.cardThongTinChuTro).visibility = View.GONE
                } else if (trangThaiDuyet == "ChoDuyet") {
                    findViewById<CardView>(R.id.cardViewChucNangChoDuyet).visibility = View.VISIBLE
                    findViewById<CardView>(R.id.cardThongTinChuTro).visibility = View.GONE
                } else if (trangThaiPhong == true) {
                    findViewById<CardView>(R.id.cardThongTinChuTro).visibility = View.GONE
                }
                else if(ManHome == "sdk"){
                    findViewById<CardView>(R.id.cardViewChucNangPhongTrenHone).visibility = View.GONE

                }
            }



//            Chuc nang Cap nhat thong tin phong
            findViewById<LinearLayout>(R.id.btnSuaPhong).setOnClickListener {
//                lay thong tin tu ViewModel chuyen doi sang UpdateRoomViewModel
//                val updateRoomModel = UpdateRoomModel(
//                    Ten_phongtro = viewModel.room.value?.tenPhongTro ?: "",
//                    Dia_chi = viewModel.room.value?.diaChi ?: "",
//                    Loai_phong = viewModel.roomType.value ?: "",
//                    Gioi_tinh = viewModel.genderInfo.value?.second ?: "",
//                    Url_image = ArrayList(viewModel.room.value?.imageUrls ?: emptyList()),
//                    Gia_phong = viewModel.room.value?.giaPhong ?: 0.0,
//                    Chi_tietthongtin = ArrayList(viewModel.chiTietList.value ?: emptyList()),
//                    Dich_vu = ArrayList(viewModel.phiDichVuList.value ?: emptyList()),
//                    Noi_that = ArrayList(viewModel.noiThatList.value ?: emptyList()),
//                    Tien_nghi = ArrayList(viewModel.tienNghiList.value ?: emptyList()),
//                    Chi_tietthem = viewModel.room.value?.moTaChiTiet ?: ""
//                )
                //                    Truyen du lieu qua Intent
//                val intent = Intent(this@RoomDetailActivity, UpdateRoomActivity::class.java)
//                intent.putExtra("updateRoomModel", updateRoomModel)
//                startActivity(intent)
            }
            findViewById<LinearLayout>(R.id.btnSuaPhongLuu).setOnClickListener{
//                val intent = Intent(this@RoomDetailActivity, UpdateRoomActivity::class.java)
//                intent.putExtra("id_PhongTro", maPhongTro)
//                startActivity(intent)
//                val updateRoomModel = UpdateRoomModel(
//                    Ten_phongtro = viewModel.room.value?.tenPhongTro ?: "",
//                    Dia_chi = viewModel.room.value?.diaChi ?: "",
//                    Loai_phong = viewModel.roomType.value ?: "",
//                    Gioi_tinh = viewModel.genderInfo.value?.second ?: "",
//                    Url_image = ArrayList(viewModel.room.value?.imageUrls ?: emptyList()),
//                    Gia_phong = viewModel.room.value?.giaPhong ?: 0.0,
//                    Chi_tietthongtin = ArrayList(viewModel.chiTietList.value ?: emptyList()),
//                    Dich_vu = ArrayList(viewModel.phiDichVuList.value ?: emptyList()),
//                    Noi_that = ArrayList(viewModel.noiThatList.value ?: emptyList()),
//                    Tien_nghi = ArrayList(viewModel.tienNghiList.value ?: emptyList()),
//                    Chi_tietthem = viewModel.room.value?.moTaChiTiet ?: ""
//                )
                //                    Truyen du lieu qua Intent
//                val intent = Intent(this@RoomDetailActivity, UpdateRoomActivity::class.java)
//                intent.putExtra("updateRoomModel", updateRoomModel)
//                startActivity(intent)
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
//            Chuc nang cho duyet -> dang luu
            findViewById<LinearLayout>(R.id.btnXacNhanHuy).setOnClickListener {
                val roomId = intent.getStringExtra("maPhongTro") ?: return@setOnClickListener
                val viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

//                Hien thi Dialog xacs nhan
                val dialog = CustomConfirmationDialog(
                    message = "Bạn có chắc chắn muốn hủy chờ duyệt không?",
                    onConfirm = {
//                        Nguoi dung nhan xac nhan
                        viewModel.updateRoomStatus(roomId, "", true)
                        // Hiển thị thông báo
                        Toast.makeText(this, "Đã hủy chờ duyệt!", Toast.LENGTH_SHORT).show()
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
                //Hien thi Dialog xacs nhan
                val dialog = CustomConfirmationDialog(
                    message = "Bạn có chắc chắn muốn đăng phòng không?",
                    onConfirm = {
                        if ( maPhongTro!= null) {

                            val PhongTro = firestore.collection("PhongTro").document(maPhongTro!!)

                            PhongTro.update(mapOf(
                                "trangThaiLuu" to false,
                                "trangThaiDuyet" to "ChoDuyet"
                            )).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("Firestore", "Cập nhật thành công")
                                } else {
                                    Log.e("Firestore", "Lỗi khi cập nhật: ${task.exception?.message}")
                                }
                            }


                        }else {
                            Toast.makeText(this, "Không tìm thấy ID phòng trọ", Toast.LENGTH_SHORT).show()
                        }
                        // Hiển thị thông báo
                        Toast.makeText(this, "Đã đăng phòng trọ!", Toast.LENGTH_SHORT).show()
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