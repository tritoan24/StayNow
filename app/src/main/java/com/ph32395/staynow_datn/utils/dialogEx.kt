package com.ph32395.staynow_datn.utils

import android.content.Context
import cn.pedant.SweetAlert.SweetAlertDialog


fun showConfirmDialog(
    context: Context,
    title: String,
    message: String,
    onConfirm: () -> Unit // Callback khi nhấn "OK"
) {
    val dialog = SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
        .setTitleText(title)
        .setContentText(message)
        .setConfirmText("Đồng ý")
        .setCancelText("Hủy bỏ")
        .setConfirmClickListener { sweetAlertDialog ->
            sweetAlertDialog.dismiss()
            onConfirm()
        }
        .setCancelClickListener { sweetAlertDialog ->
            sweetAlertDialog.dismiss()
        }

    dialog.show()
}
fun showReasonInputDialog(
    context: Context,
    title: String,
    hint: String,
    onReasonEntered: (String) -> Unit
) {
    val input = android.widget.EditText(context).apply {
        this.hint = hint
        this.setPadding(16, 16, 16, 16)
        this.background = null // Tùy chỉnh thêm nếu cần
    }

    val dialog = SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
        .setTitleText(title)
        .setCustomView(input) // Thêm EditText vào dialog
        .setConfirmText("Xác nhận")
        .setCancelText("Hủy bỏ")
        .setConfirmClickListener { sweetAlertDialog ->
            val reason = input.text.toString().trim()
            if (reason.isEmpty()) {
                SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Lỗi")
                    .setContentText("Lý do không được để trống!")
                    .setConfirmText("Đồng ý")
                    .show()
            } else {
                sweetAlertDialog.dismiss()
                onReasonEntered(reason) // Callback với lý do
            }
        }
        .setCancelClickListener { sweetAlertDialog ->
            sweetAlertDialog.dismiss()
        }

    dialog.show()
}
