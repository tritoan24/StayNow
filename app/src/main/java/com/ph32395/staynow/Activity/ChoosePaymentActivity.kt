package com.ph32395.staynow.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.ph32395.staynow.TaoHoaDon.InvoiceMonthlyModel
import com.ph32395.staynow.TaoHopDong.HopDong
import com.ph32395.staynow.databinding.ActivityChoosePaymentBinding
import com.ph32395.staynow.hieunt.model.NotificationModel
import com.ph32395.staynow.hieunt.view_model.NotificationViewModel
import com.ph32395.staynow.hieunt.view_model.ViewModelFactory
import com.ph32395.staynow.hieunt.widget.tap
import com.ph32395.staynow.payment.OrderProcessor
import com.ph32395.staynow.payment.OrderProcessorService
import com.ph32395.staynow.payment.SocketManager
import vn.zalopay.sdk.ZaloPaySDK
import java.util.Calendar


@Suppress("DEPRECATION")
class ChoosePaymentActivity : AppCompatActivity() {

    // Đối tượng Binding
    private lateinit var binding: ActivityChoosePaymentBinding
    private lateinit var socketManager: SocketManager

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChoosePaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val contract = intent.getSerializableExtra("contract") as? HopDong
        val bill = intent.getSerializableExtra("bill") as? InvoiceMonthlyModel
        val zpToken = intent.getStringExtra("zpToken")
        val orderUrl = intent.getStringExtra("orderUrl")
        val remainTime = intent.getLongExtra("remainTime", -1)

        // Khởi tạo socket
        socketManager = SocketManager()
        socketManager.connect()

        // Lắng nghe sự kiện từ server
        socketManager.on("paymentCallback") {
            runOnUiThread {
                try {
                    if (bill != null) {
                        sendNotify(bill)
                    }

                    val intent = Intent(this, SuccessPaymentActivity::class.java)
                    intent.putExtra("itemData", contract)
                    startActivity(intent)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        //lắng nghe event hóa đơn hàng tháng
        socketManager.on("paymentCallbackService") {
            runOnUiThread {
                try {
                    val intent = Intent(this, SuccessPaymentActivity::class.java)
                    intent.putExtra("bill", bill)
                    startActivity(intent)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        showQRCode(orderUrl!!)
        startTimer(remainTime)

        binding.ivBack.tap {
            onBackPressed()
        }

        binding.btnThanhtoan.tap {
            zpToken?.let {
                if (contract != null) {
                    val orderProcessor = OrderProcessor(this)
                    orderProcessor.startPayment(it, contract)
                } else {
                    val orderProcessor = OrderProcessorService(this)
                    orderProcessor.startPayment(zpToken, bill!!)
                }

            }
        }


    }

    private fun showQRCode(orderUrl: String) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(orderUrl, BarcodeFormat.QR_CODE, 400, 400)
            binding.qrCode.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun startTimer(remainTime: Long) {
        if (remainTime <= 0) {
            binding.con.visibility = View.GONE
            binding.remainTime.text = "Đơn hàng đã hết hạn\nHãy quay lại và tạo đơn mới!"
            binding.btnThanhtoan.isEnabled = false
            return
        }

        val timer = object : CountDownTimer(remainTime, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 60000
                val seconds = (millisUntilFinished % 60000) / 1000
                // Cập nhật UI để hiển thị thời gian còn lại
                binding.con.visibility = View.VISIBLE
                binding.remainTime.text = "$minutes:$seconds"
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                binding.con.visibility = View.GONE
                binding.remainTime.text = "Đơn hàng đã hết hạn\nHãy quay lại và tạo đơn mới!"
                binding.btnThanhtoan.isEnabled = false
            }
        }

        timer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        socketManager.disconnect()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        ZaloPaySDK.getInstance().onResult(intent)
    }

    fun sendNotify(bill: InvoiceMonthlyModel) {
        val notification = NotificationModel(
            title = "Thanh toán hóa đơn hàng tháng",
            message = "Thanh toán thành công cho hóa đơn ${bill!!.idHoaDon}",
            date = Calendar.getInstance().time.toString(), // Lấy ngày hiện tại
            time = "0",
            mapLink = null,
            isRead = false,
            isPushed = true,
            idModel = bill.idHoaDon
        )

        val factory = ViewModelFactory(this)
        val notificationViewModel = ViewModelProvider(
            this,
            factory
        )[NotificationViewModel::class.java]

        // Gửi thông báo đến cả hai người
        val recipientIds = listOf(bill.idNguoiGui, bill.idNguoiNhan)
        recipientIds.forEach { recipientId ->
            notificationViewModel.sendNotification(notification, recipientId)
        }
        // Giám sát trạng thái gửi thông báo
        notificationViewModel.notificationStatus.observe(this, Observer { isSuccess ->
            if (isSuccess) {
                // Thông báo thành công
                Toast.makeText(this, "Thông báo đã được gửi!", Toast.LENGTH_SHORT)
                    .show()
            } else {
                // Thông báo thất bại
                Toast.makeText(this, "Gửi thông báo thất bại!", Toast.LENGTH_SHORT)
                    .show()
            }
        })

    }

}