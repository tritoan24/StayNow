package com.ph32395.staynow_datn.hieunt.view.dialog

import android.view.LayoutInflater
import android.view.ViewGroup
import com.ph32395.staynow_datn.databinding.DialogWarningScheduleBinding
import com.ph32395.staynow_datn.hieunt.base.BaseFragmentDialog
import com.ph32395.staynow_datn.hieunt.helper.SharePrefUtils
import com.ph32395.staynow_datn.hieunt.widget.tap

class WarningScheduleDialog: BaseFragmentDialog<DialogWarningScheduleBinding>(true) {
    override fun initView() {
        binding.tvConfirm.tap {
            SharePrefUtils(requireContext()).isViewedWarningSchedule = true
            dismiss()
        }
    }

    override fun initClickListener() {

    }

    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogWarningScheduleBinding {
        return DialogWarningScheduleBinding.inflate(layoutInflater)
    }
}