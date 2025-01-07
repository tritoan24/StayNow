package com.ph32395.staynow_datn.BaoMat

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.TaoPhongTro.TaoPhongTro

class ThongBaoChuTro : DialogFragment() {

    private lateinit var checkboxChuTro: CheckBox
    private lateinit var btnHuyTBChuTro: MaterialButton
    private lateinit var btnXacNhanTBChuTro: MaterialButton
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_thong_bao_chu_tro, container, false)

        checkboxChuTro = view.findViewById(R.id.checkQuyenThongBaoChuTro)
        btnHuyTBChuTro = view.findViewById(R.id.btnHuyThongBaoChuTro)
        btnXacNhanTBChuTro = view.findViewById(R.id.btnXacNhanThongBaoChuTro)

        updateButtonState(checkboxChuTro.isChecked)

        // Lắng nghe sự kiện click của checkbox
        checkboxChuTro.setOnCheckedChangeListener { _, isChecked ->
            updateButtonState(isChecked)
        }

        // Xử lý nút Hủy
        btnHuyTBChuTro.setOnClickListener {
            dismiss() // Đóng dialog
        }


        btnXacNhanTBChuTro.setOnClickListener {
           dismiss()
        }
        return view
    }
    private fun updateButtonState(isChecked: Boolean) {
        val enabledColor = ColorStateList.valueOf(Color.parseColor("#532CA6")) // Màu tím
        val disabledColor = ColorStateList.valueOf(Color.parseColor("#C8C8C8")) // Màu xám nhạt

        if (isChecked) {
            btnHuyTBChuTro.backgroundTintList = enabledColor
            btnXacNhanTBChuTro.backgroundTintList = enabledColor
            btnHuyTBChuTro.isEnabled = true
            btnXacNhanTBChuTro.isEnabled = true
        } else {
            btnHuyTBChuTro.backgroundTintList = disabledColor
            btnXacNhanTBChuTro.backgroundTintList = disabledColor
            btnHuyTBChuTro.isEnabled = false
            btnXacNhanTBChuTro.isEnabled = false
        }
    }

    override fun getTheme(): Int {
        // Sử dụng một theme để dialog hiển thị toàn màn hình nếu cần
        return R.style.CustomDialogTheme
    }
}