package com.ph32395.staynow.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.ph32395.staynow.TaoHopDong.HopDong
import com.ph32395.staynow.databinding.ActivityChoosePaymentBinding
import com.ph32395.staynow.hieunt.widget.tap
import com.ph32395.staynow.payment.OrderProcessor
import com.ph32395.staynow.payment.SocketManager


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
        val itemsArrStr = intent.getStringExtra("itemsArrStr")
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
                    val intent = Intent(this, SuccessPaymentActivity::class.java)
                    intent.putExtra("itemData", contract)
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
            finish()
        }

        binding.btnThanhtoan.tap {
            zpToken?.let {
                val orderProcessor = OrderProcessor(this)
                orderProcessor.startPayment(it, contract!!)
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

    private fun startTimer(remainTime: Long) {
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
}