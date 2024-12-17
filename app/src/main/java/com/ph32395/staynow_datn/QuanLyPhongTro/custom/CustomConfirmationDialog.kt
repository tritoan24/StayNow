package com.ph32395.staynow_datn.QuanLyPhongTro.custom

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.ph32395.staynow_datn.R

class CustomConfirmationDialog(
    private val message: String,
    private val onConfirm: () -> Unit,
    private val onCancel: () -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)

        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_custom_confirmation, null)

        // Gắn nội dung cho TextView
        val messageText = view.findViewById<TextView>(R.id.dialogMessage)
        messageText.text = message

        // Xử lý khi nhấn nút "Xác nhận"
        view.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            onConfirm()
            dismiss() // Đóng Dialog
        }

        // Xử lý khi nhấn nút "Hủy"
        view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            onCancel()
            dismiss() // Đóng Dialog
        }

        builder.setView(view)
        return builder.create()
    }
}