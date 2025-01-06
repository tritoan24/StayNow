package com.ph32395.staynow_datn.hieunt.view.feature.schedule_room

import android.annotation.SuppressLint
import com.ph32395.staynow_datn.MainActivity
import com.ph32395.staynow_datn.databinding.ActivityScheduleRoomSuccessBinding
import com.ph32395.staynow_datn.hieunt.base.BaseActivity
import com.ph32395.staynow_datn.hieunt.helper.Default.IntentKeys.ROOM_SCHEDULE
import com.ph32395.staynow_datn.hieunt.model.ScheduleRoomModel
import com.ph32395.staynow_datn.hieunt.view_model.CommonVM
import com.ph32395.staynow_datn.hieunt.widget.currentBundle
import com.ph32395.staynow_datn.hieunt.widget.launchActivity
import com.ph32395.staynow_datn.hieunt.widget.tap

class ScheduleRoomSuccessActivity : BaseActivity<ActivityScheduleRoomSuccessBinding, CommonVM>() {
    override fun setViewBinding(): ActivityScheduleRoomSuccessBinding {
        return ActivityScheduleRoomSuccessBinding.inflate(layoutInflater)
    }

    override fun initViewModel(): Class<CommonVM> = CommonVM::class.java

    @SuppressLint("SetTextI18n")
    override fun initView() {
        val scheduleRoomModel = currentBundle()?.getSerializable(ROOM_SCHEDULE) as? ScheduleRoomModel
        binding.apply {
            tvCancel.tap {
                launchActivity(MainActivity::class.java)
                finishAffinity()
            }
            tvNameRoom.text = scheduleRoomModel?.tenPhong ?: "Không có tên"
            tvNameTenant.text = scheduleRoomModel?.tenNguoiThue ?: "Không có tên"
            tvPhoneNumberTenant.text = scheduleRoomModel?.sdtNguoiThue ?: "Không có SDT"
            scheduleRoomModel?.let { tvTime.text = "${it.thoiGianDatPhong}, ${it.ngayDatPhong}" } ?: "Không có thời gian"
        }
    }

    override fun initClickListener() {

    }

    override fun dataObserver() {

    }

}