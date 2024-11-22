package com.ph32395.staynow.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Toast
import com.ph32395.staynow.R

class ItemTaoChuTro @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_item_tao_chu_tro, this, true)

//        Khoi tao cac su kien click chuc nang
        setupItemClick(R.id.item_tao_hop_dong) { onItemClicked("TaoHopDong")}
        setupItemClick(R.id.item_tao_hoa_don) { onItemClicked("TaoHoaDon")}

    }

//    Ham su lys khi nhan vao item
    private fun setupItemClick(itemId: Int, action: () -> Unit) {
        findViewById<LinearLayout>(itemId).setOnClickListener { action() }
    }

//    Ham suw lys suwj kien khi click vao item
    private fun onItemClicked(actionName: String) {
        when(actionName) {
            "TaoHopDong" -> {
                Toast.makeText(context, "Tao hop dong", Toast.LENGTH_SHORT).show()
            }

            "TaoHoaDon" -> {
                Toast.makeText(context, "Tao hoa don", Toast.LENGTH_SHORT).show()
            }
        }
    }

}