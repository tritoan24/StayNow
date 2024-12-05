package com.ph32395.staynow.TaoHoaDon

import ContractViewModel
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow.TaoHopDong.Adapter.FixedFeeAdapter
import com.ph32395.staynow.TaoHopDong.Invoice
import com.ph32395.staynow.databinding.ActivityCreateMontlyInvoiceAutoBinding
import java.text.NumberFormat
import java.util.Locale

class CreateInvoice : AppCompatActivity() {
    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var binding: ActivityCreateMontlyInvoiceAutoBinding

    private val viewModelHopDong: ContractViewModel by viewModels()
    private val invoiceViewModel: InvoiceViewModel by viewModels()

    // Khai báo invoice là một thuộc tính của class
    private lateinit var invoice: Invoice


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
            // Gán giá trị cho invoice khi nhận được dữ liệu
            invoice = fetchedInvoice
            updateUI(invoice)
        }
    }

    private fun setupAdditionalFeeListeners() {
        // Mặc định giá trị ban đầu của tiền thêm và tiền giảm là 0
        binding.editPhiThem.setText("0")
        binding.editTienGiam.setText("0")

        // Thêm TextWatcher để theo dõi sự thay đổi của tiền thêm
        binding.editPhiThem.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Lấy giá trị mới nhập vào, nếu rỗng thì mặc định là 0
                val tienThem = s.toString().toDoubleOrNull() ?: 0.0

                // Cập nhật lại tổng hóa đơn
                updateTotalBill(tienThem, getDiscountAmount())
            }
        })

        // Tương tự cho tiền giảm
        binding.editTienGiam.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Lấy giá trị mới nhập vào, nếu rỗng thì mặc định là 0
                val tienGiam = s.toString().toDoubleOrNull() ?: 0.0

                // Cập nhật lại tổng hóa đơn
                updateTotalBill(getAdditionalAmount(), tienGiam)
            }
        })
    }
    private fun getAdditionalAmount(): Double {
        return binding.editPhiThem.text.toString().toDoubleOrNull() ?: 0.0
    }

    private fun getDiscountAmount(): Double {
        return binding.editTienGiam.text.toString().toDoubleOrNull() ?: 0.0
    }

    private fun updateTotalBill(tienThem: Double, tienGiam: Double) {
        // Tìm các dịch vụ điện và nước
        val electricityService = invoice.phiBienDong.find { it.tenDichVu.lowercase() == "điện" }
        val waterService = invoice.phiBienDong.find { it.tenDichVu.lowercase() == "nước" }

        // Tính toán phí điện và nước
        val electricityFee = if (electricityService != null) {
            // Tìm giá trị phí điện từ calculatedFees nếu có
            // Bạn có thể điều chỉnh logic này phù hợp với cách tính phí của bạn
            invoice.phiBienDong
                .filter { it.tenDichVu.lowercase() == "điện" }
                .map { it.giaTien }
                .firstOrNull() ?: 0.0
        } else 0.0

        val waterFee = if (waterService != null) {
            // Tương tự với phí nước
            invoice.phiBienDong
                .filter { it.tenDichVu.lowercase() == "nước" }
                .map { it.giaTien }
                .firstOrNull() ?: 0.0
        } else 0.0

        // Tính toán tổng hóa đơn ban đầu (không bao gồm tiền thêm và tiền giảm)
        val baseTotal = invoice.tongTienDichVu +
                invoice.tienPhong +
                invoice.tienCoc +
                electricityFee +
                waterFee

        // Áp dụng tiền thêm và tiền giảm
        val finalTotal = baseTotal + tienThem - tienGiam

        // Cập nhật TextView tổng hóa đơn
        binding.tvTongHoaDon.text = formatCurrency(finalTotal)
        binding.tvTienThem.text = formatCurrency(tienThem)
        binding.tvTienGiam.text = formatCurrency(tienGiam)
    }

    private fun updateUI(invoice: Invoice) {

        setupAdditionalFeeListeners()
        //tienphong
        binding.tvTienPhong.text = formatCurrency(invoice.tienPhong)
        binding.tvTienCoc.text = formatCurrency(invoice.tienCoc)

        var tienThem = binding.editPhiThem.text.toString().toDoubleOrNull() ?: 0.0
        var tienGiam = binding.editTienGiam.text.toString().toDoubleOrNull() ?: 0.0
        var ghiChu = binding.editTextNotes.text.toString()

        // Fixed fees
        val fixedFeeAdapter = FixedFeeAdapter(invoice.phiCoDinh)
        binding.rcvFixedFees.adapter = fixedFeeAdapter
        binding.rcvFixedFees.layoutManager = LinearLayoutManager(this)

        // Set initial values with null safety
        val soDienCu = invoice.soDienCu ?: 0
        val soNuocCu = invoice.soNuocCu ?: 0

        // Kiểm tra và ẩn hiện EditText dựa trên tồn tại của dịch vụ
        val electricityService = invoice.phiBienDong.find { it.tenDichVu.lowercase() == "điện" }
        val waterService = invoice.phiBienDong.find { it.tenDichVu.lowercase() == "nước" }

        // Ẩn hiện EditText điện
        binding.LnSoDien.visibility = if (electricityService != null) View.VISIBLE else View.GONE

        // Ẩn hiện EditText nước
        binding.LnsoNuoc.visibility = if (waterService != null) View.VISIBLE else View.GONE

        // Set old meter readings (chỉ khi dịch vụ tồn tại)
        if (electricityService != null) {
            binding.editTextSoDienCu.setText(soDienCu.toString())
        }

        if (waterService != null) {
            binding.editTextSoNuocCu.setText(soNuocCu.toString())
        }






        //btn Cancel
        binding.btnCancel.setOnClickListener {
           finish()
        }
        // Collect data from UI
        val invoice = InvoiceMonthlyModel(
            tienPhong = invoice.tienPhong, // Room price from existing invoice
            tienCoc = invoice.tienCoc, // Deposit from existing invoice
            soDienCu = binding.editTextSoDienCu.text.toString().toIntOrNull() ?: 0,
            soNuocCu = binding.editTextSoNuocCu.text.toString().toIntOrNull() ?: 0,
//            soDienMoi = binding.editTextSoDienMoi.text.toString().toIntOrNull() ?: 0,
//            soNuocMoi = binding.editTextSoNuocMoi.text.toString().toIntOrNull() ?: 0,
//            phiThem = binding.editPhiThem.text.toString().toDoubleOrNull() ?: 0.0,
//            tienGiam = binding.editTienGiam.text.toString().toDoubleOrNull() ?: 0.0,
//            ghiChu = binding.editTextNotes.text.toString(),
//            tongTienDichVu = invoice.tongTienDichVu, // Total service fees
//            tongTienPhiBienDoi = binding.tvTongTienPhiBienDoi.text.toString().toDoubleOrNull() ?: 0.0,
//            tongHoaDon = binding.tvTongHoaDon.text.toString().toDoubleOrNull() ?: 0.0,
            phiBienDong = invoice.phiBienDong, // Variable fees
            phiCoDinh = invoice.phiCoDinh, // Fixed fees
            tienThem = tienThem,
            tienGiam = tienGiam

        )

// Save invoice

        binding.btnCreate.setOnClickListener {
            invoiceViewModel.addInvoice(invoice, {
                Toast.makeText(this, "Invoice created successfully", Toast.LENGTH_SHORT).show()
                finish()
            }, {
                Toast.makeText(this, "Error creating invoice", Toast.LENGTH_SHORT).show()
            })
        }

        val variableFeesAdapter = InvoiceVariableFeesAdapter(invoice.phiBienDong)
        variableFeesAdapter.calculationListener = object : InvoiceVariableFeesAdapter.OnCalculationCompleteListener {
            override fun onCalculationComplete(calculatedFees: List<Double>) {
                // Xử lý thành tiền ở đây
                val totalFee = calculatedFees.sum()

                // Nếu có 2 dịch vụ (điện và nước)
                if (calculatedFees.size >= 2) {
                    val electricityFee = calculatedFees[0]
                    val waterFee = calculatedFees[1]

                    // Tính toán tổng tiền
                    binding.tvTongTienPhiCoDinh.text = formatCurrency(invoice.tongTienDichVu)
                    binding.tvTongTienPhiBienDoi.text = formatCurrency(totalFee)
                    val totalFee = invoice.tongTienDichVu + waterFee + electricityFee
                    binding.tvTongTien.text = formatCurrency(totalFee)
                    val totalBill = totalFee + invoice.tienPhong + invoice.tienCoc
                    binding.tvTongHoaDon.text = formatCurrency(totalBill)
                    binding.tvTienThem.text = formatCurrency(tienThem)
                    binding.tvTienGiam.text = formatCurrency(tienGiam)
                    if (tienGiam > 0) {
                        binding.tvTongHoaDon.text = formatCurrency(totalBill - tienGiam)
                    }else if(tienThem > 0){
                        binding.tvTongHoaDon.text = formatCurrency(totalBill + tienThem)
                    }
                    else{
                        binding.tvTongHoaDon.text = formatCurrency(totalBill)
                    }
                }
                // Nếu chỉ có 1 dịch vụ
                else if (calculatedFees.isNotEmpty()) {
                    val singleFee = calculatedFees[0]
                    // Tính toán tổng tiền
                    binding.tvTongTienPhiCoDinh.text = formatCurrency(invoice.tongTienDichVu)
                    binding.tvTongTienPhiBienDoi.text = formatCurrency(totalFee)
                    val totalFee = invoice.tongTienDichVu + singleFee
                    binding.tvTongTien.text = formatCurrency(totalFee)
                    val totalBill = totalFee + invoice.tienPhong + invoice.tienCoc
                    binding.tvTongHoaDon.text = formatCurrency(totalBill)
                    binding.tvTienThem.text = formatCurrency(tienThem)
                    binding.tvTienGiam.text = formatCurrency(tienGiam)
                    if (tienGiam > 0) {
                        binding.tvTongHoaDon.text = formatCurrency(totalBill - tienGiam)
                    }else if(tienThem > 0){
                        binding.tvTongHoaDon.text = formatCurrency(totalBill + tienThem)
                    }
                    else{
                        binding.tvTongHoaDon.text = formatCurrency(totalBill)
                    }
                }
            }
        }

        binding.rcvVariableFees.adapter = variableFeesAdapter
        binding.rcvVariableFees.layoutManager = LinearLayoutManager(this)

        // Tạo một hàm chung để tính toán và cập nhật
        fun calculateAndUpdateUtilities() {
            try {
                val soDienCu = if (electricityService != null)
                    binding.editTextSoDienCu.text.toString().toIntOrNull() ?: 0
                else 0

                val soNuocCu = if (waterService != null)
                    binding.editTextSoNuocCu.text.toString().toIntOrNull() ?: 0
                else 0

                val soDienMoi = if (electricityService != null)
                    binding.editTextSoDienMoi.text.toString().toIntOrNull() ?: 0
                else 0

                val soNuocMoi = if (waterService != null)
                    binding.editTextSoNuocMoi.text.toString().toIntOrNull() ?: 0
                else 0

                val dienTieuThu = maxOf(soDienMoi - soDienCu, 0)
                val nuocTieuThu = maxOf(soNuocMoi - soNuocCu, 0)

                binding.editSoDien.setText(dienTieuThu.toString())
                binding.editSoNuoc.setText(nuocTieuThu.toString())

                // Log để kiểm tra
                Log.d("UpdateUI", "Electricity consumption: $dienTieuThu, Water consumption: $nuocTieuThu")

                // Cập nhật số lượng cho adapter
                variableFeesAdapter.updateQuantities(
                    electricityQuantity = dienTieuThu.toDouble(),
                    waterQuantity = nuocTieuThu.toDouble()
                )

            } catch (e: Exception) {
                Log.e("UpdateUI", "Error calculating utilities", e)
            }
        }

        // Thêm text change listener cho các EditText (chỉ khi dịch vụ tồn tại)
        if (electricityService != null) {
            binding.editTextSoDienCu.addTextChangedListener { calculateAndUpdateUtilities() }
            binding.editTextSoDienMoi.addTextChangedListener { calculateAndUpdateUtilities() }
        }

        if (waterService != null) {
            binding.editTextSoNuocCu.addTextChangedListener { calculateAndUpdateUtilities() }
            binding.editTextSoNuocMoi.addTextChangedListener { calculateAndUpdateUtilities() }
        }

        // Gọi lần đầu để tính toán ban đầu
        calculateAndUpdateUtilities()

    }
    private fun formatCurrency(amount: Number): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return formatter.format(amount)
    }

}