package com.ph32395.staynow.view

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Toast
import com.ph32395.staynow.QuanLyPhongTro.QuanLyPhongTroActivity
import com.ph32395.staynow.R

class ItemQuanLyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_item_quan_ly, this, true)

//        Khoi tao su kien click va chi dinh  tung chuc nang
        setupItemClick(R.id.item_quan_ly_phong) { onItemClicked("QuanLyPhong")}
        setupItemClick(R.id.item_quan_ly_hop_dong) { onItemClicked("QuanLyHopDong")}
        setupItemClick(R.id.item_quan_ly_hoa_don) { onItemClicked("QuanLyHoaDon")}
        setupItemClick(R.id.item_quan_ly_nguoi_thue) { onItemClicked("QuanLyNguoiThue")}
    }

    private fun setupItemClick(itemId: Int, action: () -> Unit) {
        findViewById<LinearLayout>(itemId).setOnClickListener { action() }
    }

    private fun onItemClicked(actionName: String) {
        when (actionName) {
            "QuanLyPhong" -> {
                context.startActivity(Intent(context, QuanLyPhongTroActivity::class.java))
            }

            "QuanLyHopDong" -> {
                Toast.makeText(context, "Quan ly hop dong", Toast.LENGTH_SHORT).show()
            }

            "QuanLyHoaDon" -> {
                Toast.makeText(context, "Quan ly hoa don", Toast.LENGTH_SHORT).show()
            }

            "QuanLyNguoiThue" -> {
                Toast.makeText(context, "Quan ly nguoi thue", Toast.LENGTH_SHORT).show()
            }
        }
    }
}