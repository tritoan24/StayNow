package com.ph32395.staynow.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import com.ph32395.staynow.databinding.FragmentProfileBinding
import com.ph32395.staynow.hieunt.base.BaseFragment
import com.ph32395.staynow.hieunt.view.feature.manage_schedule_room.TenantManageScheduleRoomActivity
import com.ph32395.staynow.hieunt.view_model.CommonVM
import com.ph32395.staynow.hieunt.widget.launchActivity
import com.ph32395.staynow.hieunt.widget.tap

class ProfileFragment : BaseFragment<FragmentProfileBinding,CommonVM>() {
    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentProfileBinding {
        return FragmentProfileBinding.inflate(layoutInflater)
    }

    override fun initView() {

    }

    override fun initClickListener() {
        binding.tvManageSchedule.tap {
            launchActivity(TenantManageScheduleRoomActivity::class.java)
        }
    }

    override fun initViewModel(): Class<CommonVM> = CommonVM::class.java

    override fun dataObserver() {

    }

}