package com.ph32395.staynow.hieunt.view.dialog

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.view.ViewGroup
import com.ph32395.staynow.databinding.DialogWarningPermissionBinding
import com.ph32395.staynow.hieunt.base.BaseFragmentDialog
import com.ph32395.staynow.hieunt.widget.tap

class WarningPermissionDialog(private val goToSettingAction: () -> Unit) : BaseFragmentDialog<DialogWarningPermissionBinding>(true) {
    override fun setViewBinding(inflater: LayoutInflater, container: ViewGroup?): DialogWarningPermissionBinding =
        DialogWarningPermissionBinding.inflate(inflater, container, false)

    override fun initView() {

    }

    override fun initClickListener() {
        binding.tvGoToSetting.tap {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts(
                "package",
                requireActivity().applicationContext.packageName,
                null
            )
            intent.data = uri
            startActivity(intent)
            goToSettingAction.invoke()
            dismiss()
        }
    }
}