package com.ph32395.staynow_datn.QuanLyPhongTro.QuanLyKhoPhong

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ph32395.staynow_datn.QuanLyNhaTro.QuanLyNhaTroActivity
import com.ph32395.staynow_datn.QuanLyPhongTro.QuanLyPhongTroActivity
import com.ph32395.staynow_datn.R

class QuanLyKhoPhongActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quan_ly_kho_phong)
        findViewById<ImageView>(R.id.btnBackKhoPhong).setOnClickListener {
            finish()
        }

        findViewById<CardView>(R.id.btnPhongDon).setOnClickListener {
            startActivity(Intent(this, QuanLyPhongTroActivity::class.java))
        }

        //công add
        findViewById<CardView>(R.id.btnToaNha).setOnClickListener {
            startActivity(Intent(this, QuanLyNhaTroActivity::class.java))
        }



    }
}