package com.ph32395.staynow.utils

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
        .setConfirmText("OK")
        .setCancelText("NO")
        .setConfirmClickListener { sweetAlertDialog ->
            sweetAlertDialog.dismiss()
            onConfirm()
        }
        .setCancelClickListener { sweetAlertDialog ->
            sweetAlertDialog.dismiss()
        }

    dialog.show()
}
