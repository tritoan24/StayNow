package com.ph32395.staynow.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Toast
import cn.pedant.SweetAlert.SweetAlertDialog
import com.ph32395.staynow.R
import com.ph32395.staynow.TaoHoaDon.ChoiceContract

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
                SDialog()

            }
        }
    }

    private fun SDialog() {
        val activityContext = context as? Activity
        // Tạo SweetAlertDialog với kiểu tùy chọn
        SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
            .setTitleText("Chọn phương thức tạo hóa đơn")
            .setContentText("Bạn muốn tạo hóa đơn tự động từ hợp đồng có sẵn hay theo phòng trọ?")
            .setConfirmText("Tự Động")
            .setCancelText("Phòng Trọ")
            .setConfirmClickListener { dialog ->
                // Xử lý khi người dùng chọn "Tạo hóa đơn tự động"
                dialog.dismissWithAnimation()
                // Gọi hàm xử lý tạo hóa đơn tự động ở đây
                Toast.makeText(context, "Tạo hóa đơn tự động", Toast.LENGTH_SHORT).show()
                //chuyển màn
                val intent = Intent(activityContext, ChoiceContract::class.java)
                if (activityContext != null) {
                    activityContext.startActivity(intent)
                }
            }
            .setCancelClickListener { dialog ->
                // Xử lý khi người dùng chọn "Tạo hóa đơn thủ công"
                dialog.dismissWithAnimation()
                // Gọi hàm xử lý tạo hóa đơn thủ công ở đây
                Toast.makeText(context, "Tạo hóa đơn thủ công", Toast.LENGTH_SHORT).show()
            }
            .show()
    }


}