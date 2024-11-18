package com.ph32395.staynow.hieunt.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.ph32395.staynow.R
import com.ph32395.staynow.hieunt.helper.SystemUtils
import com.ph32395.staynow.hieunt.widget.hideNavigation

abstract class BaseFragmentDialog<VB : ViewBinding>(private val isCancel: Boolean) : DialogFragment() {
    lateinit var binding: VB
    protected abstract fun initView()
    protected abstract fun initClickListener()
    protected abstract fun setViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        SystemUtils.setLocale(activity)
        binding = setViewBinding(inflater, container)
        isCancelable = isCancel
        initView()
        initClickListener()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.hideNavigation()
    }

    override fun getTheme(): Int {
        return R.style.BaseDialog
    }

    override fun onDetach() {
        dialog?.dismiss()
        dismiss()
        super.onDetach()
    }
}