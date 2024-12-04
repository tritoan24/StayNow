package com.ph32395.staynow.hieunt.view.feature.manage_schedule_room

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.ph32395.staynow.databinding.ActivityTenantManageScheduleRoomBinding
import com.ph32395.staynow.hieunt.base.BaseActivity
import com.ph32395.staynow.hieunt.helper.Default.NotificationTitle.TITLE_CANCELED_BY_TENANT
import com.ph32395.staynow.hieunt.helper.Default.NotificationTitle.TITLE_CONFIRMED
import com.ph32395.staynow.hieunt.helper.Default.NotificationTitle.TITLE_LEAVED_BY_TENANT
import com.ph32395.staynow.hieunt.helper.Default.StatusRoom.CANCELED
import com.ph32395.staynow.hieunt.helper.Default.StatusRoom.CONFIRMED
import com.ph32395.staynow.hieunt.helper.Default.StatusRoom.WAIT
import com.ph32395.staynow.hieunt.helper.Default.StatusRoom.WATCHED
import com.ph32395.staynow.hieunt.helper.Default.listScheduleState
import com.ph32395.staynow.hieunt.view.dialog.UpdateRoomScheduleDialog
import com.ph32395.staynow.hieunt.view.feature.manage_schedule_room.adapter.ScheduleStateAdapter
import com.ph32395.staynow.hieunt.view.feature.manage_schedule_room.adapter.TenantManageScheduleRoomAdapter
import com.ph32395.staynow.hieunt.view_model.ManageScheduleRoomVM
import com.ph32395.staynow.hieunt.widget.gone
import com.ph32395.staynow.hieunt.widget.tap
import com.ph32395.staynow.hieunt.widget.toast
import com.ph32395.staynow.hieunt.widget.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class TenantManageScheduleRoomActivity :
    BaseActivity<ActivityTenantManageScheduleRoomBinding, ManageScheduleRoomVM>() {
    private var manageScheduleRoomAdapter: TenantManageScheduleRoomAdapter? = null
    private var scheduleStateAdapter: ScheduleStateAdapter? = null
    override fun setViewBinding(): ActivityTenantManageScheduleRoomBinding {
        return ActivityTenantManageScheduleRoomBinding.inflate(layoutInflater)
    }

    override fun initViewModel(): Class<ManageScheduleRoomVM> = ManageScheduleRoomVM::class.java

    override fun initView() {
        scheduleStateAdapter = ScheduleStateAdapter { status ->
            viewModel.filerScheduleRoomState(status)
        }.apply {
            addList(listScheduleState)
            setSelectedState(WAIT)
        }

        manageScheduleRoomAdapter = TenantManageScheduleRoomAdapter(
            onClickWatched = {
                updateStatusRoom(it.roomScheduleId, WATCHED)
            },
            onClickCancelSchedule = {
                updateStatusRoom(it.roomScheduleId, CANCELED)
                viewModel.pushNotification(TITLE_CANCELED_BY_TENANT, it, false) { isCompletion ->
                    toastNotification(isCompletion)
                }
            },
            onClickLeaveSchedule = {
                UpdateRoomScheduleDialog(it, onClickConfirm = { newTime, newDate ->
                    showLoading()
                    viewModel.updateScheduleRoom(
                        it.roomScheduleId,
                        newTime,
                        newDate
                    ) { updateSuccess ->
                        if (updateSuccess) {
                            viewModel.filerScheduleRoomState(0) {
                                scheduleStateAdapter?.setSelectedState(0)
                            }
                            viewModel.pushNotification(
                                TITLE_LEAVED_BY_TENANT,
                                it.copy(time = newTime, date = newDate)
                                ,false
                            ) { isCompletion ->
                                toastNotification(isCompletion)
                            }
                        } else {
                            lifecycleScope.launch {
                                toast("Có lỗi xảy ra!")
                            }
                        }
                    }
                }).show(supportFragmentManager, "UpdateRoomScheduleDialog")
            },
            onClickConfirm = {
                updateStatusRoom(it.roomScheduleId, CONFIRMED)
                viewModel.pushNotification(TITLE_CONFIRMED, it, false) { isCompletion ->
                    toastNotification(isCompletion)
                }
            }
        )

        binding.rvState.adapter = scheduleStateAdapter
        binding.rvRoomState.adapter = manageScheduleRoomAdapter
    }

    override fun initClickListener() {
        binding.ivBack.tap {
            finish()
        }
    }

    override fun dataObserver() {
        showLoading()
        viewModel.fetchAllScheduleByTenant(FirebaseAuth.getInstance().currentUser?.uid.toString()) {
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
                        dismissLoading()
                    }
                }
                launch {
                    viewModel.allScheduleRoomState.collect { allRoomStates ->
                        val newList = async(Dispatchers.IO) {
                            scheduleStateAdapter?.listData?.map { scheduleState ->
                                val count = allRoomStates.filter { room -> room.status == scheduleState.status }.size
                                scheduleState.copy(count = count)
                            }
                        }.await()
                        scheduleStateAdapter?.addListObserver(newList ?: listScheduleState)
                    }
                }
            }
        }
    }

    private fun updateStatusRoom(roomScheduleId: String, status: Int) {
        showLoading()
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

    private fun toastNotification(isCompletion: Boolean) {
        lifecycleScope.launch {
            if (isCompletion)
                toast("Thông báo đã được gửi đến chủ trọ")
            else
                toast("Có lỗi xảy ra!")
        }
    }

}