package com.ph32395.staynow.hieunt.view.feature.manage_schedule_room

import android.view.ViewGroup
import com.ph32395.staynow.R
import com.ph32395.staynow.databinding.ItemRoomCanceledBinding
import com.ph32395.staynow.databinding.ItemRoomHaveNotSeenBinding
import com.ph32395.staynow.databinding.ItemRoomSeenBinding
import com.ph32395.staynow.databinding.ItemRoomWaitBinding
import com.ph32395.staynow.hieunt.base.BaseAdapter
import com.ph32395.staynow.hieunt.base.BaseViewHolder
import com.ph32395.staynow.hieunt.model.ScheduleRoomModel
import com.ph32395.staynow.hieunt.widget.layoutInflate

class ManageScheduleRoomAdapter : BaseAdapter<ScheduleRoomModel, BaseViewHolder<ScheduleRoomModel, *>>() {
    inner class RoomWaitVH(binding: ItemRoomWaitBinding): BaseViewHolder<ScheduleRoomModel, ItemRoomWaitBinding>(binding){
        override fun bindData(data: ScheduleRoomModel) {
            super.bindData(data)
        }
    }

    inner class RoomHaveNotSeenVH(binding: ItemRoomHaveNotSeenBinding): BaseViewHolder<ScheduleRoomModel, ItemRoomHaveNotSeenBinding>(binding){
        override fun bindData(data: ScheduleRoomModel) {
            super.bindData(data)
        }
    }

    inner class RoomSeenVH(binding: ItemRoomSeenBinding): BaseViewHolder<ScheduleRoomModel, ItemRoomSeenBinding>(binding){
        override fun bindData(data: ScheduleRoomModel) {
            super.bindData(data)
        }
    }

    inner class RoomCanceledVH(binding: ItemRoomCanceledBinding): BaseViewHolder<ScheduleRoomModel, ItemRoomCanceledBinding>(binding){
        override fun bindData(data: ScheduleRoomModel) {
            super.bindData(data)
        }
    }

    override fun viewHolder(
        viewType: Int,
        parent: ViewGroup
    ): BaseViewHolder<ScheduleRoomModel, *> = when(viewType) {
        R.layout.item_room_wait -> {
            RoomWaitVH(ItemRoomWaitBinding.inflate(parent.layoutInflate(),parent,false))
        }
        R.layout.item_room_have_not_seen -> {
            RoomHaveNotSeenVH(ItemRoomHaveNotSeenBinding.inflate(parent.layoutInflate(),parent,false))
        }
        R.layout.item_room_seen -> {
            RoomSeenVH(ItemRoomSeenBinding.inflate(parent.layoutInflate(),parent,false))
        }
        else -> {
            RoomCanceledVH(ItemRoomCanceledBinding.inflate(parent.layoutInflate(),parent,false))
        }
    }

    override fun layout(position: Int): Int = when (position) {
        0 -> {
            R.layout.item_room_wait
        }
        1 -> {
            R.layout.item_room_have_not_seen
        }
        2 -> {
            R.layout.item_room_seen
        }
        else -> {
            R.layout.item_room_canceled
        }
    }
}