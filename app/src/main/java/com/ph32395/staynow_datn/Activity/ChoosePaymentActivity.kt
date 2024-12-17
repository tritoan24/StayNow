package com.ph32395.staynow_datn.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.ph32395.staynow_datn.TaoHoaDon.InvoiceMonthlyModel
import com.ph32395.staynow_datn.TaoHopDong.HopDong
import com.ph32395.staynow_datn.databinding.ActivityChoosePaymentBinding
import com.ph32395.staynow_datn.hieunt.helper.Default
import com.ph32395.staynow_datn.hieunt.model.NotificationModel
import com.ph32395.staynow_datn.hieunt.view_model.NotificationViewModel
import com.ph32395.staynow_datn.hieunt.view_model.ViewModelFactory
import com.ph32395.staynow_datn.hieunt.widget.tap
import com.ph32395.staynow_datn.payment.OrderProcessor
import com.ph32395.staynow_datn.payment.OrderProcessorService
import com.ph32395.staynow_datn.payment.SocketManager
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
                    sendNotify(bill, contract)
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
                    sendNotify(bill, contract)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        showQRCode(orderUrl!!)

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

    override fun onDestroy() {
        super.onDestroy()
        socketManager.disconnect()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        ZaloPaySDK.getInstance().onResult(intent)
    }

    private fun sendNotify(bill: InvoiceMonthlyModel?, contract: HopDong?) {

        val notification = (bill?.idHoaDon ?: contract?.maHopDong)?.let {
            val message =
                if (bill != null) "Thanh toán thành công cho hóa đơn ${bill.idHoaDon}" else "Thanh toán thành công cho hợp đồng ${contract?.maHopDong}"
            NotificationModel(
                title = "Thanh toán hóa đơn hàng tháng",
                message = message,
                date = Calendar.getInstance().time.toString(),
                time = "0",
                mapLink = null,
                isRead = false,
                isPushed = true,
                idModel = it,
                typeNotification = if (bill != null) Default.TypeNotification.TYPE_NOTI_PAYMENT_INVOICE else Default.TypeNotification.TYPE_NOTI_PAYMENT_CONTRACT
            )
        }

        val factory = ViewModelFactory(this)
        val notificationViewModel = ViewModelProvider(
            this,
            factory
        )[NotificationViewModel::class.java]

        val recipientIds = bill?.idNguoiGui

        if (notification != null) {
            if (recipientIds != null) {
                notificationViewModel.sendNotification(notification, recipientIds)
            }
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