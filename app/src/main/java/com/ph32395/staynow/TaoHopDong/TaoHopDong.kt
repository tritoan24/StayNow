// TaoHopDongActivity.kt
package com.ph32395.staynow.TaoHopDong

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.textfield.TextInputLayout
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
import com.ph32395.staynow.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TaoHopDong : AppCompatActivity() {

    //Khai báo phần lịch
    private lateinit var calendarView: CalendarView
    private var isSelectingStartDate = true
    //ngày bắt đầu và ngày kết thúc
    private var startDate: Calendar = Calendar.getInstance()
    private var endDate: Calendar = Calendar.getInstance()
    //Khai báo phần Lấy thông tin bảng Thông tin
    private lateinit var chiTietAdapter: ChiTietThongTinAdapter
    private lateinit var phiDichVuAdapter: PhiDichVuAdapter
    private lateinit var noiThatAdapter: NoiThatAdapter
    private lateinit var tienNghiAdapter: TienNghiAdapter
    //Khai báo viewModel
    private lateinit var viewModel: RoomDetailViewModel
    private lateinit var viewModelCccd: CccdViewModel

    private lateinit var tvDienTich: TextView
    private val auth = FirebaseAuth.getInstance()

    private lateinit var txtSoDienThoaiCT: TextView
    private lateinit var txtSoDienThoaiNT: TextView




    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tao_hop_dong)

        // Ánh xạ view phần xử lý chọn thời gian
        val tvStartDate = findViewById<TextView>(R.id.tvStartDate)
        val tvEndDate = findViewById<TextView>(R.id.tvEndDate)
        val startDateLayout = findViewById<LinearLayout>(R.id.StartDate)
        val endDateLayout = findViewById<LinearLayout>(R.id.EndDate)
        val tvMonth = findViewById<TextView>(R.id.tvSoThang)
        //ánh xạ thông tin CCCD
        val txtSoCCDCT = findViewById<TextView>(R.id.txtSoCCD)
        val txtHoTenCT = findViewById<TextView>(R.id.txtHoTenCCCD)
        val txtNgaySinhCT = findViewById<TextView>(R.id.txtNgaySinhCCCD)
        val txtGioiTinhCT = findViewById<TextView>(R.id.txtGioiTinhCCCD)
         txtSoDienThoaiCT = findViewById<TextView>(R.id.txtSDTCCCD)
        val txtDiaChiCT = findViewById<TextView>(R.id.txtDiaChiCCCD)
        val txtNgayCapCT = findViewById<TextView>(R.id.txtNgayCapCCCD)
        //ánh xạ thông tin CCCD bên thuê
        val txtSoCCDNT = findViewById<TextView>(R.id.txtSoCCDNT)
        val txtHoTenNT = findViewById<TextView>(R.id.txtHoTenCCCDNT)
        val txtNgaySinhNT = findViewById<TextView>(R.id.txtNgaySinhCCCDNT)
        val txtGioiTinhNT = findViewById<TextView>(R.id.txtGioiTinhCCCDNT)
         txtSoDienThoaiNT = findViewById<TextView>(R.id.txtSDTCCCDNT)
        val txtDiaChiNT = findViewById<TextView>(R.id.txtDiaChiCCCDNT)
        val txtNgayCapNT = findViewById<TextView>(R.id.txtNgayCapCCCDNT)
        //ánh xạ nút lưu
        val btnSave = findViewById<TextView>(R.id.btnSaveContract)
        //ánh xạ ngày thanh toán hàng tháng
        val txtNgayThanhToan = findViewById<TextView>(R.id.editTextNgayThanhToan)

        //nếu ngày thanh toán chưa nhập gì mặc định lấy ngày của startdate
        //ngày thanh toán ở ô Textinput của tôi đã setNumber
        if(txtNgayThanhToan.text.toString().isEmpty()){
            txtNgayThanhToan.text = startDate.get(Calendar.DAY_OF_MONTH).toString()
        }
        //nếu ngày không phải là ngày trong một tháng
        if(txtNgayThanhToan.text.toString().toInt() > 30 || txtNgayThanhToan.text.toString().toInt() < 1){
        }

        tvDienTich = findViewById(R.id.txtDienTich)
        calendarView = findViewById(R.id.calendarViewStartDate)

        //Khoi tao cac adapter
        chiTietAdapter = ChiTietThongTinAdapter(emptyList())
        phiDichVuAdapter = PhiDichVuAdapter(emptyList())
        noiThatAdapter = NoiThatAdapter(emptyList())
        tienNghiAdapter = TienNghiAdapter(emptyList())


        //lấy mã phòng trọ và lấy thông tin phòng trọ
        val maPhongTro = intent.getStringExtra("maPhongTro") ?: ""
        val maNguoiThue = intent.getStringExtra("maNguoiThue") ?: ""

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
            val tvNameRoom = findViewById<TextView>(R.id.txtTenPhongTro)
            val tvAddress = findViewById<TextView>(R.id.txtDiaChiHome)
            val tvPrice = findViewById<TextView>(R.id.tvGiaThue)
            val imageRoom = findViewById<ImageView>(R.id.imagePhongTro)

            //set gia tri cho cac view
            tvNameRoom.text = room.Ten_phongtro
            tvAddress.text = room.Dia_chichitiet
            tvPrice.text =  "${String.format("%,.0f", room.Gia_phong)} VND"
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
                tvStartDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate.time)
                // Cập nhật ngày kết thúc dựa trên số tháng
                DateUtils.updateEndDateBasedOnMonths(startDate, tvMonth.text.toString(), tvEndDate)
                txtNgayThanhToan.text = startDate.get(Calendar.DAY_OF_MONTH).toString()
            } else {
                endDate = selectedDate
                tvEndDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(endDate.time)
                // Kiểm tra nếu ngày kết thúc nhỏ hơn ngày bắt đầu
                if (endDate.before(startDate)) {
                    Toast.makeText(this, "Ngày kết thúc không được nhỏ hơn ngày bắt đầu", Toast.LENGTH_SHORT).show()
                    // Reset lại ngày kết thúc
                    endDate = Calendar.getInstance().apply { time = startDate.time }
                    tvEndDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(endDate.time)
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

}
