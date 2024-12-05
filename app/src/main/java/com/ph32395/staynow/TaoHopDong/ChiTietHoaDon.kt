package com.ph32395.staynow.TaoHopDong

import ContractViewModel
import android.content.Intent

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ph32395.staynow.MainActivity
import com.ph32395.staynow.TaoHopDong.Adapter.FixedFeeAdapter
import com.ph32395.staynow.TaoHopDong.Adapter.VariableFeeAdapter
import com.ph32395.staynow.databinding.ActivityChiTietHoaDonBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class ChiTietHoaDon : AppCompatActivity() {

    //khai báo intent để lấy thông tin hóa đơn


    private lateinit var binding: ActivityChiTietHoaDonBinding

    private val viewModelHopDong: ContractViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChiTietHoaDonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idHopDong = intent.getStringExtra("CONTRACT_ID")

        idHopDong?.let {
            viewModelHopDong.fetchInvoiceDetails(it)
        }

        // Quan sát dữ liệu và cập nhật UI
        viewModelHopDong.invoiceDetails.observe(this) { invoice ->
            updateUI(invoice)
        }

        // Sự kiện quay lại
        binding.btnBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        binding.btnContract.setOnClickListener {
            val intent = Intent(this, ChiTietHopDong::class.java)
            intent.putExtra("CONTRACT_ID", idHopDong)
            startActivity(intent)
        }

    }

    private fun updateUI(invoice: Invoice) {
        // Thông tin chung hóa đơn
        binding.tvInvoiceId.text = "Mã hóa đơn: ${invoice.idHoaDon}"
        binding.tvRoomName.text = invoice.tenPhong
        binding.tvInvoiceDate.text = "Ngày tạo hóa đơn: ${invoice.ngayLap}"
        binding.tvInvoicePeriod.text = "Thời hạn thuê:${calculateRentalPeriod(invoice.ngayLap,invoice.kyHoaDon)}, tính từ ngày${invoice.ngayLap} đến hết ngày ${invoice.kyHoaDon}"
        binding.tvReminderDate.text = "Các chi phí dịch vụ cố định và phí biến động sẽ được tính vào hóa đơn hàng tháng. Chúng tôi sẽ nhắc nhở bạn vào ngày ${invoice.ngayThanhToan} tháng sau."
        binding.tvTotal.text = formatCurrency(invoice.tongTien)
        binding.tvRoomPrice.text = formatCurrency(invoice.tienPhong)
        binding.tvRoomDeposit.text = formatCurrency(invoice.tienCoc)

        // Phí cố định
        val fixedFeeAdapter = FixedFeeAdapter(invoice.phiCoDinh)
        binding.rcvFixedFees.adapter = fixedFeeAdapter
        binding.rcvFixedFees.layoutManager = LinearLayoutManager(this)

        // Phí biến động
        val variableFeeAdapter = VariableFeeAdapter(invoice.phiBienDong)
        binding.rcvVariableFees.adapter = variableFeeAdapter
        binding.rcvVariableFees.layoutManager = LinearLayoutManager(this)
    }

    // Định dạng tiền tệ
    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return formatter.format(amount)
    }
    fun calculateRentalPeriod(startDate: String, endDate: String): String {
        // Định dạng ngày
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return try {
            val start = dateFormat.parse(startDate)
            val end = dateFormat.parse(endDate)

            if (start != null && end != null) {
                // Tính số ngày giữa hai mốc thời gian
                val diffInMillis = end.time - start.time
                val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)

                // Đổi ngày thành tháng và ngày (nếu cần)
                val months = diffInDays / 30
                val days = diffInDays % 30

                // Tạo chuỗi kết quả
                when {
                    months > 0 && days > 0 -> "$months tháng $days ngày"
                    months > 0 -> "$months tháng"
                    else -> "$days ngày"
                }
            } else {
                "Dữ liệu ngày không hợp lệ"
            }
        } catch (e: Exception) {
            "Lỗi: ${e.message}"
        }
    }
}
