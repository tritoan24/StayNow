package com.ph32395.staynow_datn.BaoMat

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.ViewModel.RoomDetailViewModel
import com.ph32395.staynow_datn.fragment.home.HomeViewModel

class ThongBaoToCaoNguoiDung : DialogFragment() {

    private lateinit var checkbox: CheckBox
    private lateinit var btnHuyTB: MaterialButton
    private lateinit var btnXacNhanTB: MaterialButton
    private lateinit var viewModel: RoomDetailViewModel
    private lateinit var homViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_thong_bao_to_cao_nguoi_dung, container, false)

        viewModel = ViewModelProvider(this)[RoomDetailViewModel::class.java]
        homViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

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

        // Lấy userId và maPhongTro từ arguments
        val userId = arguments?.getString("idUser")
        val maPhongTro = arguments?.getString("maPhongTro") // Nhận mã phòng trọ từ arguments

        btnXacNhanTB.setOnClickListener {
            if (userId != null && maPhongTro != null) {
                val intent = Intent(requireContext(), ToCaoPhongTro::class.java)
                intent.putExtra("idUser", userId) // Truyền userId qua Intent
                intent.putExtra("maPhongTro", maPhongTro) // Truyền mã phòng trọ qua Intent
                startActivity(intent)
                dismiss() // Đóng dialog
            } else {
                Toast.makeText(requireContext(), "Không có userId hoặc maPhongTro để truyền", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    // Hàm cập nhật trạng thái nút dựa trên checkbox
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
