package com.ph32395.staynow.ChucNangTimKiem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ph32395.staynow.R
import com.ph32395.staynow.databinding.BottomSheetFilterSearchBinding
class BottomSheetFilter : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetFilterSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetFilterSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // Lấy BottomSheetDialog
        val dialog = dialog as BottomSheetDialog

        // Truy cập FrameLayout của BottomSheet
        val bottomSheet = dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)

        // Đảm bảo bottomSheet không null rồi thay đổi chiều cao
        bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT

        // Cài đặt chiều rộng và chiều cao
        bottomSheet?.layoutParams?.width = ViewGroup.LayoutParams.MATCH_PARENT

        // Cho phép bottom sheet được kéo lên xuống
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
    }
}
