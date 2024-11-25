// TaoHopDongActivity.kt
package com.ph32395.staynow.TaoHopDong

import FinancialInfo
import HopDong
import PersonInfo
import RoomDetail
import RoomInfo
import UtilityFee
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CalendarView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ph32395.staynow.Adapter.ChiTietThongTinAdapter
import com.ph32395.staynow.Adapter.NoiThatAdapter
import com.ph32395.staynow.Adapter.PhiDichVuAdapter
import com.ph32395.staynow.Adapter.SpacingItemDecoration
import com.ph32395.staynow.Adapter.TienNghiAdapter
import com.ph32395.staynow.CCCD.CccdViewModel
import com.ph32395.staynow.R
import com.ph32395.staynow.ViewModel.RoomDetailViewModel
import com.ph32395.staynow.hieunt.widget.toast
import com.ph32395.staynow.utils.DateUtils
import jp.wasabeef.richeditor.RichEditor
import java.text.SimpleDateFormat
import java.util.*

class TaoHopDong : AppCompatActivity() {

    // Khai báo ViewModel
    private val viewModelHopDong: ContractViewModel by viewModels()
    private lateinit var viewModel: RoomDetailViewModel
    private lateinit var viewModelCccd: CccdViewModel

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

    private lateinit var btnSave: TextView
    private lateinit var txtNgayThanhToan: TextView
    private lateinit var editorDieuKhoan: RichEditor
    private lateinit var note: TextView

    private lateinit var maPhongTro: String
    private lateinit var maNguoiThue: String

    private var giaPhong: Double = 0.0




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


        calendarView = findViewById(R.id.calendarViewStartDate)

        //Khoi tao cac adapter
        chiTietAdapter = ChiTietThongTinAdapter(emptyList())
        phiDichVuAdapter = PhiDichVuAdapter(emptyList())
        noiThatAdapter = NoiThatAdapter(emptyList())
        tienNghiAdapter = TienNghiAdapter(emptyList())


        //lấy mã phòng trọ và lấy thông tin phòng trọ
        maPhongTro = intent.getStringExtra("maPhongTro") ?: ""
        maNguoiThue = intent.getStringExtra("maNguoiThue") ?: ""

        //gọi hàm lấy số điện thoại người dùng
        getPhoneNumberFromId(auth.currentUser?.uid ?: "") { phoneNumber1 ->
            txtSoDienThoaiCT.text = phoneNumber1
        }
        getPhoneNumberFromId(maNguoiThue) { phoneNumber2 ->
            txtSoDienThoaiNT.text = phoneNumber2
        }

        //Khoi tao viewModel
        viewModel = ViewModelProvider(this)[RoomDetailViewModel::class.java]
        viewModelCccd = ViewModelProvider(this).get(CccdViewModel::class.java)

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

        btnSave.setOnClickListener {
            if(validateContract()) {
                createAndSaveContract()
            }
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
                name = phiDichVu.ten_dichvu,
                amount = phiDichVu.so_tien,
                unit = phiDichVu.don_vi,
                isRequired = true // Có thể thêm trường này vào model PhiDichVu nếu cần
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
                name = tt.ten_thongtin,
                value = tt.so_luong_donvi,
                unit = tt.don_vi,
            )
        } ?: emptyList()
        // Lấy giá trị tiền cọc từ ViewModel
        val tienCoc = viewModel.getTienCocValue()


        // Tạo đối tượng hợp đồng từ dữ liệu form
        val contract = HopDong(
            contractId = "", // Để trống để tạo mới
            createdDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
            startDate = tvStartDate.text.toString(),
            endDate = tvEndDate.text.toString(),
            rentDuration = tvMonth.text.toString(),
            paymentDay = txtNgayThanhToan.text.toString().toInt(),
            note = note.text.toString(),
            roomInfo = RoomInfo(
                roomId = maPhongTro,
                roomName = tvNameRoom.text.toString(),
                address = tvAddress.text.toString(),
                area = tvDienTich.text.toString().replace("m²", "").trim().toDouble(),
                details = roomDetail
            ),
            landlordInfo = PersonInfo(
                userId = auth.currentUser?.uid ?: "",
                fullName = txtHoTenCT.text.toString(),
                idNumber = txtSoCCDCT.text.toString(),
                phone = txtSoDienThoaiCT.text.toString(),
                address = txtDiaChiCT.text.toString(),
                idIssueDate = txtNgayCapCT.text.toString(),
                dateOfBirth = txtNgaySinhCT.text.toString()
                // Thêm các thông tin khác
            ),
            tenantInfo = PersonInfo(
                userId = maNguoiThue,
                fullName = txtHoTenNT.text.toString(),
                idNumber = txtSoCCDNT.text.toString(),
                phone = txtSoDienThoaiNT.text.toString(),
                address = txtDiaChiNT.text.toString(),
                idIssueDate = txtNgayCapNT.text.toString(),
                dateOfBirth = txtNgaySinhNT.text.toString()
                // Thêm các thông tin khác
            ),
            financialInfo = FinancialInfo(
               monthlyRent = giaPhong,
                deposit = tienCoc,
                utilities = utilityFees
            ),
            amenities = listAmenities, // Danh sách String chứa tên tiện nghi
            furniture = listFurniture, // Danh sách String chứa tên nội thất

            terms = editorDieuKhoan.html,



            // Thêm các thông tin khác

        )
        // Lưu hợp đồng
        viewModelHopDong.saveContract(contract)
    }

    private fun initViews() {
        // Thông tin phòng
        tvNameRoom = findViewById(R.id.txtTenPhongTro)
        tvAddress = findViewById(R.id.txtDiaChiHome)
        tvPrice = findViewById(R.id.tvGiaThue)
        imageRoom = findViewById(R.id.imagePhongTro)
        tvDienTich = findViewById(R.id.txtDienTich)

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
        btnSave = findViewById(R.id.btnSaveContract)

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
        // Thêm các validation khác
        return true
    }
}

