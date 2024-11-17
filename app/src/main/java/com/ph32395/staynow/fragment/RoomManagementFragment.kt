package com.ph32395.staynow.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import com.ph32395.staynow.databinding.ActivityManageScheduleRoomBinding
import com.ph32395.staynow.databinding.FragmentRoomManagementBinding
import com.ph32395.staynow.hieunt.base.BaseFragment
import com.ph32395.staynow.hieunt.helper.Default.listScheduleState
import com.ph32395.staynow.hieunt.view.feature.manage_schedule_room.ScheduleStateAdapter
import com.ph32395.staynow.hieunt.view_model.ManageScheduleRoomVM

class RoomManagementFragment : BaseFragment<FragmentRoomManagementBinding, ManageScheduleRoomVM>() {
    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRoomManagementBinding {
        return FragmentRoomManagementBinding.inflate(inflater,container,false)
    }

    override fun initView() {
        binding.rvState.adapter = ScheduleStateAdapter { state ->

        }.apply { addList(listScheduleState) }
    }

    override fun initClickListener() {

    }

    override fun initViewModel(): Class<ManageScheduleRoomVM> = ManageScheduleRoomVM::class.java

    override fun dataObserver() {

    }

}