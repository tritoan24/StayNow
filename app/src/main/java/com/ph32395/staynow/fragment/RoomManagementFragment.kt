package com.ph32395.staynow.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.ph32395.staynow.databinding.FragmentRoomManagementBinding
import com.ph32395.staynow.hieunt.base.BaseFragment
import com.ph32395.staynow.hieunt.helper.Default.StatusRoom.CANCEL
import com.ph32395.staynow.hieunt.helper.Default.StatusRoom.HAVE_NOT_SEEN
import com.ph32395.staynow.hieunt.helper.Default.StatusRoom.SEEN
import com.ph32395.staynow.hieunt.helper.Default.StatusRoom.WAIT
import com.ph32395.staynow.hieunt.helper.Default.listScheduleState
import com.ph32395.staynow.hieunt.view.feature.manage_schedule_room.adapter.RenterManageScheduleRoomAdapter
import com.ph32395.staynow.hieunt.view.feature.manage_schedule_room.adapter.ScheduleStateAdapter
import com.ph32395.staynow.hieunt.view_model.ManageScheduleRoomVM
import com.ph32395.staynow.hieunt.widget.gone
import com.ph32395.staynow.hieunt.widget.toast
import com.ph32395.staynow.hieunt.widget.visible
import kotlinx.coroutines.launch

class RoomManagementFragment : BaseFragment<FragmentRoomManagementBinding, ManageScheduleRoomVM>() {
    private var manageScheduleRoomAdapter: RenterManageScheduleRoomAdapter? = null
    private var scheduleStateAdapter : ScheduleStateAdapter ?= null
    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRoomManagementBinding {
        return FragmentRoomManagementBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        scheduleStateAdapter =  ScheduleStateAdapter { status ->
            viewModel.filerScheduleRoomState(status)
        }.apply {
            addList(listScheduleState)
            setSelectedState(WAIT)
        }

        manageScheduleRoomAdapter = RenterManageScheduleRoomAdapter(
            onClickCancel = {
                updateStatusRoom(it.roomScheduleId, CANCEL)
            },
            onClickConfirm = {
                updateStatusRoom(it.roomScheduleId, HAVE_NOT_SEEN)
            },
            onClickDeposited = {
                updateStatusRoom(it.roomScheduleId, SEEN)
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

    override fun initViewModel(): Class<ManageScheduleRoomVM> = ManageScheduleRoomVM::class.java

    override fun dataObserver() {
        showLoadingIfNotBaseActivity()
        viewModel.fetchAllScheduleByUser(FirebaseAuth.getInstance().currentUser?.uid.toString()){
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
                    dismissLoadingIfNotBaseActivity()
                }
            }
        }
    }

    private fun updateStatusRoom (roomScheduleId: String, status: Int){
        showLoadingIfNotBaseActivity()
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