package com.ph32395.staynow.hieunt.view.feature.manage_schedule_room

import com.ph32395.staynow.databinding.ActivityManageScheduleRoomBinding
import com.ph32395.staynow.hieunt.base.BaseActivity
import com.ph32395.staynow.hieunt.helper.Default.listScheduleState
import com.ph32395.staynow.hieunt.view_model.ManageScheduleRoomVM

class ManageScheduleRoomActivity : BaseActivity<ActivityManageScheduleRoomBinding, ManageScheduleRoomVM>() {

    override fun setViewBinding(): ActivityManageScheduleRoomBinding {
        return ActivityManageScheduleRoomBinding.inflate(layoutInflater)
    }

    override fun initViewModel(): Class<ManageScheduleRoomVM> = ManageScheduleRoomVM::class.java

    override fun initView() {
        binding.rvState.adapter = ScheduleStateAdapter { state ->

        }.apply { addList(listScheduleState) }
    }

    override fun initClickListener() {

    }

    override fun dataObserver() {

    }

}