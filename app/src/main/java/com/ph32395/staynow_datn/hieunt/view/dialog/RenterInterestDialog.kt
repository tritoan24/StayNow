package com.ph32395.staynow_datn.hieunt.view.dialog

import android.view.LayoutInflater
import android.view.ViewGroup
import com.ph32395.staynow_datn.databinding.DialogRenterInterestBinding
import com.ph32395.staynow_datn.hieunt.base.BaseFragmentDialog
import com.ph32395.staynow_datn.hieunt.helper.SharePrefUtils
import com.ph32395.staynow_datn.hieunt.widget.tap

class RenterInterestDialog(
    private val onClickButton : () -> Unit
): BaseFragmentDialog<DialogRenterInterestBinding>(true) {
    override fun initView() {
        binding.apply {
            cbRead.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {

                } else {

                }
            }
            btnConfirm.tap {
                onClickButton()
                if (cbRead.isChecked){
                    SharePrefUtils(requireContext()).isReadRenterInterest = true
                    dismiss()
                } else {
                    dismiss()
                }
            }
            btnHuyChuTro.tap {
                dismiss()
            }
        }
    }

    override fun initClickListener() {

    }

    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogRenterInterestBinding {
        return DialogRenterInterestBinding.inflate(layoutInflater)
    }
}