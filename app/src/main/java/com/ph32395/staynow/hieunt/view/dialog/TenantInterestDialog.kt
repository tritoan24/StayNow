package com.ph32395.staynow.hieunt.view.dialog

import android.view.LayoutInflater
import android.view.ViewGroup
import com.ph32395.staynow.databinding.DialogTenantInterestBinding
import com.ph32395.staynow.hieunt.base.BaseFragmentDialog
import com.ph32395.staynow.hieunt.helper.SharePrefUtils
import com.ph32395.staynow.hieunt.widget.tap

class TenantInterestDialog(
    private val onClickButton : () -> Unit
): BaseFragmentDialog<DialogTenantInterestBinding>(false) {
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
                    SharePrefUtils(requireContext()).isReadTenantInterest = true
                    dismiss()
                } else {
                    dismiss()
                }
            }
        }
    }

    override fun initClickListener() {

    }

    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogTenantInterestBinding {
        return DialogTenantInterestBinding.inflate(layoutInflater)
    }
}