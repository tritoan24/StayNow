// TaoHopDongActivity.kt
package com.ph32395.staynow_datn.TaoHopDong

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ph32395.staynow_datn.Activity.RoomDetailActivity
import com.ph32395.staynow_datn.Adapter.ChiTietThongTinAdapter
import com.ph32395.staynow_datn.Adapter.NoiThatAdapter
import com.ph32395.staynow_datn.Adapter.PhiDichVuAdapter
import com.ph32395.staynow_datn.Adapter.SpacingItemDecoration
import com.ph32395.staynow_datn.Adapter.TienNghiAdapter
import com.ph32395.staynow_datn.CCCD.CccdViewModel
import com.ph32395.staynow_datn.ChucNangChung.LoadingUtil
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.ViewModel.RoomDetailViewModel
import com.ph32395.staynow_datn.hieunt.model.NotificationModel
import com.ph32395.staynow_datn.hieunt.view_model.NotificationViewModel
import com.ph32395.staynow_datn.hieunt.view_model.ViewModelFactory
import com.ph32395.staynow_datn.hieunt.widget.toast
import com.ph32395.staynow_datn.utils.DateUtils
import jp.wasabeef.richeditor.RichEditor
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class TaoHopDong : AppCompatActivity() {

    // Khai báo ViewModel
    private val viewModelHopDong: ContractViewModel by viewModels()
    private lateinit var viewModel: RoomDetailViewModel
    private lateinit var viewModelCccd: CccdViewModel
    private lateinit var viewModelNotification: NotificationViewModel
    private var utilityFees: List<UtilityFee> = listOf()

    // Khai báo phần lịch
    private lateinit var calendarView: CalendarView
    private var isSelectingStartDate = true

    // Ngày bắt đầu và ngày kết thúc
    private var startDate: Calendar = Calendar.getInstance()
    private var endDate: Calendar = Calendar.getInstance()

    // Khai báo phần Lấy thông tin bảng Thông tin
    private lateinit var chiTietAdapter: ChiTietThongTinAdapter
    private lateinit var phiDichVuAdapter: PhiDichVuAdapter
    private lateinit var noiThatAdapter: NoiThatAdapter
    private lateinit var tienNghiAdapter: TienNghiAdapter

    // Firebase Auth
    private val auth = FirebaseAuth.getInstance()

    // Ánh xạ View
    private lateinit var tvNameRoom: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvPrice: TextView
    private lateinit var imageRoom: ImageView
    private lateinit var tvDienTich: TextView

    private lateinit var edSodien: EditText
    private lateinit var edSonuoc: EditText
    private lateinit var soNguoio: EditText


    private lateinit var tvStartDate: TextView
    private lateinit var tvEndDate: TextView
    private lateinit var startDateLayout: LinearLayout
    private lateinit var endDateLayout: LinearLayout
    private lateinit var tvMonth: TextView

    private lateinit var txtSoCCDCT: TextView
    private lateinit var txtHoTenCT: TextView
    private lateinit var txtNgaySinhCT: TextView
    private lateinit var txtGioiTinhCT: TextView
    private lateinit var txtSoDienThoaiCT: TextView
    private lateinit var txtDiaChiCT: TextView
    private lateinit var txtNgayCapCT: TextView

    private lateinit var txtSoCCDNT: TextView
    private lateinit var txtHoTenNT: TextView
    private lateinit var txtNgaySinhNT: TextView
    private lateinit var txtGioiTinhNT: TextView
    private lateinit var txtSoDienThoaiNT: TextView
    private lateinit var txtDiaChiNT: TextView
    private lateinit var txtNgayCapNT: TextView

    private lateinit var btnSaveContract: Button
    private lateinit var txtNgayThanhToan: TextView
    private lateinit var editorDieuKhoan: RichEditor
    private lateinit var note: TextView
    private lateinit var btnBack: Button
    private lateinit var btncardview: CardView

    private lateinit var maPhongTro: String
    private lateinit var maNguoiThue: String
    private lateinit var idLichhen: String
    private lateinit var idHopDong:String

    private var giaPhong: Double = 0.0

    //khai báo loading animation
    private lateinit var loadingUtil: LoadingUtil


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tao_hop_dong)

        // Gọi hàm ánh xạ View
        initViews()

        //nếu ngày thanh toán chưa nhập gì mặc định lấy ngày của startdate
        //ngày thanh toán ở ô Textinput của tôi đã setNumber
        if (txtNgayThanhToan.text.toString().isEmpty()) {
            txtNgayThanhToan.text = startDate.get(Calendar.DAY_OF_MONTH).toString()
        }


        // Khởi tạo LoadingUtil
        loadingUtil = LoadingUtil(this)


        calendarView = findViewById(R.id.calendarViewStartDate)

        //Khoi tao cac adapter
        chiTietAdapter = ChiTietThongTinAdapter(emptyList())
        phiDichVuAdapter = PhiDichVuAdapter(emptyList())
        noiThatAdapter = NoiThatAdapter(emptyList())
        tienNghiAdapter = TienNghiAdapter(emptyList())


        //lấy mã phòng trọ và lấy thông tin phòng trọ
        maPhongTro = intent.getStringExtra("maPhongTro") ?: ""
        maNguoiThue = intent.getStringExtra("maNguoiThue") ?: ""
        idLichhen = intent.getStringExtra("idLichhen") ?: ""


        //gọi hàm lấy số điện thoại người dùng
        getPhoneNumberFromId(auth.currentUser?.uid ?: "") { phoneNumber1 ->
            txtSoDienThoaiCT.text = phoneNumber1
        }
        getPhoneNumberFromId(maNguoiThue) { phoneNumber2 ->
            txtSoDienThoaiNT.text = phoneNumber2
        }

        //Khoi tao viewModel
        viewModel = ViewModelProvider(this)[RoomDetailViewModel::class.java]
        viewModelCccd = ViewModelProvider(this)[CccdViewModel::class.java]
        val factory = ViewModelFactory(this)
        viewModelNotification = ViewModelProvider(this, factory)[NotificationViewModel::class.java]

        //Lay du lieu chi tiet thong tin phong tro
        viewModel.fetchChiTietThongTin(maPhongTro)
        viewModel.fetchPhiDichVu(maPhongTro)
        viewModel.fetchNoiThat(maPhongTro)
        viewModel.fetchTienNghi(maPhongTro)
        //Tai du lieu tu Firebase
        viewModel.fetchRoomDetail(maPhongTro)
        // Hàm lấy dữ liệu CCCD been cho thuee
        viewModelCccd.fetchCccdData(userId = auth.currentUser?.uid ?: "")
        // Hàm lấy dữ liệu CCCD cho ben thue
        viewModelCccd.fetchCccdDataNT(maNguoiThue)



        viewModel.room.observe(this) { room ->
            Log.d("TaoHopDong", "room: $room")
            //khai baos cac view


            //set gia tri cho Phòng trọ
            tvNameRoom.text = room.Ten_phongtro
            tvAddress.text = room.Dia_chichitiet
            tvPrice.text = "${String.format("%,.0f", room.Gia_phong)} VND"
            giaPhong = room.Gia_phong
            // Cập nhật ảnh phòng trọ
            Glide.with(this)
                .load(room.imageUrls[0])
                .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(16)))
                .into(imageRoom)
        }
        observeViewModel()
        setupRecyclerViewThongTinChiTiet()
        setupListPhiDichVu()
        setupRecyViewTienNghi()
        setupRecyclerViewNoiThat()


        //Lay du lieu chi tiet thong tin phong tro
        viewModel.fetchChiTietThongTin(maPhongTro)
        viewModel.fetchPhiDichVu(maPhongTro)
        viewModel.fetchNoiThat(maPhongTro)
        viewModel.fetchTienNghi(maPhongTro)

        //code xử lý chọn ngày
        // Set ngày bắt đầu là ngày hôm nay
        val calendar = Calendar.getInstance()
        val today = calendar.time
        startDate.time = today
        tvStartDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(today)
        endDate.time = today
        tvEndDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(today)
        // Mặc định ẩn CalendarView
        calendarView.visibility = View.GONE
        // Khi nhấn vào vùng chọn ngày bắt đầu
        startDateLayout.setOnClickListener {
            isSelectingStartDate = true
            toggleCalendarVisibility()
        }
        // Khi nhấn vào vùng chọn ngày kết thúc
        endDateLayout.setOnClickListener {
            isSelectingStartDate = false
            toggleCalendarVisibility()
        }
        // Xử lý sự kiện chọn ngày trên CalendarView
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            if (isSelectingStartDate) {
                startDate = selectedDate
                tvStartDate.text =
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate.time)
                // Cập nhật ngày kết thúc dựa trên số tháng
                DateUtils.updateEndDateBasedOnMonths(startDate, tvMonth.text.toString(), tvEndDate)
                txtNgayThanhToan.text = startDate.get(Calendar.DAY_OF_MONTH).toString()
            } else {
                endDate = selectedDate
                tvEndDate.text =
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(endDate.time)
                // Kiểm tra nếu ngày kết thúc nhỏ hơn ngày bắt đầu
                if (endDate.before(startDate)) {
                    Toast.makeText(
                        this,
                        "Ngày kết thúc không được nhỏ hơn ngày bắt đầu",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Reset lại ngày kết thúc
                    endDate = Calendar.getInstance().apply { time = startDate.time }
                    tvEndDate.text =
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(endDate.time)
                } else {
                    // Cập nhật số tháng nếu ngày kết thúc hợp lệ
                    DateUtils.updateMonthsBasedOnDates(startDate, endDate, tvMonth)
                }
            }

            // Ẩn CalendarView sau khi chọn
            calendarView.visibility = View.GONE
        }
        // Khi nhấn vào TextView số tháng
        tvMonth.setOnClickListener {
            DateUtils.showMonthPicker(this, tvMonth, startDate, tvEndDate)
        }


        // Quan sát lỗi
        viewModelCccd.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
        viewModelCccd.cccdData.observe(this) { data ->
            // Hiển thị dữ liệu CCCD
            data?.let {
                Log.d("CCCD Data", it.toString())
                // Hiển thị dữ liệu CCCD lên các TextView
                // Hiển thị dữ liệu CCCD lên các TextView
                txtSoCCDCT.text = "Số CCCD: ${it["So_cccd"] ?: "Không có"}"
                txtHoTenCT.text = "Họ và tên: ${it["Hovaten"] ?: "Không có"}"
                txtNgaySinhCT.text = "Ngày sinh: ${it["Ngaysinh"] ?: "Không có"}"
                txtDiaChiCT.text = "Địa chỉ: ${it["Điachi"] ?: "Không có"}"
                txtNgayCapCT.text = "Ngày cấp: ${it["Ngaycap"] ?: "Không có"}"

            }
        }
        viewModelCccd.cccdDataNT.observe(this) { data ->
            // Hiển thị dữ liệu CCCD
            data?.let {
                Log.d("CCCD Data", it.toString())
                // Hiển thị dữ liệu CCCD lên các TextView
                // Hiển thị dữ liệu CCCD lên các TextView
                txtSoCCDNT.text = "Số CCCD: ${it["So_cccd"] ?: "Không có"}"
                txtHoTenNT.text = "Họ và tên: ${it["Hovaten"] ?: "Không có"}"
                txtNgaySinhNT.text = "Ngày sinh: ${it["Ngaysinh"] ?: "Không có"}"
                txtDiaChiNT.text = "Địa chỉ: ${it["Điachi"] ?: "Không có"}"
                txtNgayCapNT.text = "Ngày cấp: ${it["Ngaycap"] ?: "Không có"}"

            }
        }



        //sử lý text DieuKhoan
        editorDieuKhoan.setEditorFontSize(16) // Kích thước font chữ
        editorDieuKhoan.setEditorFontColor(
            resources.getColor(
                android.R.color.black,
                theme
            )
        ) // Màu chữ
        editorDieuKhoan.setEditorBackgroundColor(
            resources.getColor(
                android.R.color.white,
                theme
            )
        ) // Màu nền
        editorDieuKhoan.setPlaceholder("Nhập quy định hợp đồng...") // Gợi ý khi chưa có nội dung

        // Xử lý nút "Bullet" (ký tự đầu dòng)
        findViewById<ImageButton>(R.id.btn_bullet).setOnClickListener {
            editorDieuKhoan.setBullets()
        }

        //back button
        btnBack.setOnClickListener {
            finish()
        }
        // Lưu hợp đồng
        btnSaveContract.setOnClickListener {
            if(validateContract()) {
                loadingUtil.show()
                createAndSaveContract()
            }
        }

        //ấn vào phòng trọ
        btncardview.setOnClickListener{
            val intent = Intent(this, RoomDetailActivity::class.java)
            intent.putExtra("maPhongTro", maPhongTro)
            startActivity(intent)
        }

    }



    // Hàm chuyển đổi trạng thái hiển thị CalendarView
    private fun toggleCalendarVisibility() {
        if (calendarView.visibility == View.GONE) {
            calendarView.visibility = View.VISIBLE
        } else {
            calendarView.visibility = View.GONE
        }
    }
    //    Danh sacch thng tin chi tiet
    private fun setupRecyclerViewThongTinChiTiet() {
        findViewById<RecyclerView>(R.id.listViewThongTin).apply {
            layoutManager = GridLayoutManager(context, 4)
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

//           addItemDecoration(SpacingItemDecoration(1))
        }
    }
    //hàm lấy số điện thoại người dùng từ id
    private fun getPhoneNumberFromId(id: String, callback: (String) -> Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("NguoiDung").child(id)
        ref.get().addOnSuccessListener {
            if (it.exists()) {
                val phoneNumber = it.child("sdt").value.toString()
                callback(phoneNumber)  // Gọi callback và truyền số điện thoại vào
            } else {
                callback("")  // Nếu không tìm thấy, trả về chuỗi rỗng
            }
        }
    }

