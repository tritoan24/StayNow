package com.ph32395.staynow_datn.quanlyhoadon

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.google.gson.Gson
import com.ph32395.staynow_datn.Activity.ChoosePaymentActivity
import com.ph32395.staynow_datn.TaoHoaDon.InvoiceMonthlyModel
import com.ph32395.staynow_datn.TaoHoaDon.InvoiceViewModel
import com.ph32395.staynow_datn.TaoHopDong.Adapter.FixedFeeAdapter
import com.ph32395.staynow_datn.TaoHopDong.Adapter.VariableFeeAdapter
import com.ph32395.staynow_datn.TaoHopDong.InvoiceStatus
import com.ph32395.staynow_datn.databinding.ActivityDetailBillBinding
import com.ph32395.staynow_datn.hieunt.widget.tap
import com.ph32395.staynow_datn.payment.OrderProcessorService
import com.ph32395.staynow_datn.payment.TypeBill
import com.ph32395.staynow_datn.utils.Constants
import com.ph32395.staynow_datn.utils.showConfirmDialog
import vn.zalopay.sdk.Environment
import vn.zalopay.sdk.ZaloPaySDK
import java.text.NumberFormat
import java.util.Locale

@Suppress("DEPRECATION")
class DetailBillActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBillBinding
    private lateinit var loadingIndicator: LottieAnimationView
    private lateinit var invoiceViewModel: InvoiceViewModel

    @SuppressLint("SuspiciousIndentation", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBillBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingIndicator = binding.loadingIndicator
        invoiceViewModel = ViewModelProvider(this).get(InvoiceViewModel::class.java)

        initZaloPay()

        // nhận intent từ billAdapter
        val invoice = intent.getSerializableExtra("bill") as? InvoiceMonthlyModel
        val invoiceId = intent.getStringExtra("invoiceId")

        val detail = intent.getStringExtra("detail")
        val notify = intent.getStringExtra("notify")

        // convert invoice to jsonArrStr
        val gson = Gson()
        val itemsArrStr = gson.toJson(listOf(invoice))

        if (invoiceId != null) {
            invoiceViewModel.fetchInvoiceById(invoiceId)
            invoiceViewModel.invoice.observe(this) { invoices ->
                if (invoice != null) {
                    invoices?.let { updateUI(it) }
                }
            }
        }

        updateUI(invoice!!)

        if (detail != null) {
            binding.btnThanhtoan.visibility = View.GONE
            binding.tvTitle.text = "Chi tiết hóa đơn hàng tháng"
        }
        if (notify != null) {
            binding.btnThanhtoan.visibility = View.GONE
        }
        if (invoice.trangThai != InvoiceStatus.PENDING) {
            binding.btnThanhtoan.visibility = View.GONE
        }

        binding.ivBack.tap {
            onBackPressed()
            finish()
        }

        binding.btnThanhtoan.tap {

            showConfirmDialog(
                this,
                "Xác nhận thanh toán",
                "Bạn đã kiểm tra kĩ thông tin rồi chứ ?"
            ) {
                showLoading()
                val orderProcessor = OrderProcessorService(this)
                orderProcessor.checkAndCreateOrder(
                    invoice.tongTien,
                    invoice.idHoaDon,
                    itemsArrStr,
                    TypeBill.HoaDonHangThang
                ) { token, orderUrl, remainTime ->
                    hideLoading()
                    if (token != null && orderUrl != null) {
                        val intent = Intent(this, ChoosePaymentActivity::class.java)
                        intent.putExtra("bill", invoice)
                        intent.putExtra("zpToken", token)
                        intent.putExtra("orderUrl", orderUrl)
                        intent.putExtra("remainTime", remainTime)
                        startActivity(intent)
                    }
                }
            }

        }

    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(invoice: InvoiceMonthlyModel) {
        // Thông tin chung hóa đơn
        binding.tvInvoiceId.text = "Mã hóa đơn: ${invoice.idHoaDon}"
        binding.tvRoomName.text = "Phòng: " + invoice.tenPhong
        binding.tvInvoiceDate.text = "Ngày tạo hóa đơn: ${invoice.ngayTaoHoaDon}"
        binding.tvTotal.text = formatCurrency(invoice.tongTien)
        binding.tvRoomPrice.text = formatCurrency(invoice.tienPhong)
        binding.tvServiceFee.text = formatCurrency(invoice.tongTienDichVu)
        binding.tvTienGiam.text = formatCurrency(invoice.tienGiam)
        binding.tvTienThem.text = formatCurrency(invoice.tienThem)
        binding.tvTotal.text = formatCurrency(invoice.tongTien)

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
        loadingIndicator.visibility = View.VISIBLE
        loadingIndicator.playAnimation()
    }

    private fun hideLoading() {
        loadingIndicator.visibility = View.GONE
    }

    private fun initZaloPay() {
        StrictMode.ThreadPolicy.Builder().permitAll().build()
            .also { StrictMode.setThreadPolicy(it) }
        ZaloPaySDK.init(Constants.APP_ID, Environment.SANDBOX)
    }

}
