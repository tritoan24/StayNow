package com.ph32395.staynow.hieunt.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import com.ph32395.staynow.R
import com.ph32395.staynow.hieunt.helper.SystemUtils

abstract class BaseDialog<VB : ViewBinding>(context: Context, private val isCancel: Boolean) :
    Dialog(context, R.style.BaseDialog) {
    lateinit var binding: VB
    protected abstract fun initView()
    protected abstract fun initClickListener()
    protected abstract fun setViewBinding(inflater: LayoutInflater): VB

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        SystemUtils.setLocale(context)
        binding = setViewBinding(layoutInflater)
        setCancelable(isCancel)
        initView()
        initClickListener()
        setContentView(binding.root)
    }

}