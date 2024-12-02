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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
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
import com.ph32395.staynow.MainActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_detail)



        // Khởi tạo viewModel
        viewModel = ViewModelProvider(this)[RoomDetailViewModel::class.java]

        // Nhận dữ liệu từ Intent
        val maPhongTro = intent.getStringExtra("maPhongTro") ?: ""

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
            launchActivity(
                Bundle().apply {
                    putSerializable(ROOM_DETAIL, viewModel.room.value)
                    putString(ROOM_ID, maPhongTro)
                },
                ScheduleRoomActivity::class.java
            )
        }


        // Nút chi tiết
        findViewById<AppCompatButton>(R.id.viewInfor).setOnClickListener {
            val intent = Intent(this@RoomDetailActivity, ThongTinNguoiDung::class.java)

            // Quan sát userId để lấy thông tin người dùng
            viewModel.userId.observe(this) { (ma_NguoiDung, hoTen) ->
                intent.putExtra("idUser", ma_NguoiDung)
                startActivity(intent)
            }

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


        // Khởi tạo Adapter
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

        // Lấy dữ liệu chi tiết thông tin phòng trọ


        // Xử lý liên kết động
    }

    private fun shareLink(dynamicLink: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain" // Định dạng chia sẻ là văn bản
            putExtra(Intent.EXTRA_TEXT, dynamicLink) // Thêm dynamic link vào Intent
        }

        // Hiển thị các ứng dụng có thể chia sẻ link
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ qua"))
    }

    private fun createDynamicLink(roomDetailLink: String, callback: (String) -> Unit) {
        val dynamicLink = FirebaseDynamicLinks.getInstance()
            .createDynamicLink()
            .setLink(Uri.parse(roomDetailLink))
            .setDomainUriPrefix("https://staynowshare.page.link") // Đảm bảo sử dụng domain của bạn
            .setAndroidParameters(
                DynamicLink.AndroidParameters.Builder("com.ph32395.staynow")
                    .build()
            )
            .buildDynamicLink()

        val dynamicLinkUri = dynamicLink.uri.toString()

        callback(dynamicLinkUri)
    }
    private fun showDynamicLinkDialog(dynamicLink: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Dynamic Link")
        builder.setMessage("Here is your Dynamic Link: $dynamicLink")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        builder.setNegativeButton("Copy Link") { dialog, _ ->
            // Copy dynamic link to clipboard
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Dynamic Link", dynamicLink)
            clipboard.setPrimaryClip(clip)
            dialog.dismiss()
        }
        builder.create().show()
    }



    // Danh sách thông tin chi tiết
    private fun setupRecyclerView() {
        findViewById<RecyclerView>(R.id.recyclerViewChiTietThongTin).apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = chiTietAdapter
            addItemDecoration(SpacingItemDecoration(20))  // 20dp là khoảng cách giữa các item
        }
    }

    // Danh sách tiện nghi
    private fun setupRecyViewTienNghi() {
        findViewById<RecyclerView>(R.id.recyclerViewTienNghi).apply {
            layoutManager = GridLayoutManager(context, 4)
            adapter = tienNghiAdapter
            addItemDecoration(SpacingItemDecoration(4))
        }
    }

    // Danh sách nội thất
    private fun setupRecyclerViewNoiThat() {
        findViewById<RecyclerView>(R.id.recyclerViewNoiThat).apply {
            layoutManager = GridLayoutManager(context, 4)
            adapter = noiThatAdapter
            addItemDecoration(SpacingItemDecoration(16))
        }
    }

    // Danh sách phí dịch vụ
    private fun setupListPhiDichVu() {
        findViewById<RecyclerView>(R.id.recyclerViewPhiDichVu).apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = phiDichVuAdapter
            addItemDecoration(SpacingItemDecoration(6))
        }
    }

    private fun setupImage() {
        // Thiết lập ViewPager và RecyclerView cho ảnh
        val viewPager = findViewById<ViewPager>(R.id.viewPager)
        viewPagerAdapter = ImagePagerAdapter(viewPager)
        viewPager.adapter = viewPagerAdapter

        recyclerViewAdapter = ImageRecyclerViewAdapter { imageUrl ->
            // Sự kiện khi nhấn ảnh nhỏ trên RecyclerView sẽ hiển thị lên giao diện
            viewPagerAdapter.setCurrentImage(imageUrl)
        }

        findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = recyclerViewAdapter
        }
    }

    private fun observeViewModel() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBarDetail)

        // Quan sát chi tiết phòng trọ chính
        viewModel.room.observe(this) { room ->
            // Cập nhật giao diện thông tin phòng trọ
            findViewById<TextView>(R.id.txtTenPhongTro).text = room.Ten_phongtro
            findViewById<TextView>(R.id.txtDiaChi).text = room.Dia_chi
            findViewById<TextView>(R.id.txtGiaThue).text =
                "${String.format("%,.0f", room.Gia_phong)} VND"
            findViewById<TextView>(R.id.txtChiTietThem).text = room.Mota_chitiet

            // Cập nhật hình ảnh
            room.imageUrls?.let {
                viewPagerAdapter.setImages(it)
                recyclerViewAdapter.setImages(it)
            }
        }

        // Hiển thị ProgressBar khi bắt đầu tải dữ liệu
        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Quan sát thông tin giới tính
        viewModel.genderInfo.observe(this) { (imgUrlGioiTinh, tenGioiTinh) ->
            findViewById<TextView>(R.id.txtGioiTinh).text = tenGioiTinh
            Glide.with(this).load(imgUrlGioiTinh).into(findViewById(R.id.img_gender))
        }

        // Quan sát thông tin loại phòng
        viewModel.roomType.observe(this) { roomType ->
            findViewById<TextView>(R.id.txtLoaiPhong).text = roomType
        }

        // Quan sát thông tin người dùng
        viewModel.userInfo.observe(this) { (anhDaiDien, hoTen) ->
            findViewById<TextView>(R.id.txtTenChuTro).text = hoTen
            Glide.with(this).load(anhDaiDien).into(findViewById(R.id.imgAvatarChuTro))
        }

        // Quan sát chi tiết thông tin
        viewModel.chiTietList.observe(this) { chiTietList ->
            chiTietAdapter = ChiTietThongTinAdapter(chiTietList)
            findViewById<RecyclerView>(R.id.recyclerViewChiTietThongTin).adapter = chiTietAdapter
        }

        // Quan sát dữ liệu phí dịch vụ
        viewModel.phiDichVuList.observe(this) { phiDichVuList ->
            phiDichVuAdapter = PhiDichVuAdapter(phiDichVuList)
            findViewById<RecyclerView>(R.id.recyclerViewPhiDichVu).adapter = phiDichVuAdapter
        }

        // Quan sát dữ liệu nội thất
        viewModel.noiThatList.observe(this) { noiThatList ->
            noiThatAdapter = NoiThatAdapter(noiThatList)
            findViewById<RecyclerView>(R.id.recyclerViewNoiThat).adapter = noiThatAdapter
        }

        // Quan sát dữ liệu tiện nghi
        viewModel.tienNghiList.observe(this) { tienNghiList ->
            tienNghiAdapter = TienNghiAdapter(tienNghiList)
            findViewById<RecyclerView>(R.id.recyclerViewTienNghi).adapter = tienNghiAdapter
        }
    }
}