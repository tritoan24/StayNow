package com.ph32395.staynow_datn.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ph32395.staynow_datn.MainActivity
import com.ph32395.staynow_datn.TaoHoaDon.InvoiceMonthlyModel
import com.ph32395.staynow_datn.TaoHopDong.HopDong
import com.ph32395.staynow_datn.databinding.ActivitySuccessPaymentBinding
import com.ph32395.staynow_datn.hieunt.widget.tap
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
        val bill = intent.getSerializableExtra("bill") as? InvoiceMonthlyModel

        if (contract != null) {
            binding.tvInvoiceId.text = contract.hoaDonHopDong.idHoaDon
            binding.tvAmount.text = contract.hoaDonHopDong.tongTien.toString()
            binding.tvAddress.text = contract.thongtinphong.diaChiPhong
            binding.tvDate.text = contract.ngayThanhToan.toString()
            binding.tvInvoiceId.text = "ID hợp đồng: " + contract.maHopDong
            binding.tvLandlordName.text = contract.chuNha.hoTen
            binding.tvLandlordAddress.text = contract.chuNha.diaChi
            binding.tvTenantName.text = contract.nguoiThue.hoTen
            binding.tvTenantAddress.text = contract.nguoiThue.diaChi
            binding.tvNameRoom.text = "Tên phòng: " + contract.thongtinphong.tenPhong
            binding.tvAmount.text = "Tổng tiền: " + formatCurrency(contract.hoaDonHopDong.tongTien)
            binding.tvAddress.text = "Địa chỉ: " + contract.thongtinphong.diaChiPhong
            binding.tvTerm.text = "Thời hạn thuê: " + contract.thoiHanThue
            binding.tvDate.text = formatServerTime(contract.hoaDonHopDong.paymentDate)

        }

        if (bill != null) {
            hideUIForInvoice()
            binding.tvInvoiceId.text = bill.idHoaDon
            binding.tvTenantName.text = bill.tenKhachHang
            binding.tvInvoiceId.text = "ID hợp đồng: " + bill.idHopDong
            binding.tvNameRoom.text = "Tên phòng: " + bill.tenPhong
            binding.tvAmount.text = "Tổng tiền: " + formatCurrency(bill.tongTien)
            binding.tvAmountRoom.text =
                "Tiền phòng: " + formatCurrency(bill.tienPhong)
            binding.tvDate.text = formatServerTime(bill.paymentDate)

        }

        binding.btnGoHome.tap {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }

    private fun hideUIForInvoice() {
        binding.tvLandlordAddress.visibility = View.GONE
        binding.tvAddress.visibility = View.GONE
        binding.tvTerm.visibility = View.GONE
        binding.tvAmountDeposit.visibility = View.GONE
        binding.tvTenantAddress.visibility = View.GONE
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

