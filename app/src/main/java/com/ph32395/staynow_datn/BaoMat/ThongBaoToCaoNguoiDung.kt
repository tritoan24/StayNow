package com.ph32395.staynow_datn.BaoMat

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.ViewModel.RoomDetailViewModel

class ThongBaoToCaoNguoiDung : DialogFragment() {

    private lateinit var checkbox: CheckBox
    private lateinit var btnHuyTB: MaterialButton
    private lateinit var btnXacNhanTB: MaterialButton
    private lateinit var viewModel: RoomDetailViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_thong_bao_to_cao_nguoi_dung, container, false)

        viewModel = ViewModelProvider(this)[RoomDetailViewModel::class.java]
        // Ánh xạ các view từ layout
        checkbox = view.findViewById(R.id.checkQuyenThongBao)
        btnHuyTB = view.findViewById(R.id.btnHuyThongBao)
        btnXacNhanTB = view.findViewById(R.id.btnXacNhanThongBao)

        // Thiết lập màu sắc ban đầu cho các nút khi checkbox chưa được chọn
        updateButtonState(checkbox.isChecked)

        // Lắng nghe sự kiện click của checkbox
        checkbox.setOnCheckedChangeListener { _, isChecked ->
            updateButtonState(isChecked)
        }

        // Xử lý nút Hủy
        btnHuyTB.setOnClickListener {
            dismiss() // Đóng dialog
        }

        // Xử lý nút Xác nhận
        btnXacNhanTB.setOnClickListener {
            val intent = Intent(requireContext(), ToCaoTaiKhoan::class.java)
            // Kiểm tra lại cách lấy userId từ ViewModel hoặc các nguồn khác
            viewModel.userId.observe(this) { (maNguoiDung, hoTen) ->
                intent.putExtra("idUser", maNguoiDung) // Đảm bảo rằng maNguoiDung có giá trị đúng
            }
            startActivity(intent)
        }




        return view
    }

    private fun updateButtonState(isChecked: Boolean) {
        val enabledColor = ColorStateList.valueOf(Color.parseColor("#532CA6")) // Màu tím
        val disabledColor = ColorStateList.valueOf(Color.parseColor("#C8C8C8")) // Màu xám nhạt

        if (isChecked) {
            btnHuyTB.backgroundTintList = enabledColor
            btnXacNhanTB.backgroundTintList = enabledColor
            btnHuyTB.isEnabled = true
            btnXacNhanTB.isEnabled = true
        } else {
            btnHuyTB.backgroundTintList = disabledColor
            btnXacNhanTB.backgroundTintList = disabledColor
            btnHuyTB.isEnabled = false
            btnXacNhanTB.isEnabled = false
        }
    }

    override fun getTheme(): Int {
        // Sử dụng một theme để dialog hiển thị toàn màn hình nếu cần
        return R.style.CustomDialogTheme
    }
}
