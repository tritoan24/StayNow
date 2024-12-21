package com.ph32395.staynow_datn.TaoHoaDon

import android.annotation.SuppressLint
import com.ph32395.staynow_datn.TaoHopDong.ContractViewModel
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.ph32395.staynow_datn.ChucNangChung.CurrencyFormatTextWatcher
import com.ph32395.staynow_datn.ChucNangChung.LoadingUtil
import com.ph32395.staynow_datn.TaoHopDong.Adapter.FixedFeeAdapter
import com.ph32395.staynow_datn.TaoHopDong.Invoice
import com.ph32395.staynow_datn.TaoHopDong.InvoiceStatus
import com.ph32395.staynow_datn.TaoHopDong.UtilityFeeDetail
import com.ph32395.staynow_datn.databinding.ActivityCreateMontlyInvoiceAutoBinding
import com.ph32395.staynow_datn.hieunt.model.NotificationModel
import com.ph32395.staynow_datn.hieunt.view_model.NotificationViewModel
import com.ph32395.staynow_datn.hieunt.view_model.ViewModelFactory
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class CreateInvoice : AppCompatActivity() {
    private lateinit var binding: ActivityCreateMontlyInvoiceAutoBinding
    private val viewModelHopDong: ContractViewModel by viewModels()
    private val invoiceViewModel: InvoiceViewModel by viewModels()

    private val utilityFeeDetails = mutableListOf<UtilityFeeDetail>()
    private lateinit var invoice: Invoice

    private var slDienTieuThu = 0
    private var slNuocTieuThu = 0
    private var tienThem = 0.0
    private var tienGiam = 0.0
    private var tienPhong = 0.0
    private var tongTienDichVuCoDinh = 0.0
    private var tongTienPhiBienDong = 0.0
    private var tongPhiDichVu = 0.0
    private var tongTienHoaDon = 0.0

    private var idNguoiNhan: String = ""
    private var idNguoiGui: String = ""
    private var idHopDong: String = ""
    private var tenKhachHang: String = ""
    private var tenPhong: String = ""
    private var tienCoc: Double = 0.0
    private val phiCoDinhList = mutableListOf<UtilityFeeDetail>()

    private lateinit var loadingUtil: LoadingUtil


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCreateMontlyInvoiceAutoBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val idHopDong = intent.getStringExtra("CONTRACT_ID")

        idHopDong?.let {
            viewModelHopDong.fetchInvoiceDetails(it)
        }

        // Quan sát dữ liệu và cập nhật UI
        viewModelHopDong.invoiceDetails.observe(this) { fetchedInvoice ->
            invoice = fetchedInvoice
            updateUI(invoice)
            setupCalculationListeners()
        }
        if (idHopDong != null) {
            viewModelHopDong.fetchPreviousUtilities(idHopDong)
        }
        viewModelHopDong.previousUtilities.observe(this) { utilities ->
            binding.editTextSoDienCu.setText(utilities.first?.toString() ?: "0")
            binding.editTextSoNuocCu.setText(utilities.second?.toString() ?: "0")
        }
        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    // Thêm biến flag để kiểm soát việc lưu dữ liệu
    private var isFinalCalculation = false

    private fun updateUI(invoice: Invoice) {

        // Tính toán các giá trị ban đầu
        tienPhong = invoice.tienPhong
        tongTienDichVuCoDinh = invoice.tongTienDichVu
        tongTienPhiBienDong = 0.0
        tongPhiDichVu = tongTienDichVuCoDinh + tongTienPhiBienDong
        tongTienHoaDon = tienPhong + tongPhiDichVu

        loadingUtil = LoadingUtil(this)

        // Lấy thông tin người nhận và người gửi
        idNguoiNhan = invoice.idNguoinhan
        idNguoiGui = invoice.idNguoigui
        tenKhachHang = invoice.tenKhachHang
        tenPhong = invoice.tenPhong
        tienCoc = invoice.tienCoc
        phiCoDinhList.addAll(invoice.phiCoDinh)

        // Cập nhật các TextView với giá trị ban đầu
        binding.tvTienPhong.text = formatCurrency(tienPhong)
        binding.tvTongTienPhiCoDinh.text = formatCurrency(tongTienDichVuCoDinh)
        binding.tvTongTienPhiBienDoi.text = formatCurrency(tongTienPhiBienDong)
        binding.tvTongTien.text = formatCurrency(tongPhiDichVu)
        binding.tvTongHoaDon.text = formatCurrency(tongTienHoaDon)

        binding.tvTienThem.text = formatCurrency(tienThem)
        binding.tvTienGiam.text = formatCurrency(tienGiam)


        Log.d("Invoice", "tien phong: " + tienPhong)
        Log.d("Invoice", "phi co dinh: " + tongTienDichVuCoDinh)
        Log.d("Invoice", "tong tien phi bien dong: " + tongTienPhiBienDong)
        Log.d("Invoice", "tong phi dich vu: " + tongPhiDichVu)
        Log.d("Invoice", "tong tien hoa don: " + tongTienHoaDon)

        // Thiết lập adapter cho các phí cố định và biến động
        val fixedFeeAdapter = FixedFeeAdapter(invoice.phiCoDinh)
        binding.rcvFixedFees.adapter = fixedFeeAdapter
        binding.rcvFixedFees.layoutManager = LinearLayoutManager(this)

        val variableFeesAdapter = InvoiceVariableFeesAdapter(invoice.phiBienDong)
        binding.rcvVariableFees.adapter = variableFeesAdapter
        binding.rcvVariableFees.layoutManager = LinearLayoutManager(this)

        // Xử lý hiển thị và ẩn các EditText dựa trên dịch vụ
        val electricityService = invoice.phiBienDong.find { it.tenDichVu.lowercase() == "điện" }
        val waterService = invoice.phiBienDong.find { it.tenDichVu.lowercase() == "nước" }

        binding.LnSoDien.visibility = if (electricityService != null) View.VISIBLE else View.GONE
        binding.LnsoNuoc.visibility = if (waterService != null) View.VISIBLE else View.GONE


        // Set sự kiện click cho txtThang
        binding.txtThang.setOnClickListener {
            showMonthPicker()
        }


        // Lấy tháng hiện tại và đặt làm giá trị mặc định cho TextView
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        binding.txtThang.text = currentMonth.toString()

        // Thêm nút xác nhận lưu hóa đơn
        binding.btnCreate.setOnClickListener {
            // Đánh dấu là tính toán cuối cùng
            isFinalCalculation = true

            // Thực hiện tính toán cuối cùng
            calculateUtilities()
            updateTotalBill()

            val soDienCu =   binding.editTextSoNuocCu.text.toString().toInt()
            if(soDienCu != invoice.soDienCu){
              Toast.makeText(this, "Số điện cũ đã bị thay đổi", Toast.LENGTH_SHORT).show()
            }
            loadingUtil.show()
            saveInvoice()
        }

        // định dạng số tiền nhập vào
        setupCalculationListeners()
        CurrencyFormatTextWatcher.addTo(binding.editTienGiam) {
            updateTotalBill()
        }
        CurrencyFormatTextWatcher.addTo(binding.editPhiThem) {
            updateTotalBill()
        }
    }

    private fun setupCalculationListeners() {
        val calculationTriggers = listOf(
            binding.editTextSoDienCu,
            binding.editTextSoDienMoi,
            binding.editTextSoNuocCu,
            binding.editTextSoNuocMoi,
            binding.editPhiThem,
            binding.editTienGiam
        )

        calculationTriggers.forEach { editText ->
            editText.addTextChangedListener {
                calculateUtilities()
                updateTotalBill()
            }
        }
    }

    private fun calculateUtilities() {
        val soDienCu = binding.editTextSoDienCu.text.toString().toIntOrNull() ?: 0
        val soDienMoi = binding.editTextSoDienMoi.text.toString().toIntOrNull() ?: 0
        val soNuocCu = binding.editTextSoNuocCu.text.toString().toIntOrNull() ?: 0
        val soNuocMoi = binding.editTextSoNuocMoi.text.toString().toIntOrNull() ?: 0

        slDienTieuThu = maxOf(soDienMoi - soDienCu, 0)
        slNuocTieuThu = maxOf(soNuocMoi - soNuocCu, 0)

        binding.editSoDien.setText(slDienTieuThu.toString())
        binding.editSoNuoc.setText(slNuocTieuThu.toString())

        (binding.rcvVariableFees.adapter as? InvoiceVariableFeesAdapter)?.updateQuantities(
            electricityQuantity = slDienTieuThu.toDouble(),
            waterQuantity = slNuocTieuThu.toDouble()
        )
    }

    private fun calculateTotalBill(): Double {
        // Tính tổng phí biến động
        val calculatedFees = invoice.phiBienDong.mapIndexed { index, fee ->
            when (fee.tenDichVu.lowercase()) {
                "điện" -> fee.giaTien * slDienTieuThu
                "nước" -> fee.giaTien * slNuocTieuThu
                else -> fee.giaTien
            }
        }

        val totalVariableFees = calculatedFees.sum()

        // Lấy giá trị số thực
        tienThem = CurrencyFormatTextWatcher.getUnformattedValue(binding.editPhiThem)
        tienGiam = CurrencyFormatTextWatcher.getUnformattedValue(binding.editTienGiam)

        tongTienPhiBienDong = totalVariableFees
        tongTienDichVuCoDinh = invoice.tongTienDichVu
        tongPhiDichVu = tongTienDichVuCoDinh + tongTienPhiBienDong
        tongTienHoaDon = tienPhong + tongPhiDichVu + tienThem - tienGiam

        return tongTienHoaDon
    }

    private fun updateTotalBill() {
        // Nếu là tính toán cuối cùng, lưu chi tiết phí
        if (isFinalCalculation) {
            // Xóa danh sách chi tiết phí cũ
            utilityFeeDetails.clear()

            val calculatedFees = invoice.phiBienDong.mapIndexed { index, fee ->
                when (fee.tenDichVu.lowercase()) {
                    "điện" -> fee.giaTien * slDienTieuThu
                    "nước" -> fee.giaTien * slNuocTieuThu
                    else -> fee.giaTien
                }
            }

            // Nếu có 2 dịch vụ (điện và nước)
            if (calculatedFees.size >= 2) {
                val electricityFee = calculatedFees[0]
                val waterFee = calculatedFees[1]

                utilityFeeDetails.add(
                    UtilityFeeDetail(
                        tenDichVu = invoice.phiBienDong[0].tenDichVu,
                        giaTien = invoice.phiBienDong[0].giaTien,
                        donVi = invoice.phiBienDong[0].donVi,
                        soLuong = slDienTieuThu,
                        thanhTien = electricityFee
                    )
                )
                utilityFeeDetails.add(
                    UtilityFeeDetail(
                        tenDichVu = invoice.phiBienDong[1].tenDichVu,
                        giaTien = invoice.phiBienDong[1].giaTien,
                        donVi = invoice.phiBienDong[1].donVi,
                        soLuong = slNuocTieuThu,
                        thanhTien = waterFee
                    )
                )
            }
            // Nếu chỉ có 1 dịch vụ
            else if (calculatedFees.isNotEmpty()) {
                val singleFee = calculatedFees[0]
                val tenDichVu = invoice.phiBienDong[0].tenDichVu

                utilityFeeDetails.add(
                    UtilityFeeDetail(
                        tenDichVu = tenDichVu,
                        giaTien = invoice.phiBienDong[0].giaTien,
                        donVi = invoice.phiBienDong[0].donVi,
                        soLuong = if (tenDichVu.lowercase() == "điện") slDienTieuThu else slNuocTieuThu,
                        thanhTien = singleFee
                    )
                )
            }

            // Reset flag sau khi lưu
            isFinalCalculation = false
        }

        // Tính toán tổng hóa đơn
        val tongHoaDon = calculateTotalBill()

        // Cập nhật UI
        binding.tvTongTienPhiBienDoi.text = formatCurrency(tongTienPhiBienDong)
        binding.tvTongTienPhiCoDinh.text = formatCurrency(tongTienDichVuCoDinh)
        binding.tvTongTien.text = formatCurrency(tongPhiDichVu)
        binding.tvTongHoaDon.text = formatCurrency(tongHoaDon)
        binding.tvTienThem.text = formatCurrency(tienThem)
        binding.tvTienGiam.text = formatCurrency(tienGiam)

        // Log để kiểm tra
        Log.d("Invoice1", "Utility Fee Details: $utilityFeeDetails")
    }

    private fun saveInvoice() {
        // Phương thức lưu hóa đơn
        // Sử dụng utilityFeeDetails để lưu chi tiết phí
        // Triển khai logic lưu vào Firestore hoặc cơ sở dữ liệu của bạn
        val hoaDonMon = InvoiceMonthlyModel(
            idHoaDon = "",
            idNguoiNhan = idNguoiNhan,
            idNguoiGui = idNguoiGui,
            idHopDong = idHopDong,
            tenKhachHang = tenKhachHang,
            tenPhong = tenPhong,
            ngayTaoHoaDon = Calendar.getInstance().time.toString(),
            hoaDonThang = binding.txtThang.text.toString().toInt(),
            phiCoDinh = phiCoDinhList,
            phiBienDong = utilityFeeDetails,
            tongTien = tongTienHoaDon,
            trangThai = InvoiceStatus.PENDING,
            tienPhong = tienPhong,
            tienCoc = tienCoc,
            tongPhiCoDinh = tongTienDichVuCoDinh,
            tongPhiBienDong = tongTienPhiBienDong,
            tongTienDichVu = tongPhiDichVu,
            kieuHoadon = invoice.kieuHoadon,
            paymentDate = "Ngày thanh toán",
            soDienCu = binding.editTextSoDienCu.text.toString().toIntOrNull() ?: 0,
            soNuocCu = binding.editTextSoNuocCu.text.toString().toIntOrNull() ?: 0,
            soDienMoi = binding.editTextSoDienMoi.text.toString().toIntOrNull() ?: 0,
            soNuocMoi = binding.editTextSoNuocMoi.text.toString().toIntOrNull() ?: 0,
            soDienTieuThu = slDienTieuThu,
            soNuocTieuThu = slNuocTieuThu,
            tienGiam = tienGiam,
            tienThem = tienThem,
            ghiChu = binding.editTextNotes.text.toString()

            // Các trường khác
        )
        Log.d("Invoice2", "Invoice: $hoaDonMon")

        invoiceViewModel.addInvoice(hoaDonMon, {
            val factory = ViewModelFactory(applicationContext) // Hoặc context cần thiết
            val notificationViewModel = ViewModelProvider(this, factory).get(NotificationViewModel::class.java)
            Toast.makeText(this, "Tạo hóa đơn thành công", Toast.LENGTH_SHORT).show()
            loadingUtil.hide()
            //gọi hàm update số điện số nc
            viewModelHopDong.updatePreviousUtilities(
                idHopDong,
                binding.editTextSoDienMoi.text.toString().toInt(),
                binding.editTextSoNuocMoi.text.toString().toInt()
            )
            Log.d("Invoice", "idHopDong: $idHopDong")
            // Giám sát trạng thái gửi thông báo
            notificationViewModel.notificationStatus.observe(this, Observer { isSuccess ->
                if (isSuccess) {
                    // Thông báo thành công
                    Toast.makeText(this, "Thông báo đã được gửi!", Toast.LENGTH_SHORT).show()
                } else {
                    // Thông báo thất bại
                    Toast.makeText(this, "Gửi thông báo thất bại!", Toast.LENGTH_SHORT).show()
                }
            })

            val soDienCu =   binding.editTextSoDienMoi.text.toString().toInt()
            val soNuocCu =  binding.editTextSoNuocMoi.text.toString().toInt()
            val message = if (invoice.soDienCu != soDienCu || invoice.soNuocCu != soNuocCu) {
                "Đến ngày cần thanh toán hóa đơn cho tháng ${hoaDonMon.hoaDonThang}. Lưu ý: Đã có sự thay đổi về số điện hoặc số nước!"
            } else {
                "Đến ngày cần thanh toán hóa đơn cho tháng ${hoaDonMon.hoaDonThang}"
            }
            // Ví dụ: gửi thông báo
            val notification = NotificationModel(
                tieuDe = "Thanh toán hóa đơn",
                tinNhan = message,
                //lấy ngày hôm nay
                ngayGuiThongBao = Calendar.getInstance().time.toString(),
                thoiGian = "0",
                mapLink = null,
                daDoc = false,
                daGui = true,
                loaiThongBao = "invoiceRemind",
                idModel = idHopDong
            )

            val recipientId = idNguoiNhan
            notificationViewModel.sendNotification(notification, recipientId)

        }, {
            Toast.makeText(this, "Lỗi khi tạo hóa đơn", Toast.LENGTH_SHORT).show()
        })
    }

    private fun formatCurrency(amount: Number): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return formatter.format(amount)
    }

    @SuppressLint("CheckResult")
    private fun showMonthPicker() {
        // Tạo danh sách tháng từ 1 đến 12
        val months = (1..12).map { "Tháng $it" }
        // Hiển thị MaterialDialog
        MaterialDialog(this).show {
            title(text = "Chọn tháng") // Tiêu đề của dialog
            listItems(items = months) { _, index, _ ->
                // Xử lý khi người dùng chọn một tháng
                val selectedMonth = index + 1 // Vì danh sách bắt đầu từ 0, cộng thêm 1
                binding.txtThang.text = "$selectedMonth"
            }
        }
    }
}