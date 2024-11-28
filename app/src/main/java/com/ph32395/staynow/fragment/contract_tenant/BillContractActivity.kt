package com.ph32395.staynow.fragment.contract_tenant

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ph32395.staynow.TaoHopDong.HopDong
import com.ph32395.staynow.TaoHopDong.Invoice
import com.ph32395.staynow.TaoHopDong.PersonInfo
import com.ph32395.staynow.TaoHopDong.RoomInfo
import com.ph32395.staynow.databinding.ActivityBillContractBinding

class BillContractActivity : AppCompatActivity() {

    // Đối tượng Binding
    private lateinit var binding: ActivityBillContractBinding

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBillContractBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val roomIfo = intent.getSerializableExtra("thongTinPhong") as? RoomInfo
        val bill = intent.getSerializableExtra("hoaDonHopDong") as? Invoice
        val nguoiThue = intent.getSerializableExtra("nguoiThue") as? PersonInfo
        val chuNha = intent.getSerializableExtra("chuNha") as? PersonInfo
        val contract = intent.getSerializableExtra("hopDong") as? HopDong


        binding.ivBack.setOnClickListener {
            onBackPressed()
            finish()
        }

    }


}
