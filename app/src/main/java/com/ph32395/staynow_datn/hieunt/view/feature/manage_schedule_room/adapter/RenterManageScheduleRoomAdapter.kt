package com.ph32395.staynow_datn.hieunt.view.feature.manage_schedule_room.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.databinding.RenterItemRoomCanceledBinding
import com.ph32395.staynow_datn.databinding.RenterItemRoomConfirmedBinding
import com.ph32395.staynow_datn.databinding.RenterItemRoomSeenBinding
import com.ph32395.staynow_datn.databinding.RenterItemRoomWaitBinding
import com.ph32395.staynow_datn.hieunt.base.BaseAdapter
import com.ph32395.staynow_datn.hieunt.base.BaseViewHolder
import com.ph32395.staynow_datn.hieunt.model.ScheduleRoomModel
import com.ph32395.staynow_datn.hieunt.widget.layoutInflate
import com.ph32395.staynow_datn.hieunt.widget.tap


@SuppressLint("SetTextI18n")
class RenterManageScheduleRoomAdapter(
    private val onClickCancelSchedule: (ScheduleRoomModel) -> Unit,
    private val onClickLeaveSchedule: (ScheduleRoomModel) -> Unit,
    private val onClickConfirm: (ScheduleRoomModel) -> Unit,
    private val onClickCreateContract: (ScheduleRoomModel) -> Unit,
    private val onClickWatched: (ScheduleRoomModel) -> Unit,
) : BaseAdapter<ScheduleRoomModel, BaseViewHolder<ScheduleRoomModel, *>>() {
    inner class RoomWaitVH(binding: RenterItemRoomWaitBinding) :
        BaseViewHolder<ScheduleRoomModel, RenterItemRoomWaitBinding>(binding) {
        override fun bindData(data: ScheduleRoomModel) {
            super.bindData(data)
            binding.apply {
                tvNameTenant.text = "Người thuê: ${data.tenNguoiThue}"
                tvNameRoom.text = "Tên phòng: ${data.tenPhong}"
                tvPhoneNumber.text = "SDT: ${data.sdtNguoiThue}"
                tvTime.text = "Thời gian: ${data.thoiGianDatPhong} ngày ${data.ngayDatPhong}"

                tvConfirm.tap {
                    onClickConfirm.invoke(data)
                }
                tvCancel.tap {
                    onClickCancelSchedule.invoke(data)
                }
                tvLeaveSchedule.tap {
                    onClickLeaveSchedule.invoke(data)
                }
            }
        }
    }

    inner class RoomConfirmedVH(binding: RenterItemRoomConfirmedBinding) :
        BaseViewHolder<ScheduleRoomModel, RenterItemRoomConfirmedBinding>(binding) {
        override fun bindData(data: ScheduleRoomModel) {
            super.bindData(data)
            binding.apply {
                tvNameTenant.text = "Người thuê: ${data.tenNguoiThue}"
                tvNameRoom.text = "Tên phòng: ${data.tenPhong}"
                tvPhoneNumber.text = "SDT: ${data.sdtNguoiThue}"
                tvTime.text = "Thời gian: ${data.thoiGianDatPhong} ngày ${data.ngayDatPhong}"
                tvCreateContract.tap {
                    onClickCreateContract.invoke(data)
                }
                tvWatched.tap {
                    onClickWatched.invoke(data)
                }
                tvCancelSchedule.tap {
                    onClickCancelSchedule.invoke(data)
                }
                tvLeaveSchedule.tap {
                    onClickLeaveSchedule.invoke(data)
                }
            }
        }
    }

    inner class RoomSeenVH(binding: RenterItemRoomSeenBinding) :
        BaseViewHolder<ScheduleRoomModel, RenterItemRoomSeenBinding>(binding) {
        override fun bindData(data: ScheduleRoomModel) {
            super.bindData(data)
            binding.apply {
                tvNameTenant.text = "Người thuê: ${data.tenNguoiThue}"
                tvNameRoom.text = "Tên phòng: ${data.tenPhong}"
                tvPhoneNumber.text = "SDT: ${data.sdtNguoiThue}"
                tvTime.text = "Thời gian: ${data.thoiGianDatPhong} ngày ${data.ngayDatPhong}"
            }
        }
    }

    inner class RoomCanceledVH(binding: RenterItemRoomCanceledBinding) :
        BaseViewHolder<ScheduleRoomModel, RenterItemRoomCanceledBinding>(binding) {
        override fun bindData(data: ScheduleRoomModel) {
            super.bindData(data)
            binding.apply {
                tvNameTenant.text = "Người thuê: ${data.tenNguoiThue}"
                tvNameRoom.text = "Tên phòng: ${data.tenPhong}"
                tvPhoneNumber.text = "SDT: ${data.sdtNguoiThue}"
                tvTime.text = "Thời gian: ${data.thoiGianDatPhong} ngày ${data.ngayDatPhong}"
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

        R.layout.renter_item_room_confirmed -> {
            RoomConfirmedVH(
                RenterItemRoomConfirmedBinding.inflate(
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

    override fun layout(position: Int): Int = when (listData[position].trangThaiDatPhong) {
        0 -> {
            R.layout.renter_item_room_wait
        }

        1 -> {
            R.layout.renter_item_room_confirmed
        }

        2 -> {
            R.layout.renter_item_room_seen
        }

        else -> {
            R.layout.renter_item_room_canceled
        }
    }



}