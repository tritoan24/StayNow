package com.ph32395.staynow.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Toast
import com.ph32395.staynow.R

class ItemTienIchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_item_tien_ich, this, true)

        // Khởi tạo các sự kiện click và chỉ định từng chức năng
        setupItemClick(R.id.item_tim_xung_quanh) { onItemClicked("TimXungQuanh") }
        setupItemClick(R.id.item_tim_nguoi_o_khep) { onItemClicked("TimNguoiOKhep") }
        setupItemClick(R.id.item_van_chuyen_do) { onItemClicked("VanChuyenDo") }
        setupItemClick(R.id.item_doi_binh_gas) { onItemClicked("DoiBinhGas") }
    }

    private fun setupItemClick(itemId: Int, action: () -> Unit) {
        findViewById<LinearLayout>(itemId).setOnClickListener { action() }
    }

    private fun onItemClicked(actionName: String) {
        when (actionName) {
            "TimXungQuanh" -> {
                Toast.makeText(context, "Tìm xung quanh", Toast.LENGTH_SHORT).show()
            }

            "TimNguoiOKhep" -> {
                Toast.makeText(context, "Tìm người ở ghép", Toast.LENGTH_SHORT).show()
            }

            "VanChuyenDo" -> {
                Toast.makeText(context, "Vận chuyển đồ", Toast.LENGTH_SHORT).show()
            }

            "DoiBinhGas" -> {
                Toast.makeText(context, "Đổi bình gas", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

