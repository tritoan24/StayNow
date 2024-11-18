package com.ph32395.staynow.hieunt.view.feature.manage_schedule_room.adapter

import android.view.ViewGroup
import com.ph32395.staynow.R
import com.ph32395.staynow.databinding.RenterItemRoomCanceledBinding
import com.ph32395.staynow.databinding.RenterItemRoomHaveNotSeenBinding
import com.ph32395.staynow.databinding.RenterItemRoomSeenBinding
import com.ph32395.staynow.databinding.RenterItemRoomWaitBinding
import com.ph32395.staynow.hieunt.base.BaseAdapter
import com.ph32395.staynow.hieunt.base.BaseViewHolder
import com.ph32395.staynow.hieunt.model.ScheduleRoomModel
import com.ph32395.staynow.hieunt.widget.layoutInflate
import com.ph32395.staynow.hieunt.widget.tap

class RenterManageScheduleRoomAdapter(
    private val onClickCancel: (ScheduleRoomModel) -> Unit,
    private val onClickConfirm: (ScheduleRoomModel) -> Unit,
    private val onClickDeposited: (ScheduleRoomModel) -> Unit,
    private val onClickWatched: (ScheduleRoomModel) -> Unit,
) : BaseAdapter<ScheduleRoomModel, BaseViewHolder<ScheduleRoomModel, *>>() {
    inner class RoomWaitVH(binding: RenterItemRoomWaitBinding) :
        BaseViewHolder<ScheduleRoomModel, RenterItemRoomWaitBinding>(binding) {
        override fun bindData(data: ScheduleRoomModel) {
            super.bindData(data)
            binding.apply {
                tvNameTenant.text = "Người thuê: ${data.renterName}"
                tvNameRoom.text = "Tên phòng: ${data.roomName}"
                tvPhoneNumber.text = "SDT: ${data.renterPhoneNumber}"
                tvTime.text = "Thời gian: ${data.time} ngày ${data.date}"
                tvConfirm.tap {
                    onClickConfirm.invoke(data)
                }
                tvCancel.tap {
                    onClickCancel.invoke(data)
                }
            }
        }
    }

    inner class RoomHaveNotSeenVH(binding: RenterItemRoomHaveNotSeenBinding) :
        BaseViewHolder<ScheduleRoomModel, RenterItemRoomHaveNotSeenBinding>(binding) {
        override fun bindData(data: ScheduleRoomModel) {
            super.bindData(data)
            binding.apply {
                tvNameTenant.text = "Người thuê: ${data.renterName}"
                tvNameRoom.text = "Tên phòng: ${data.roomName}"
                tvPhoneNumber.text = "SDT: ${data.renterPhoneNumber}"
                tvTime.text = "Thời gian: ${data.time} ngày ${data.date}"
                tvDeposited.tap {
                    onClickDeposited.invoke(data)
                }
                tvWatched.tap {
                    onClickWatched.invoke(data)
                }
            }
        }
    }

    inner class RoomSeenVH(binding: RenterItemRoomSeenBinding) :
        BaseViewHolder<ScheduleRoomModel, RenterItemRoomSeenBinding>(binding) {
        override fun bindData(data: ScheduleRoomModel) {
            super.bindData(data)
            binding.apply {
                tvNameTenant.text = "Người thuê: ${data.renterName}"
                tvNameRoom.text = "Tên phòng: ${data.roomName}"
                tvPhoneNumber.text = "SDT: ${data.renterPhoneNumber}"
                tvTime.text = "Thời gian: ${data.time} ngày ${data.date}"
            }
        }
    }

    inner class RoomCanceledVH(binding: RenterItemRoomCanceledBinding) :
        BaseViewHolder<ScheduleRoomModel, RenterItemRoomCanceledBinding>(binding) {
        override fun bindData(data: ScheduleRoomModel) {
            super.bindData(data)
            binding.apply {
                tvNameTenant.text = "Người thuê: ${data.renterName}"
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
        R.layout.renter_item_room_wait -> {
            RoomWaitVH(RenterItemRoomWaitBinding.inflate(parent.layoutInflate(), parent, false))
        }

        R.layout.renter_item_room_have_not_seen -> {
            RoomHaveNotSeenVH(
                RenterItemRoomHaveNotSeenBinding.inflate(
                    parent.layoutInflate(),
                    parent,
                    false
                )
            )
        }

        R.layout.renter_item_room_seen -> {
            RoomSeenVH(RenterItemRoomSeenBinding.inflate(parent.layoutInflate(), parent, false))
        }

        else -> {
            RoomCanceledVH(
                RenterItemRoomCanceledBinding.inflate(
                    parent.layoutInflate(),
                    parent,
                    false
                )
            )
        }
    }

    override fun layout(position: Int): Int = when (listData[position].status) {
        0 -> {
            R.layout.renter_item_room_wait
        }

        1 -> {
            R.layout.renter_item_room_have_not_seen
        }

        2 -> {
            R.layout.renter_item_room_seen
        }

        else -> {
            R.layout.renter_item_room_canceled
        }
    }
}