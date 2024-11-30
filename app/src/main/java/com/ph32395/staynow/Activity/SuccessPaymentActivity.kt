package com.ph32395.staynow.Activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ph32395.staynow.MainActivity
import com.ph32395.staynow.TaoHopDong.HopDong
import com.ph32395.staynow.databinding.ActivitySuccessPaymentBinding

@Suppress("DEPRECATION")
class SuccessPaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySuccessPaymentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuccessPaymentBinding.inflate(layoutInflater)

        val contract = intent.getSerializableExtra("itemData") as? HopDong

        binding.tvInvoiceId.text = contract!!.hoaDonHopDong.idHoaDon
        binding.tvLandlordInfo.text = contract.hoaDonHopDong.idNguoinhan
        binding.tvTenantInfo.text = contract.hoaDonHopDong.idNguoigui
        binding.tvAmount.text = contract.hoaDonHopDong.tongTien.toString()
        binding.tvAddress.text = contract.thongTinPhong.diaChiPhong
        binding.tvStartDate.text = contract.ngayBatDau
        binding.tvEndDate.text = contract.ngayKetThuc
        binding.tvDate.text = contract.ngayThanhToan.toString()

        binding.btnGoHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }

}

