package com.ph32395.staynow.hieunt.view.feature.no_internet

import com.ph32395.staynow.databinding.ActivityNoInternetBinding
import com.ph32395.staynow.hieunt.base.BaseActivity
import com.ph32395.staynow.hieunt.view_model.CommonVM

class NoInternetActivity : BaseActivity<ActivityNoInternetBinding, CommonVM>() {
    override fun setViewBinding(): ActivityNoInternetBinding {
        return ActivityNoInternetBinding.inflate(layoutInflater)
    }

    override fun initViewModel(): Class<CommonVM> = CommonVM::class.java

    override fun initView() {

    }

    override fun dataObserver() {

    }

}