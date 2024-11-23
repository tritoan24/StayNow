package com.ph32395.staynow.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.ph32395.staynow.databinding.FragmentRoomManagementBinding
import com.ph32395.staynow.hieunt.base.BaseFragment
import com.ph32395.staynow.hieunt.helper.Default.NotificationTitle.TITLE_CANCELED_BY_RENTER
import com.ph32395.staynow.hieunt.helper.Default.NotificationTitle.TITLE_CONFIRMED
import com.ph32395.staynow.hieunt.helper.Default.NotificationTitle.TITLE_LEAVED_BY_RENTER
import com.ph32395.staynow.hieunt.helper.Default.StatusRoom.CANCELED
import com.ph32395.staynow.hieunt.helper.Default.StatusRoom.CONFIRMED
import com.ph32395.staynow.hieunt.helper.Default.StatusRoom.WAIT
import com.ph32395.staynow.hieunt.helper.Default.StatusRoom.WATCHED
import com.ph32395.staynow.hieunt.helper.Default.listScheduleState
import com.ph32395.staynow.hieunt.view.dialog.UpdateRoomScheduleDialog
import com.ph32395.staynow.hieunt.view.feature.manage_schedule_room.adapter.RenterManageScheduleRoomAdapter
import com.ph32395.staynow.hieunt.view.feature.manage_schedule_room.adapter.ScheduleStateAdapter
import com.ph32395.staynow.hieunt.view_model.ManageScheduleRoomVM
import com.ph32395.staynow.hieunt.widget.gone
import com.ph32395.staynow.hieunt.widget.toast
import com.ph32395.staynow.hieunt.widget.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class RoomManagementFragment : BaseFragment<FragmentRoomManagementBinding, ManageScheduleRoomVM>() {
    private var manageScheduleRoomAdapter: RenterManageScheduleRoomAdapter? = null
    private var scheduleStateAdapter: ScheduleStateAdapter? = null
    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRoomManagementBinding {
        return FragmentRoomManagementBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        scheduleStateAdapter = ScheduleStateAdapter { status ->
            viewModel.filerScheduleRoomState(status)
        }.apply {
            addList(listScheduleState)
            setSelectedState(WAIT)
        }

        manageScheduleRoomAdapter = RenterManageScheduleRoomAdapter(
            onClickWatched = {
                updateStatusRoom(it.roomScheduleId, WATCHED)
            },
            onClickConfirm = { data ->
                updateStatusRoom(data.roomScheduleId, CONFIRMED)
                viewModel.pushNotification(TITLE_CONFIRMED, data){ isCompletion ->
                    toastNotification(isCompletion)
                }
            },
            onClickLeaveSchedule = {
                UpdateRoomScheduleDialog(it, onClickConfirm = { newTime, newDate ->
                    showLoadingIfNotBaseActivity()
                    viewModel.updateScheduleRoom(
                        it.roomScheduleId,
                        newTime,
                        newDate,
                        isChangedScheduleByRenter = true
                    ) { updateSuccess ->
                        if (updateSuccess) {
                            viewModel.filerScheduleRoomState(0) {
                                scheduleStateAdapter?.setSelectedState(0)
                            }
                            viewModel.pushNotification(TITLE_LEAVED_BY_RENTER, it){ isCompletion ->
                                toastNotification(isCompletion)
                            }
                        } else {
                            lifecycleScope.launch {
                                toast("Có lỗi xảy ra!")
                            }
                        }
                    }
                }).show(childFragmentManager, "UpdateRoomScheduleDialog")
            },
            onClickCancelSchedule = {
                updateStatusRoom(it.roomScheduleId, CANCELED)
                viewModel.pushNotification(TITLE_CANCELED_BY_RENTER, it){ isCompletion ->
                    toastNotification(isCompletion)
                }
            },
            onClickCreateContract = {

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
        viewModel.fetchAllScheduleByRenter(FirebaseAuth.getInstance().currentUser?.uid.toString()) {
            viewModel.filerScheduleRoomState(WAIT)
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
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
                launch {
                    viewModel.allScheduleRoomState.collect {allRoomStates->
                        val newList = async(Dispatchers.IO) {
                            listScheduleState.map { scheduleState ->
                                val count = allRoomStates.filter { room -> room.status == scheduleState.status }.size
                                scheduleState.copy(count = count)
                            }
                        }.await()
                        scheduleStateAdapter?.addListObserver(newList)
                    }
                }
            }
        }
    }

    private fun updateStatusRoom(roomScheduleId: String, status: Int) {
        showLoadingIfNotBaseActivity()
        viewModel.updateScheduleRoomStatus(roomScheduleId, status) { updateSuccess ->
            if (updateSuccess) {
                viewModel.filerScheduleRoomState(status) {
                    scheduleStateAdapter?.setSelectedState(status)
                    if (status == 3) binding.rvState.scrollToPosition(status)
                }
            } else {
                lifecycleScope.launch {
                    toast("Có lỗi xảy ra!")
                }
            }
        }
    }

    private fun toastNotification (isCompletion: Boolean){
        lifecycleScope.launch {
            if (isCompletion)
                toast("Thông báo đã được gửi đến người thuê")
            else
                toast("Có lỗi xảy ra!")
        }
    }

}