package com.ph32395.staynow.hieunt.view.dialog

import android.content.Context
import android.view.LayoutInflater
import com.ph32395.staynow.databinding.DialogLoadingBinding
import com.ph32395.staynow.hieunt.base.BaseDialog

class LoadingDialog(context: Context) : BaseDialog<DialogLoadingBinding>(context, false) {
    override fun initView() {

    }

    override fun initClickListener() {

    }

    override fun setViewBinding(
        inflater: LayoutInflater,
    ): DialogLoadingBinding {
        return DialogLoadingBinding.inflate(inflater)
    }
}