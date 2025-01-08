package com.ph32395.staynow_datn.TaoHopDong

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow_datn.Activity.RoomDetailActivity
import com.ph32395.staynow_datn.Adapter.ChiTietThongTinAdapter
import com.ph32395.staynow_datn.Adapter.NoiThatAdapter
import com.ph32395.staynow_datn.Adapter.PhiDichVuAdapter
import com.ph32395.staynow_datn.Adapter.SpacingItemDecoration
import com.ph32395.staynow_datn.Adapter.TienNghiAdapter
import com.ph32395.staynow_datn.CCCD.CccdViewModel
import com.ph32395.staynow_datn.ChucNangChung.LoadingUtil
import com.ph32395.staynow_datn.ViewModel.RoomDetailViewModel
import com.ph32395.staynow_datn.databinding.UpdateHopDongBinding
import com.ph32395.staynow_datn.hieunt.model.NotificationModel
import com.ph32395.staynow_datn.hieunt.view_model.NotificationViewModel
import com.ph32395.staynow_datn.hieunt.view_model.ViewModelFactory
import com.ph32395.staynow_datn.hieunt.widget.toast
import com.ph32395.staynow_datn.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class UpdateHopDong(
    private var maPhongTro: String,
    private var maNguoiThue: String,
    private val contract: HopDong
) :
    BottomSheetDialogFragment() {

    private lateinit var binding: UpdateHopDongBinding
    private val firebase = FirebaseFirestore.getInstance()
    private val hopDongRef = firebase.collection("HopDong")

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
//    private lateinit var tvNameRoom: TextView
//    private lateinit var tvAddress: TextView
//    private lateinit var tvPrice: TextView
//    private lateinit var imageRoom: ImageView
//    private lateinit var tvDienTich: TextView

//    private lateinit var edSodien: EditText
//    private lateinit var edSonuoc: EditText
//    private lateinit var soNguoio: EditText


//    private lateinit var tvStartDate: TextView
//    private lateinit var tvEndDate: TextView
//    private lateinit var startDateLayout: LinearLayout
//    private lateinit var endDateLayout: LinearLayout
//    private lateinit var tvMonth: TextView

//    private lateinit var txtSoCCDCT: TextView
//    private lateinit var txtHoTenCT: TextView
//    private lateinit var txtNgaySinhCT: TextView
//    private lateinit var txtGioiTinhCT: TextView
//    private lateinit var txtSoDienThoaiCT: TextView
//    private lateinit var txtDiaChiCT: TextView
//    private lateinit var txtNgayCapCT: TextView

//    private lateinit var txtSoCCDNT: TextView
//    private lateinit var txtHoTenNT: TextView
//    private lateinit var txtNgaySinhNT: TextView
//    private lateinit var txtGioiTinhNT: TextView
//    private lateinit var txtSoDienThoaiNT: TextView
//    private lateinit var txtDiaChiNT: TextView
//    private lateinit var txtNgayCapNT: TextView

//    private lateinit var btnSaveContract: Button
//    private lateinit var txtNgayThanhToan: TextView
//    private lateinit var editorDieuKhoan: RichEditor
//    private lateinit var note: TextView
//    private lateinit var btnBack: Button
//    private lateinit var btncardview: CardView

    //    private lateinit var maPhongTro: String
//    private lateinit var maNguoiThue: String
    private lateinit var idLichhen: String
    private lateinit var idHopDong: String

    private var giaPhong: Double = 0.0

    //khai báo loading animation
    private lateinit var loadingUtil: LoadingUtil
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = UpdateHopDongBinding.inflate(inflater, container?.parent as ViewGroup?, false)
        // Gọi hàm ánh xạ View
        //initViews()

        //nếu ngày thanh toán chưa nhập gì mặc định lấy ngày của startdate
        //ngày thanh toán ở ô Textinput của tôi đã setNumber
        if (binding.editTextNgayThanhToan.text.toString().isEmpty()) {
            binding.editTextNgayThanhToan.setText(startDate.get(Calendar.DAY_OF_MONTH).toString())
        }


        // Khởi tạo LoadingUtil
        loadingUtil = LoadingUtil(requireActivity())


        calendarView = binding.calendarViewStartDate

        //Khoi tao cac adapter
        chiTietAdapter = ChiTietThongTinAdapter(emptyList())
        phiDichVuAdapter = PhiDichVuAdapter(emptyList())
        noiThatAdapter = NoiThatAdapter(emptyList())
        tienNghiAdapter = TienNghiAdapter(emptyList())


        //lấy mã phòng trọ và lấy thông tin phòng trọ
//        maPhongTro = requireActivity().intent.getStringExtra("maPhongTro") ?: ""
//        maNguoiThue = requireActivity().intent.getStringExtra("maNguoiThue") ?: ""
//        idLichhen = requireActivity().intent.getStringExtra("idLichhen") ?: ""


        //gọi hàm lấy số điện thoại người dùng
        getPhoneNumberFromId(auth.currentUser?.uid ?: "") { phoneNumber1 ->
            binding.txtSDTCCCD.text = phoneNumber1
        }
        getPhoneNumberFromId(maNguoiThue) { phoneNumber2 ->
            binding.txtSDTCCCDNT.text = phoneNumber2
        }

        //Khoi tao viewModel
        viewModel = ViewModelProvider(this)[RoomDetailViewModel::class.java]
        viewModelCccd = ViewModelProvider(this)[CccdViewModel::class.java]
        val factory = ViewModelFactory(requireActivity())
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


        // Quan sát trạng thái hiển thị của EditText điện
        viewModelHopDong.isElectricityInputVisible.observe(this) { isVisible ->
            binding.editTextSoDien.visibility = if (isVisible) View.VISIBLE else View.GONE
        }

        // Quan sát trạng thái hiển thị của EditText nước
        viewModelHopDong.isWaterInputVisible.observe(this) { isVisible ->
            binding.editTextSoNuoc.visibility = if (isVisible) View.VISIBLE else View.GONE
        }

        viewModel.phiDichVuList.observe(this) { phiDichVuList ->
            // Cập nhật trạng thái hiển thị
            viewModelHopDong.updateUtilityInputVisibility(phiDichVuList)
        }



        viewModel.room.observe(this) { room ->
            Log.d("TaoHopDong", "room: $room")
            //khai baos cac view


            //set gia tri cho Phòng trọ
            binding.txtTenPhongTro.text = room.tenPhongTro
            binding.txtDiaChiHome.text = room.diaChiChiTiet
            binding.tvGiaThue.text = "${String.format("%,.0f", room.giaPhong)} VND"
            giaPhong = room.giaPhong
            // Cập nhật ảnh phòng trọ
            Glide.with(this)
                .load(room.imageUrls[0])
                .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(16)))
                .into(binding.imagePhongTro)
        }
        observeViewModel()
        setupRecyclerViewThongTinChiTiet()
        setupListPhiDichVu()
        setupRecyViewTienNghi()
        setupRecyclerViewNoiThat()
        updateUI(contract)


        //Lay du lieu chi tiet thong tin phong tro
        viewModel.fetchChiTietThongTin(maPhongTro)
        viewModel.fetchPhiDichVu(maPhongTro)
        viewModel.fetchNoiThat(maPhongTro)
        viewModel.fetchTienNghi(maPhongTro)

        //code xử lý chọn ngày
        // Set ngày bắt đầu là ngày hôm nay
        //val calendar = Calendar.getInstance()
        //val today = calendar.time
        //startDate.time = today
        //binding.tvStartDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(today)
        //endDate.time = today
        //binding.tvEndDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(today)
        // Mặc định ẩn CalendarView
        calendarView.visibility = View.GONE
        // Khi nhấn vào vùng chọn ngày bắt đầu
        binding.StartDate.setOnClickListener {
            isSelectingStartDate = true
            toggleCalendarVisibility()
        }
        // Khi nhấn vào vùng chọn ngày kết thúc
        binding.EndDate.setOnClickListener {
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
                binding.tvStartDate.text =
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate.time)
                // Cập nhật ngày kết thúc dựa trên số tháng
                DateUtils.updateEndDateBasedOnMonths(
                    startDate,
                    binding.tvSoThang.text.toString(),
                    binding.tvEndDate
                )
                binding.editTextNgayThanhToan.setText(
                    startDate.get(Calendar.DAY_OF_MONTH).toString()
                )
            } else {
                endDate = selectedDate
                binding.tvEndDate.text =
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(endDate.time)
                // Kiểm tra nếu ngày kết thúc nhỏ hơn ngày bắt đầu
                if (endDate.before(startDate)) {
                    Toast.makeText(
                        requireActivity(),
                        "Ngày kết thúc không được nhỏ hơn ngày bắt đầu",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Reset lại ngày kết thúc
                    endDate = Calendar.getInstance().apply { time = startDate.time }
                    binding.tvEndDate.text =
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(endDate.time)
                } else {
                    // Cập nhật số tháng nếu ngày kết thúc hợp lệ
                    DateUtils.updateMonthsBasedOnDates(startDate, endDate, binding.tvSoThang)
                }
            }

            // Ẩn CalendarView sau khi chọn
            calendarView.visibility = View.GONE
        }
        // Khi nhấn vào TextView số tháng
        binding.tvSoThang.setOnClickListener {
            DateUtils.showMonthPicker(
                requireActivity() as AppCompatActivity,
                binding.tvSoThang,
                startDate,
                binding.tvEndDate
            )
        }


        // Quan sát lỗi
        viewModelCccd.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireActivity(), it, Toast.LENGTH_SHORT).show()
            }
        }
        viewModelCccd.cccdData.observe(this) { data ->
            // Hiển thị dữ liệu CCCD
            data?.let {
                Log.d("CCCD Data", it.toString())
                // Hiển thị dữ liệu CCCD lên các TextView
                // Hiển thị dữ liệu CCCD lên các TextView
                binding.txtSoCCD.text = "Số CCCD: ${it["So_cccd"] ?: "Không có"}"
                binding.txtHoTenCCCD.text = "Họ và tên: ${it["Hovaten"] ?: "Không có"}"
                binding.txtNgaySinhCCCD.text = "Ngày sinh: ${it["Ngaysinh"] ?: "Không có"}"
                binding.txtDiaChiCCCD.text = "Địa chỉ: ${it["Điachi"] ?: "Không có"}"
                binding.txtNgayCapCCCD.text = "Ngày cấp: ${it["Ngaycap"] ?: "Không có"}"

            }
        }
        viewModelCccd.cccdDataNT.observe(this) { data ->
            // Hiển thị dữ liệu CCCD
            data?.let {
                Log.d("CCCD Data", it.toString())
                // Hiển thị dữ liệu CCCD lên các TextView
                // Hiển thị dữ liệu CCCD lên các TextView
                binding.txtSoCCDNT.text = "Số CCCD: ${it["So_cccd"] ?: "Không có"}"
                binding.txtHoTenCCCDNT.text = "Họ và tên: ${it["Hovaten"] ?: "Không có"}"
                binding.txtNgaySinhCCCDNT.text = "Ngày sinh: ${it["Ngaysinh"] ?: "Không có"}"
                binding.txtDiaChiCCCDNT.text = "Địa chỉ: ${it["Điachi"] ?: "Không có"}"
                binding.txtNgayCapCCCDNT.text = "Ngày cấp: ${it["Ngaycap"] ?: "Không có"}"

            }
        }


        //sử lý text DieuKhoan
        binding.editorDieuKhoan.setEditorFontSize(16) // Kích thước font chữ
        binding.editorDieuKhoan.setEditorFontColor(
            resources.getColor(
                android.R.color.black
            )
        ) // Màu chữ
        binding.editorDieuKhoan.setEditorBackgroundColor(
            resources.getColor(
                android.R.color.white
            )
        ) // Màu nền
        binding.editorDieuKhoan.setPlaceholder("Nhập quy định hợp đồng...") // Gợi ý khi chưa có nội dung

        // Xử lý nút "Bullet" (ký tự đầu dòng)
        binding.btnBullet.setOnClickListener {
            binding.editorDieuKhoan.setBullets()
        }

        //back button
        binding.btnBack.setOnClickListener {
            dismiss()
        }
        // Lưu hợp đồng
        binding.btnXacNhan.setOnClickListener {
            if (validateContract()) {
                loadingUtil.show()
                updateThongTinHopDong()
            }
        }

        //ấn vào phòng trọ
        binding.cardViewPhongTro.setOnClickListener {
            val intent = Intent(requireActivity(), RoomDetailActivity::class.java)
            intent.putExtra("maPhongTro", maPhongTro)
            startActivity(intent)
        }
        binding.btnCloseDialog.setOnClickListener {
            dismiss()
        }


        return binding.root
    }

    private fun updateThongTinHopDong() {
        val utilityFees = viewModel.phiDichVuList.value?.map { phiDichVu ->
            UtilityFee(
                tenDichVu = phiDichVu.tenDichVu,
                giaTien = phiDichVu.soTien,
                donVi = phiDichVu.donVi,
                batBuoc = true // Có thể thêm trường này vào model PhiDichVu nếu cần
            )
        } ?: emptyList()

        val (totalFee, feeDetails) = viewModelHopDong.extractFixedFees(
            utilityFees,
            binding.editTextSoNguoi.text.toString().toInt()
        )
        hopDongRef.document(contract.maHopDong).update(
            "dieuKhoan", binding.editorDieuKhoan.html.toString(),
            "ghiChu", binding.note.text.toString(),
            "hoaDonHopDong.kyHoaDon", binding.tvEndDate.text.toString(),
            "hoaDonHopDong.ngayLap", binding.tvStartDate.text.toString(),
            "hoaDonHopDong.soDienCu", binding.editTextSoDien.text.toString().toIntOrNull() ?: 0,
            "hoaDonHopDong.tongTienDichVu", totalFee,
            "ngayBatDau", binding.tvStartDate.text.toString(),
            "ngayKetThuc", binding.tvEndDate.text.toString(),
            "ngayTao", binding.tvStartDate.text.toString(),
            "ngayThanhToan", binding.editTextNgayThanhToan.text.toString().toIntOrNull() ?: 0,
            "soDienCu", binding.editTextSoDien.text.toString().toIntOrNull() ?: 0,
            "soNguoiO", binding.editTextSoNguoi.text.toString().toIntOrNull() ?: 0,
            "soNuocCu", binding.editTextSoNuoc.text.toString().toIntOrNull() ?: 0,
            "thoiHanThue", binding.tvSoThang.text.toString(),
            "thongTinTaiChinh.soDienht", binding.editTextSoDien.text.toString().toIntOrNull() ?: 0,
            "thongTinTaiChinh.soNguoio", binding.editTextSoNguoi.text.toString().toIntOrNull() ?: 0,
            "thongTinTaiChinh.soNuocht", binding.editTextSoNuoc.text.toString().toIntOrNull() ?: 0,
        )
            .addOnSuccessListener {
                Log.d("TAGzzz", "updateThongTinHopDong: update thanh cong")
                val notificationModel = NotificationModel(
                    tieuDe = "Hợp đồng mới được sửa từ chủ trọ",
                    tinNhan = "Bạn có một hợp đồng mới đã sửa",
                    ngayGuiThongBao = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
                        Date()
                    ),
                    thoiGian = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                    loaiThongBao = "UpdateHopDong",
                    mapLink = null,
                    thoiGianGuiThongBao = System.currentTimeMillis(),
                    idModel = contract.maHopDong,
                )
                viewModelNotification.sendNotification(notificationModel, maNguoiThue)
                // Đóng màn hình hiện tại sau khi chuyển màn
                toast("Update thành công chờ người thuê xác nhận")
                dismiss()
            }
            .addOnFailureListener {
                Log.e("TAGzzz", "updateThongTinHopDong: update that bai")
            }
            .addOnCompleteListener {
                Log.d("TAGzzz", "updateThongTinHopDong: done update hợp đồng")
                loadingUtil.hide()
            }
    }

    private fun updateUI(contract: HopDong) {
        binding.tvSoThang.text = contract.thoiHanThue
        binding.tvStartDate.text = contract.ngayBatDau
        binding.tvEndDate.text = contract.ngayKetThuc
        binding.editTextNgayThanhToan.setText(contract.ngayThanhToan.toString())
        binding.editTextSoDien.setText(contract.soDienCu.toString())
        binding.editTextSoNguoi.setText(contract.soNguoiO.toString())
        binding.editorDieuKhoan.html = contract.dieuKhoan
        binding.note.setText(contract.ghiChu)
    }

    //hàm lưu hợp đồng

    private fun observeViewModel() {
        //        Quan sat chi tiet thong tin
        viewModel.chiTietList.observe(this) { chiTietList ->
            chiTietAdapter = ChiTietThongTinAdapter(chiTietList)
            binding.listViewThongTin.adapter = chiTietAdapter
            if (chiTietList.size > 3) {
                binding.txtDienTich.text = "${chiTietList[3].soLuongDonVi} m²"
            } else {
                binding.txtDienTich.text = "Không có dữ liệu"
            }

        }
        //        Quan sat du lieu phi dich vu
        viewModel.phiDichVuList.observe(this) { phiDichVuList ->
            phiDichVuAdapter = PhiDichVuAdapter(phiDichVuList)
            binding.recyclerViewPhiDichVu.adapter = phiDichVuAdapter
        }

//        Quan sat du lieu noi that
        viewModel.noiThatList.observe(this) { noiThatList ->
            noiThatAdapter = NoiThatAdapter(noiThatList)
            binding.recyclerViewNoiThat.adapter = noiThatAdapter
        }

//        Quan sat du lieu tien nghi
        viewModel.tienNghiList.observe(this) { tienNghiList ->
            tienNghiAdapter = TienNghiAdapter(tienNghiList)
            binding.recyclerViewTienNghi.adapter = tienNghiAdapter
        }

    }

    private fun validateContract(): Boolean {
        //nếu ngày không phải là ngày trong một tháng
        if (binding.editTextNgayThanhToan.text.toString()
                .toInt() > 30 || binding.editTextNgayThanhToan.text.toString()
                .toInt() < 1
        ) {
            toast("Ngày thanh toán không hợp lệ")
            return false
        }
        if (binding.editTextSoNguoi.text.toString().isEmpty()) {
            toast("Vui lòng nhập đầy đủ thông tin")
            return false
        }

        // Thêm các validation khác
        return true
    }

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

    // Hàm chuyển đổi trạng thái hiển thị CalendarView
    private fun toggleCalendarVisibility() {
        if (calendarView.visibility == View.GONE) {
            calendarView.visibility = View.VISIBLE
        } else {
            calendarView.visibility = View.GONE
        }
    }

    private fun setupRecyclerViewThongTinChiTiet() {
        binding.listViewThongTin.apply {
            layoutManager = GridLayoutManager(context, 4)
            adapter = chiTietAdapter

            // Thêm SpacingItemDecoration để tạo khoảng cách đều giữa các item
            addItemDecoration(SpacingItemDecoration(10))  // 16dp là khoảng cách giữa các item
        }
    }

    //    Danh sach tien nghi
    private fun setupRecyViewTienNghi() {
        binding.recyclerViewTienNghi.apply {
            layoutManager = GridLayoutManager(context, 4)
            adapter = tienNghiAdapter

            addItemDecoration(SpacingItemDecoration(4))
        }
    }

    //    Danh sach noi that
    private fun setupRecyclerViewNoiThat() {
        binding.recyclerViewNoiThat.apply {
            layoutManager = GridLayoutManager(context, 4)
            adapter = noiThatAdapter

            addItemDecoration(SpacingItemDecoration(16))
        }
    }

    //    danh sach phi dich vu
    private fun setupListPhiDichVu() {
        binding.recyclerViewPhiDichVu.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = phiDichVuAdapter

//           addItemDecoration(SpacingItemDecoration(1))
        }
    }


    override fun onStart() {
        super.onStart()

        val dialog = dialog as BottomSheetDialog
        val bottomSheet =
            dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)

        // Đảm bảo BottomSheet được mở toàn màn hình
        val behavior = BottomSheetBehavior.from(bottomSheet!!)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isHideable = false  // Không cho phép ẩn

        // Thay đổi chiều cao và chiều rộng của BottomSheet
        val params = bottomSheet.layoutParams
        params.height = ViewGroup.LayoutParams.MATCH_PARENT // Chiếm toàn bộ chiều cao
        params.width = ViewGroup.LayoutParams.MATCH_PARENT  // Chiếm toàn bộ chiều rộng

        bottomSheet.layoutParams = params

        // Cho phép kéo lên xuống
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
    }
}

