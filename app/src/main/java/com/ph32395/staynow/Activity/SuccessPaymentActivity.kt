package com.ph32395.staynow.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ph32395.staynow.MainActivity
import com.ph32395.staynow.TaoHopDong.HopDong
import com.ph32395.staynow.databinding.ActivitySuccessPaymentBinding
import com.ph32395.staynow.hieunt.widget.tap
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("DEPRECATION")
class SuccessPaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySuccessPaymentBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuccessPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val hieuUngBinding = binding.hieuUngView
        hieuUngBinding.animationView.setAnimation("success.json")
        hieuUngBinding.animationView.playAnimation()
        hieuUngBinding.textMessage.text = "Thanh toán thành công"

        val contract = intent.getSerializableExtra("itemData") as? HopDong

        binding.tvInvoiceId.text = "ID hợp đồng: " + contract!!.maHopDong
        binding.tvLandlordInfo.text =contract.chuNha.hoTen
        binding.tvLandlordPhone.text =contract.chuNha.soDienThoai
        binding.tvTenantInfo.text = contract.nguoiThue.hoTen
        binding.tvTenantPhone.text = contract.nguoiThue.soDienThoai
        binding.tvNameRoom.text = "Tên phòng: " + contract.thongtinphong.tenPhong
        binding.tvAmount.text = "Tổng tiền: " + formatCurrency(contract.hoaDonHopDong.tongTien)
        binding.tvAmountRoom.text =
            "Tiền phòng: " + formatCurrency(contract.hoaDonHopDong.tienPhong)
        binding.tvAmountDeposit.text =
            "Tiền cọc: " + formatCurrency(contract.hoaDonHopDong.tienCoc)
        binding.tvAddress.text = "Địa chỉ: " + contract.thongtinphong.diaChiPhong
        binding.tvStartDate.text = "Ngày bắt đầu: " + contract.ngayBatDau
        binding.tvEndDate.text = "Ngày kết thúc: " + contract.ngayKetThuc
        binding.tvTerm.text = "Thời hạn thuê: " + contract.thoiHanThue
        binding.tvDate.text = formatServerTime(contract.hoaDonHopDong.paymentDate)

        binding.btnGoHome.tap {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


    }


    // Định dạng tiền tệ
    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return formatter.format(amount)
    }

    private fun formatServerTime(serverTime: String): String {
        val date = Date(serverTime.toLong())

        // Định dạng ngày giờ theo chuẩn mong muốn
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return dateFormat.format(date)
    }

}

