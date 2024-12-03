package com.ph32395.staynow.fragment.contract_tenant

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.google.gson.Gson
import com.ph32395.staynow.Activity.ChoosePaymentActivity
import com.ph32395.staynow.TaoHopDong.Adapter.FixedFeeAdapter
import com.ph32395.staynow.TaoHopDong.Adapter.VariableFeeAdapter
import com.ph32395.staynow.TaoHopDong.HopDong
import com.ph32395.staynow.TaoHopDong.Invoice
import com.ph32395.staynow.databinding.ActivityBillContractBinding
import com.ph32395.staynow.hieunt.widget.tap
import com.ph32395.staynow.payment.OrderProcessor
import com.ph32395.staynow.utils.Constants
import vn.zalopay.sdk.Environment
import vn.zalopay.sdk.ZaloPaySDK
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

@Suppress("DEPRECATION")
class BillContractActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBillContractBinding
    private lateinit var loadingIndicator: LottieAnimationView

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBillContractBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingIndicator = binding.loadingIndicator

        initZaloPay()

        // nhận intent từ contractAdapter
        val invoice = intent.getSerializableExtra("hoaDonHopDong") as? Invoice
        val contract = intent.getSerializableExtra("hopDong") as? HopDong

        // convert invoice to jsonArrStr
        val gson = Gson()
        val itemsArrStr = gson.toJson(listOf(invoice))

        updateUI(invoice!!, contract!!)

        binding.ivBack.tap {
            onBackPressed()
            finish()
        }

        binding.btnThanhtoan.tap {
            showLoading()
            val orderProcessor = OrderProcessor(this)
            orderProcessor.checkAndCreateOrder(
                invoice.tongTien,
                contract.maHopDong,
                contract.hoaDonHopDong.idHoaDon,
                itemsArrStr
            ) { token, orderUrl,remainTime ->
                hideLoading()
                if (token != null && orderUrl != null) {
                    val intent = Intent(this, ChoosePaymentActivity::class.java)
                    intent.putExtra("contract", contract)
                    intent.putExtra("itemsArrStr", itemsArrStr)
                    intent.putExtra("zpToken", token)
                    intent.putExtra("orderUrl", orderUrl)
                    intent.putExtra("remainTime", remainTime)
                    Log.d("remainTimeBillContract", remainTime.toString())

                    startActivity(intent)

                } else {
                    Toast.makeText(this, "Lỗi khi tạo đơn hàng", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(invoice: Invoice, contract: HopDong) {
        // Thông tin chung hóa đơn
        binding.tvInvoiceId.text = "Mã hóa đơn: ${invoice.idHoaDon}"
        binding.tvRoomName.text = invoice.tenPhong
        binding.tvInvoiceDate.text = "Ngày tạo hóa đơn: ${invoice.ngayLap}"
        binding.tvInvoicePeriod.text = "Thời hạn thuê:${
            calculateRentalPeriod(
                invoice.ngayLap,
                invoice.kyHoaDon
            )
        }, tính từ ngày ${invoice.ngayLap} đến hết ngày ${invoice.kyHoaDon}"
        binding.tvReminderDate.text =
            "Các chi phí dịch vụ cố định và phí biến động sẽ được tính vào hóa đơn hàng tháng. Chúng tôi sẽ nhắc nhở bạn vào ngày ${contract.ngayThanhToan} tháng sau."
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

    private fun calculateRentalPeriod(startDate: String, endDate: String): String {
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


    private fun showLoading() {
        binding.container.setBackgroundColor(Color.parseColor("#A9A9A9"));
        loadingIndicator.visibility = View.VISIBLE
        loadingIndicator.playAnimation()
    }

    private fun hideLoading() {
        binding.container.setBackgroundColor(Color.TRANSPARENT)
        loadingIndicator.visibility = View.GONE
    }

    private fun initZaloPay() {
        StrictMode.ThreadPolicy.Builder().permitAll().build()
            .also { StrictMode.setThreadPolicy(it) }
        ZaloPaySDK.init(Constants.APP_ID, Environment.SANDBOX)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        ZaloPaySDK.getInstance().onResult(intent)
    }


}
