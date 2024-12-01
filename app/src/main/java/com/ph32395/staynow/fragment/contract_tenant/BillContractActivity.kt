package com.ph32395.staynow.fragment.contract_tenant

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.ph32395.staynow.TaoHopDong.Adapter.FixedFeeAdapter
import com.ph32395.staynow.TaoHopDong.Adapter.VariableFeeAdapter
import com.ph32395.staynow.TaoHopDong.HopDong
import com.ph32395.staynow.TaoHopDong.Invoice
import com.ph32395.staynow.databinding.ActivityBillContractBinding
import com.ph32395.staynow.payment.OrderProcessor
import com.ph32395.staynow.payment.SocketManager
import com.ph32395.staynow.utils.Constants
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

//import vn.zalopay.sdk.Environment
//import vn.zalopay.sdk.ZaloPaySDK

@Suppress("DEPRECATION")
class BillContractActivity : AppCompatActivity() {

    // Đối tượng Binding
    private lateinit var binding: ActivityBillContractBinding
    private lateinit var socketManager: SocketManager

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBillContractBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo socket
        socketManager = SocketManager()
        socketManager.initSocket(Constants.URL_PAYMENT)
//        initZaloPay()

        // nhận intent từ contractAdapter
        val invoice = intent.getSerializableExtra("hoaDonHopDong") as? Invoice
        val contract = intent.getSerializableExtra("hopDong") as? HopDong

        // convert invoice to jsonArrStr
        val gson = Gson()
        val itemsArrStr = gson.toJson(listOf(invoice))

        // Quan sát dữ liệu và cập nhật UI
        if (invoice != null) {
            updateUI(invoice)
        } else {
            Toast.makeText(this, "Hóa đơn không tồn tại", Toast.LENGTH_SHORT).show()
        }


        binding.ivBack.setOnClickListener {
            onBackPressed()
            finish()
        }

        binding.btnThanhtoan.setOnClickListener {
            val orderProcessor = OrderProcessor(this)
            orderProcessor.checkAndCreateOrder(
                invoice!!.tongTien,
                contract!!.maHopDong,
                contract.hoaDonHopDong.idHoaDon,
                itemsArrStr
            ) { token ->
                if (token != null) {
//                    orderProcessor.startPayment(token, contract)
                } else {
                    Toast.makeText(this, "Lỗi khi tạo đơn hàng", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

//    private fun initZaloPay() {
//        StrictMode.ThreadPolicy.Builder().permitAll().build()
//            .also { StrictMode.setThreadPolicy(it) }
//        ZaloPaySDK.init(Constants.APP_ID, Environment.SANDBOX)
//    }
//
//    override fun onNewIntent(intent: Intent) {
//        super.onNewIntent(intent)
//        setIntent(intent)
//        ZaloPaySDK.getInstance().onResult(intent)
//    }

    override fun onDestroy() {
        super.onDestroy()
        socketManager.disconnect()
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
