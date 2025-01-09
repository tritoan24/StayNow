package com.ph32395.staynow_datn.hieunt.view.feature.manage_schedule_room

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.ph32395.staynow_datn.databinding.ActivityTenantManageScheduleRoomBinding
import com.ph32395.staynow_datn.hieunt.base.BaseActivity
import com.ph32395.staynow_datn.hieunt.helper.Default.NotificationTitle.TITLE_CANCELED_BY_OVER_TIME
import com.ph32395.staynow_datn.hieunt.helper.Default.NotificationTitle.TITLE_CANCELED_BY_TENANT
import com.ph32395.staynow_datn.hieunt.helper.Default.NotificationTitle.TITLE_CONFIRMED
import com.ph32395.staynow_datn.hieunt.helper.Default.NotificationTitle.TITLE_LEAVED_BY_TENANT
import com.ph32395.staynow_datn.hieunt.helper.Default.StatusRoom.CANCELED
import com.ph32395.staynow_datn.hieunt.helper.Default.StatusRoom.CONFIRMED
import com.ph32395.staynow_datn.hieunt.helper.Default.StatusRoom.WAIT
import com.ph32395.staynow_datn.hieunt.helper.Default.StatusRoom.WATCHED
import com.ph32395.staynow_datn.hieunt.helper.Default.listScheduleState
import com.ph32395.staynow_datn.hieunt.view.dialog.UpdateRoomScheduleDialog
import com.ph32395.staynow_datn.hieunt.view.feature.manage_schedule_room.adapter.ScheduleStateAdapter
import com.ph32395.staynow_datn.hieunt.view.feature.manage_schedule_room.adapter.TenantManageScheduleRoomAdapter
import com.ph32395.staynow_datn.hieunt.view_model.ManageScheduleRoomVM
import com.ph32395.staynow_datn.hieunt.widget.gone
import com.ph32395.staynow_datn.hieunt.widget.tap
import com.ph32395.staynow_datn.hieunt.widget.toast
import com.ph32395.staynow_datn.hieunt.widget.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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
                updateStatusRoom(it.maDatPhong, WATCHED)
            },
            onClickCancelSchedule = {
                updateStatusRoom(it.maDatPhong, CANCELED)
                viewModel.pushNotification(TITLE_CANCELED_BY_TENANT, it, false) { isCompletion ->
                    toastNotification(isCompletion)
                }
            },
            onClickLeaveSchedule = {
                UpdateRoomScheduleDialog(it, onClickConfirm = { newTime, newDate ->
                    showLoading()
                    viewModel.updateScheduleRoom(
                        it.maDatPhong,
                        newTime,
                        newDate
                    ) { updateSuccess ->
                        if (updateSuccess) {
                            viewModel.filerScheduleRoomState(0) {
                                scheduleStateAdapter?.setSelectedState(0)
                            }
                            viewModel.pushNotification(
                                TITLE_LEAVED_BY_TENANT,
                                it.copy(thoiGianDatPhong = newTime, ngayDatPhong = newDate)
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
                updateStatusRoom(it.maDatPhong, CONFIRMED)
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun dataObserver() {
        showLoading()
        viewModel.fetchAllScheduleByTenant(FirebaseAuth.getInstance().currentUser?.uid.toString()) {
            viewModel.filerScheduleRoomState(0)
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
                        launch {
                            val filteredList = allRoomStates.filter { schedule ->
                                val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                                val currentDate = LocalDate.now()

                                val ngayDatPhong = try {
                                    LocalDate.parse(schedule.ngayDatPhong, formatter)
                                } catch (e: Exception) {
                                    null
                                }
                                ngayDatPhong?.let { ngayDat ->
                                    val ngayDatCong3Ngay = ngayDat.plusDays(3)
                                    ngayDatCong3Ngay.isBefore(currentDate) && schedule.trangThaiDatPhong == 1
                                } ?: false
                            }
                            Log.d("filteredList", "filteredList: $filteredList")
                            if (filteredList.isNotEmpty()){
                                filteredList.forEach {
                                    updateStatusRoom(it.maDatPhong, CANCELED)
                                    viewModel.pushNotification(TITLE_CANCELED_BY_OVER_TIME, it, true)
                                    viewModel.pushNotification(TITLE_CANCELED_BY_OVER_TIME, it, false) { _ ->
                                        toast("Huỷ lịch hẹn thành công!")
                                    }
                                }
                            }
                        }
                        launch {
                            val newList = async(Dispatchers.IO) {
                                listScheduleState.map { scheduleState ->
                                    val count = allRoomStates.filter { room -> room.trangThaiDatPhong == scheduleState.status }.size
                                    scheduleState.copy(count = count)
                                }
                            }.await()
                            scheduleStateAdapter?.addListObserver(newList)
                        }
                    }
                }
            }
        }
    }

    private fun updateStatusRoom(maDatPhong: String, status: Int) {
        showLoading()
        viewModel.updateScheduleRoomStatus(maDatPhong, status) { updateSuccess ->
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