package com.ph32395.staynow.hieunt.view.feature.manage_schedule_room

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.ph32395.staynow.databinding.ActivityTenantManageScheduleRoomBinding
import com.ph32395.staynow.hieunt.base.BaseActivity
import com.ph32395.staynow.hieunt.helper.Default.StatusRoom.CANCEL
import com.ph32395.staynow.hieunt.helper.Default.StatusRoom.SEEN
import com.ph32395.staynow.hieunt.helper.Default.StatusRoom.WAIT
import com.ph32395.staynow.hieunt.helper.Default.listScheduleState
import com.ph32395.staynow.hieunt.view.feature.manage_schedule_room.adapter.ScheduleStateAdapter
import com.ph32395.staynow.hieunt.view.feature.manage_schedule_room.adapter.TenantManageScheduleRoomAdapter
import com.ph32395.staynow.hieunt.view_model.ManageScheduleRoomVM
import com.ph32395.staynow.hieunt.widget.gone
import com.ph32395.staynow.hieunt.widget.toast
import com.ph32395.staynow.hieunt.widget.visible
import kotlinx.coroutines.launch

class TenantManageScheduleRoomActivity : BaseActivity<ActivityTenantManageScheduleRoomBinding, ManageScheduleRoomVM>() {
    private var manageScheduleRoomAdapter: TenantManageScheduleRoomAdapter? = null
    private var scheduleStateAdapter : ScheduleStateAdapter?= null
    override fun setViewBinding(): ActivityTenantManageScheduleRoomBinding {
        return ActivityTenantManageScheduleRoomBinding.inflate(layoutInflater)
    }

    override fun initViewModel(): Class<ManageScheduleRoomVM> = ManageScheduleRoomVM::class.java

    override fun initView() {
        scheduleStateAdapter =  ScheduleStateAdapter { status ->
            viewModel.filerScheduleRoomState(status)
        }.apply {
            addList(listScheduleState)
            setSelectedState(WAIT)
        }

        manageScheduleRoomAdapter = TenantManageScheduleRoomAdapter(
            onClickCancel = {
                updateStatusRoom(it.roomScheduleId, CANCEL)
            },
            onClickGoToRoom = {

            },
            onClickWatched = {
                updateStatusRoom(it.roomScheduleId, SEEN)
            }
        )

        binding.rvState.adapter = scheduleStateAdapter
        binding.rvRoomState.adapter = manageScheduleRoomAdapter
    }

    override fun initClickListener() {

    }

    override fun dataObserver() {
        showLoading()
        viewModel.fetchAllScheduleByTenant(FirebaseAuth.getInstance().currentUser?.uid.toString()){
            viewModel.filerScheduleRoomState(WAIT)
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.scheduleRoomState.collect {
                    if (it.isNotEmpty()) {
                        binding.tvNoData.gone()
                    } else {
                        binding.tvNoData.visible()
                    }
                    manageScheduleRoomAdapter?.addListObserver(it)
                    dismissLoading()
                }
            }
        }
    }

    private fun updateStatusRoom (roomScheduleId: String, status: Int){
        showLoading()
        viewModel.updateScheduleRoomStatus(roomScheduleId, status) { updateSuccess ->
            if (updateSuccess){
                viewModel.filerScheduleRoomState(status){
                    scheduleStateAdapter?.setSelectedState(status)
                }
            } else {
                lifecycleScope.launch {
                    toast("Có lỗi khi hủy!")
                }
            }
        }
    }

}