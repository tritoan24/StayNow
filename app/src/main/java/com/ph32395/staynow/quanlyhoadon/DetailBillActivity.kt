package com.ph32395.staynow.quanlyhoadon

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
import com.ph32395.staynow.TaoHopDong.InvoiceStatus
import com.ph32395.staynow.databinding.ActivityBillContractBinding
import com.ph32395.staynow.databinding.ActivityDetailBillBinding
import com.ph32395.staynow.hieunt.widget.tap
import com.ph32395.staynow.payment.OrderProcessor
import com.ph32395.staynow.payment.TypeBill
import com.ph32395.staynow.utils.Constants
import com.ph32395.staynow.utils.showConfirmDialog
import vn.zalopay.sdk.Environment
import vn.zalopay.sdk.ZaloPaySDK
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

@Suppress("DEPRECATION")
class BillContractActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBillBinding
    private lateinit var loadingIndicator: LottieAnimationView

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBillBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingIndicator = binding.loadingIndicator

        initZaloPay()

        // nhận intent từ contractAdapter
        val invoice = intent.getSerializableExtra("hoaDonHopDong") as? Invoice

        // convert invoice to jsonArrStr
        val gson = Gson()
        val itemsArrStr = gson.toJson(listOf(invoice))

        updateUI(invoice!!)

        binding.ivBack.tap {
            onBackPressed()
            finish()
        }

        if (invoice.trangThai == InvoiceStatus.PAID) {
            binding.btnThanhtoan.visibility = View.GONE
        }

        binding.btnThanhtoan.tap {
            showConfirmDialog(this, "Xác nhận thanh toán","Bạn đã kiểm tra kĩ thông tin rồi chứ ?"){

            }

        }

    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(invoice: Invoice) {
        // Thông tin chung hóa đơn
        binding.tvInvoiceId.text = "Mã hóa đơn: ${invoice.idHoaDon}"
        binding.tvRoomName.text = invoice.tenPhong
        binding.tvInvoiceDate.text = "Ngày tạo hóa đơn: ${invoice.ngayLap}"
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

}