private fun observeViewModel() {
    //        Quan sat chi tiet thong tin
    viewModel.chiTietList.observe(this) { chiTietList ->
        chiTietAdapter = ChiTietThongTinAdapter(chiTietList)
        findViewById<RecyclerView>(R.id.listViewThongTin).adapter = chiTietAdapter
        if (chiTietList.size > 3) {
            tvDienTich.text = "${chiTietList[3].so_luong_donvi} m²"
        } else {
            tvDienTich.text = "Không có dữ liệu"
        }

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
    //hàm lưu hợp đồng
    private fun createAndSaveContract() {
        val utilityFees = viewModel.phiDichVuList.value?.map { phiDichVu ->
            UtilityFee(
                tenDichVu = phiDichVu.ten_dichvu,
                giaTien = phiDichVu.so_tien,
                donVi = phiDichVu.don_vi,
                batBuoc = true // Có thể thêm trường này vào model PhiDichVu nếu cần
            )
        } ?: emptyList()



        // Lấy danh sách tên tiện nghi
        val listAmenities = viewModel.tienNghiList.value?.map { tn ->
            tn.Ten_tiennghi // Chỉ cần lấy tên tiện nghi vì model HopDong.amenities là List<String>
        } ?: emptyList()

        // Lấy danh sách tên nội thất
        val listFurniture = viewModel.noiThatList.value?.map { nt ->
            nt.Ten_noithat // Chỉ cần lấy tên nội thất vì model HopDong.furniture là List<String>
        } ?: emptyList()
        val roomDetail = viewModel.chiTietList.value?.map { tt ->
            RoomDetail(
                ten = tt.ten_thongtin,
                giaTri = tt.so_luong_donvi,
                donVi = tt.don_vi,
            )
        } ?: emptyList()


        // Lấy giá trị tiền cọc từ ViewModel
        val tienCoc = viewModel.getTienCocValue()

        val (totalFee, feeDetails) = viewModelHopDong.extractFixedFees(utilityFees, soNguoio.text.toString().toInt())
        val extractVariableFees = viewModelHopDong.extractVariableFees(utilityFees)

        // Tạo đối tượng hợp đồng từ dữ liệu form
        val contract = HopDong(
            maHopDong = "",
            ngayTao = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
            ngayBatDau = tvStartDate.text.toString(),
            ngayKetThuc = tvEndDate.text.toString(),
            thoiHanThue = tvMonth.text.toString(),
            ngayThanhToan = txtNgayThanhToan.text.toString().toInt(),
            ghiChu = note.text.toString().trim(),
            soNguoiO = soNguoio.text.toString().toIntOrNull() ?: 1,
            soDienCu = edSodien.text.toString().toInt(),
            soNuocCu = edSonuoc.text.toString().toInt(),
            chuNha = PersonInfo(
                maNguoiDung = auth.currentUser?.uid ?: "",
                hoTen = txtHoTenCT.text.toString(),
                soCCCD = txtSoCCDCT.text.toString(),
                soDienThoai = txtSoDienThoaiCT.text.toString(),
                diaChi = txtDiaChiCT.text.toString(),
                ngayCapCCCD = txtNgayCapCT.text.toString(),
                ngaySinh = txtNgaySinhCT.text.toString()
                // Thêm các thông tin khác
            ),
            nguoiThue = PersonInfo(
                maNguoiDung = maNguoiThue,
                hoTen = txtHoTenNT.text.toString(),
                soCCCD = txtSoCCDNT.text.toString(),
                soDienThoai = txtSoDienThoaiNT.text.toString(),
                diaChi = txtDiaChiNT.text.toString(),
                ngayCapCCCD = txtNgayCapNT.text.toString(),
                ngaySinh = txtNgaySinhNT.text.toString()
                // Thêm các thông tin khác
            ),
            thongTinTaiChinh = FinancialInfo(
                giaThue = giaPhong,
                tienCoc = tienCoc,
                soDienht = edSodien.text.toString().toInt(),
                soNguoio = soNguoio.text.toString().toInt(),
                soNuocht = edSonuoc.text.toString().toInt(),
                phiDichVu = utilityFees
            ),
            thongtinphong = RoomInfo(
                maPhongTro = maPhongTro,
                tenPhong = tvNameRoom.text.toString(),
                diaChiPhong = tvAddress.text.toString(),
                dienTich = tvDienTich.text.toString().replace("m²", "").trim().toDouble(),
                thongTinChiTiet = roomDetail
            ),
            hoaDonHopDong = Invoice(
                idHoaDon =  UUID.randomUUID().toString(),
                ngayLap = tvStartDate.text.toString(),
                kyHoaDon = tvEndDate.text.toString(),
                tenKhachHang =txtHoTenNT.text.toString(),
                tenPhong = tvNameRoom.text.toString(),
                phiCoDinh =feeDetails,
                phiBienDong = extractVariableFees,
                tongTien = giaPhong+tienCoc,
                trangThai = InvoiceStatus.PENDING,
                tienPhong = giaPhong,
                tienCoc = tienCoc,
                tongTienDichVu = totalFee,
                kieuHoadon = "HoaDonHopDong",
                idNguoinhan = maNguoiThue,
                idNguoigui = auth.currentUser?.uid ?: "",
                ngayThanhToan = txtNgayThanhToan.text.toString().toInt(),
                paymentDate = "",
                soDienCu = edSodien.text.toString().toInt(),
                soNuocCu = edSonuoc.text.toString().toInt(),
            ),

            tienNghi = listAmenities, // Danh sách String chứa tên tiện nghi
            noiThat = listFurniture, // Danh sách String chứa tên nội thất
            dieuKhoan = editorDieuKhoan.html,

            // Thêm các thông tin khác

        )
        viewModelHopDong.saveContract(contract,idLichhen)
        viewModelHopDong.saveMessageStatus(maNguoiThue,auth.currentUser?.uid ?: "")

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModelHopDong.navigateToContractDetail.collect { contractId ->
                    contractId?.let {
                        val intent = Intent(this@TaoHopDong, ChiTietHopDong::class.java)
                        intent.putExtra("CONTRACT_ID", it)
                        intent.putExtra("Check", 1)
                        startActivity(intent)
                        // Reset giá trị sau khi chuyển màn
                        viewModelHopDong.resetNavigation()
                        // Gửi thông báo
                        val notificationModel = NotificationModel(
                            title = "Hợp đồng mới",
                            message = "Bạn có một hợp đồng mới",
                            date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                            time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                            typeNotification = "HopDongMoi",
                            mapLink = null,
                            timestamp = System.currentTimeMillis(),
                            idModel = it,


                        )
                        viewModelNotification.sendNotification (notificationModel, maNguoiThue)
                        // Đóng màn hình hiện tại sau khi chuyển màn
                        finish()
                        loadingUtil.hide()
                    }
                }
            }
        }



    }

    private fun initViews() {
        // Thông tin phòng
        tvNameRoom = findViewById(R.id.txtTenPhongTro)
        tvAddress = findViewById(R.id.txtDiaChiHome)
        tvPrice = findViewById(R.id.tvGiaThue)
        imageRoom = findViewById(R.id.imagePhongTro)
        tvDienTich = findViewById(R.id.txtDienTich)

        edSodien = findViewById(R.id.editTextSoDien)
        edSonuoc = findViewById(R.id.editTextSoNuoc)
        soNguoio = findViewById(R.id.editTextSoNguoi)


        // Thời gian
        tvStartDate = findViewById(R.id.tvStartDate)
        tvEndDate = findViewById(R.id.tvEndDate)
        startDateLayout = findViewById(R.id.StartDate)
        endDateLayout = findViewById(R.id.EndDate)
        tvMonth = findViewById(R.id.tvSoThang)

        // CCCD chủ nhà
        txtSoCCDCT = findViewById(R.id.txtSoCCD)
        txtHoTenCT = findViewById(R.id.txtHoTenCCCD)
        txtNgaySinhCT = findViewById(R.id.txtNgaySinhCCCD)
        txtGioiTinhCT = findViewById(R.id.txtGioiTinhCCCD)
        txtSoDienThoaiCT = findViewById(R.id.txtSDTCCCD)
        txtDiaChiCT = findViewById(R.id.txtDiaChiCCCD)
        txtNgayCapCT = findViewById(R.id.txtNgayCapCCCD)

        // CCCD người thuê
        txtSoCCDNT = findViewById(R.id.txtSoCCDNT)
        txtHoTenNT = findViewById(R.id.txtHoTenCCCDNT)
        txtNgaySinhNT = findViewById(R.id.txtNgaySinhCCCDNT)
        txtGioiTinhNT = findViewById(R.id.txtGioiTinhCCCDNT)
        txtSoDienThoaiNT = findViewById(R.id.txtSDTCCCDNT)
        txtDiaChiNT = findViewById(R.id.txtDiaChiCCCDNT)
        txtNgayCapNT = findViewById(R.id.txtNgayCapCCCDNT)

        // Lưu hợp đồng
        btnSaveContract = findViewById(R.id.btnSaveContract)

        // Quay lại
        btnBack = findViewById(R.id.btnBack)

        // ấn vào phòng trọ
        btncardview = findViewById(R.id.cardViewPhongTro)

        // Ngày thanh toán
        txtNgayThanhToan = findViewById(R.id.editTextNgayThanhToan)

        // Điều khoản
        editorDieuKhoan = findViewById(R.id.editorDieuKhoan)

        //note
        note = findViewById(R.id.note)


    }

    private fun validateContract(): Boolean {
        //nếu ngày không phải là ngày trong một tháng
        if (txtNgayThanhToan.text.toString().toInt() > 30 || txtNgayThanhToan.text.toString().toInt() < 1){
            toast("Ngày thanh toán không hợp lệ")
            return false
        }
        if (edSodien.text.toString().isEmpty() || edSonuoc.text.toString().isEmpty() || soNguoio.text.toString().isEmpty()) {
            toast("Vui lòng nhập đầy đủ thông tin")
            return false
        }

        // Thêm các validation khác
        return true
    }
}