package com.ph32395.staynow.ChucNangChung

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.text.NumberFormat
import java.util.Locale

class CurrencyFormatTextWatcher(
    private val editText: EditText,
    private val onValueChanged: (() -> Unit)? = null
) : TextWatcher {
    private var current = ""

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        if (s.toString() != current) {
            editText.removeTextChangedListener(this)

            val cleanString = s.toString().replace(".", "").replace(",", "")
            val formatted: String = if (cleanString.isNotEmpty()) {
                val parsed = cleanString.toLongOrNull() ?: 0
                NumberFormat.getNumberInstance(Locale("vi", "VN")).format(parsed)
            } else {
                ""
            }

            current = formatted
            editText.setText(formatted)
            editText.setSelection(formatted.length)

            // Gọi callback nếu có
            onValueChanged?.invoke()

            editText.addTextChangedListener(this)
        }
    }

    companion object {
        // Phương thức tĩnh để dễ dàng áp dụng
        fun addTo(
            editText: EditText,
            onValueChanged: (() -> Unit)? = null
        ) {
            editText.addTextChangedListener(CurrencyFormatTextWatcher(editText, onValueChanged))
        }

        // Phương thức để lấy giá trị số từ EditText đã format
        fun getUnformattedValue(editText: EditText): Double {
            return editText.text.toString()
                .replace(".", "")
                .replace(",", "")
                .toDoubleOrNull() ?: 0.0
        }
    }
}


//// Cách 1: Sử dụng trực tiếp
//CurrencyFormatTextWatcher.addTo(binding.editTienGiam) {
//    // Tùy chọn: Thực hiện các hành động khi giá trị thay đổi
//    updateTotalBill()
//}
//
//// Cách 2: Lấy giá trị
//val tienGiam = CurrencyFormatTextWatcher.getUnformattedValue(binding.editTienGiam)
//
//// Trong hàm updateTotalBill()
//private fun updateTotalBill() {
//    tienThem = CurrencyFormatTextWatcher.getUnformattedValue(binding.editPhiThem)
//    tienGiam = CurrencyFormatTextWatcher.getUnformattedValue(binding.editTienGiam)
//
//    // Phần còn lại của code giữ nguyên
//}