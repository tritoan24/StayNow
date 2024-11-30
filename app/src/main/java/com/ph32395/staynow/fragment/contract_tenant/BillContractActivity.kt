package com.ph32395.staynow.fragment.contract_tenant

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.ph32395.staynow.TaoHopDong.HopDong
import com.ph32395.staynow.TaoHopDong.Invoice
import com.ph32395.staynow.TaoHopDong.PersonInfo
import com.ph32395.staynow.databinding.ActivityBillContractBinding
import com.ph32395.staynow.payment.OrderProcessor
import com.ph32395.staynow.payment.SocketManager
import com.ph32395.staynow.utils.Constants
import vn.zalopay.sdk.Environment
import vn.zalopay.sdk.ZaloPaySDK

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
        initZaloPay()

        // nhận intent từ contractAdapter
        val invoice = intent.getSerializableExtra("hoaDonHopDong") as? Invoice
        val contract = intent.getSerializableExtra("hopDong") as? HopDong

        // convert invoice to jsonArrStr
        val gson = Gson()
        val itemsArrStr = gson.toJson(listOf(invoice))

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
                    orderProcessor.startPayment(token, contract)
                } else {
                    Toast.makeText(this, "Lỗi khi tạo đơn hàng", Toast.LENGTH_SHORT).show()
                }
            }
        }

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

    override fun onDestroy() {
        super.onDestroy()
        socketManager.disconnect()
    }
}
