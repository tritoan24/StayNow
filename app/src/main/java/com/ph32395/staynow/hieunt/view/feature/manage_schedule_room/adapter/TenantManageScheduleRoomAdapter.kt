package com.ph32395.staynow.hieunt.view.feature.manage_schedule_room.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import com.ph32395.staynow.R
import com.ph32395.staynow.databinding.TenantItemRoomCanceledBinding
import com.ph32395.staynow.databinding.TenantItemRoomConfirmedBinding
import com.ph32395.staynow.databinding.TenantItemRoomSeenBinding
import com.ph32395.staynow.databinding.TenantItemRoomWaitBinding
import com.ph32395.staynow.hieunt.base.BaseAdapter
import com.ph32395.staynow.hieunt.base.BaseViewHolder
import com.ph32395.staynow.hieunt.model.ScheduleRoomModel
import com.ph32395.staynow.hieunt.widget.gone
import com.ph32395.staynow.hieunt.widget.layoutInflate
import com.ph32395.staynow.hieunt.widget.tap
import com.ph32395.staynow.hieunt.widget.visible

@SuppressLint("SetTextI18n")
class TenantManageScheduleRoomAdapter(
    private val onClickCancelSchedule: (ScheduleRoomModel) -> Unit,
    private val onClickLeaveSchedule: (ScheduleRoomModel) -> Unit,
    private val onClickWatched: (ScheduleRoomModel) -> Unit,
    private val onClickConfirm: (ScheduleRoomModel) -> Unit,
) : BaseAdapter<ScheduleRoomModel, BaseViewHolder<ScheduleRoomModel, *>>() {
    inner class RoomWaitVH(binding: TenantItemRoomWaitBinding) :
        BaseViewHolder<ScheduleRoomModel, TenantItemRoomWaitBinding>(binding) {
        override fun bindData(data: ScheduleRoomModel) {
            super.bindData(data)
            binding.apply {
                if (data.changedScheduleByRenter) {
                    tvNotification.visible()
                    llYesOrNo.visible()
                    llCancelAndLeave.gone()
                } else {
                    tvNotification.gone()
                    llYesOrNo.gone()
                    llCancelAndLeave.visible()
                }
                tvNameRenter.text = "Chủ trọ: ${data.renterName}"
                tvNameRoom.text = "Tên phòng: ${data.roomName}"
                tvPhoneNumber.text = "SDT: ${data.renterPhoneNumber}"
                tvTime.text = "Thời gian: ${data.time} ngày ${data.date}"
                tvCancelSchedule.tap {
                    onClickCancelSchedule.invoke(data)
                }
                tvLeaveSchedule.tap {
                    onClickLeaveSchedule.invoke(data)
                }
                tvNo.tap {
                    onClickCancelSchedule.invoke(data)
                }
                tvYes.tap {
                    onClickConfirm.invoke(data)
                }
            }
        }
    }

    inner class RoomConfirmedVH(binding: TenantItemRoomConfirmedBinding) :
        BaseViewHolder<ScheduleRoomModel, TenantItemRoomConfirmedBinding>(binding) {
        override fun bindData(data: ScheduleRoomModel) {
            super.bindData(data)
            binding.apply {
                tvNameRenter.text = "Chủ trọ: ${data.renterName}"
                tvNameRoom.text = "Tên phòng: ${data.roomName}"
                tvPhoneNumber.text = "SDT: ${data.renterPhoneNumber}"
                tvTime.text = "Thời gian: ${data.time} ngày ${data.date}"
                tvWatched.tap {
                    onClickWatched.invoke(data)
                }
                tvLeaveSchedule.tap {
                    onClickLeaveSchedule.invoke(data)
                }
                tvCancelSchedule.tap {
                    onClickCancelSchedule.invoke(data)
                }
            }
        }
    }

    inner class RoomSeenVH(binding: TenantItemRoomSeenBinding) :
        BaseViewHolder<ScheduleRoomModel, TenantItemRoomSeenBinding>(binding) {
        override fun bindData(data: ScheduleRoomModel) {
            super.bindData(data)
            binding.apply {
                tvNameRenter.text = "Chủ trọ: ${data.renterName}"
                tvNameRoom.text = "Tên phòng: ${data.roomName}"
                tvPhoneNumber.text = "SDT: ${data.renterPhoneNumber}"
                tvTime.text = "Thời gian: ${data.time} ngày ${data.date}"
            }
        }
    }

    inner class RoomCanceledVH(binding: TenantItemRoomCanceledBinding) :
        BaseViewHolder<ScheduleRoomModel, TenantItemRoomCanceledBinding>(binding) {
        override fun bindData(data: ScheduleRoomModel) {
            super.bindData(data)
            binding.apply {
                tvNameRenter.text = "Chủ trọ: ${data.renterName}"
                tvNameRoom.text = "Tên phòng: ${data.roomName}"
                tvPhoneNumber.text = "SDT: ${data.renterPhoneNumber}"
                tvTime.text = "Thời gian: ${data.time} ngày ${data.date}"
            }
        }
    }

    override fun viewHolder(
        viewType: Int,
        parent: ViewGroup
    ): BaseViewHolder<ScheduleRoomModel, *> = when (viewType) {
        R.layout.tenant_item_room_wait -> {
            RoomWaitVH(TenantItemRoomWaitBinding.inflate(parent.layoutInflate(), parent, false))
        }

        R.layout.tenant_item_room_confirmed -> {
            RoomConfirmedVH(
                TenantItemRoomConfirmedBinding.inflate(
                    parent.layoutInflate(),
                    parent,
                    false
                )
            )
        }

        R.layout.tenant_item_room_seen -> {
            RoomSeenVH(TenantItemRoomSeenBinding.inflate(parent.layoutInflate(), parent, false))
        }

        else -> {
            RoomCanceledVH(
                TenantItemRoomCanceledBinding.inflate(
                    parent.layoutInflate(),
                    parent,
                    false
                )
            )
        }
    }

    override fun layout(position: Int): Int = when (listData[position].status) {
        0 -> {
            R.layout.tenant_item_room_wait
        }

        1 -> {
            R.layout.tenant_item_room_confirmed
        }

        2 -> {
            R.layout.tenant_item_room_seen
        }

        else -> {
            R.layout.tenant_item_room_canceled
        }
    }
}