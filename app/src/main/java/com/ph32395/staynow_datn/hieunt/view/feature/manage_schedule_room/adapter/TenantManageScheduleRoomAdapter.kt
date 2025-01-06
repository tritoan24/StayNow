package com.ph32395.staynow_datn.hieunt.view.feature.manage_schedule_room.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.databinding.TenantItemRoomCanceledBinding
import com.ph32395.staynow_datn.databinding.TenantItemRoomConfirmedBinding
import com.ph32395.staynow_datn.databinding.TenantItemRoomSeenBinding
import com.ph32395.staynow_datn.databinding.TenantItemRoomWaitBinding
import com.ph32395.staynow_datn.hieunt.base.BaseAdapter
import com.ph32395.staynow_datn.hieunt.base.BaseViewHolder
import com.ph32395.staynow_datn.hieunt.model.ScheduleRoomModel
import com.ph32395.staynow_datn.hieunt.widget.gone
import com.ph32395.staynow_datn.hieunt.widget.layoutInflate
import com.ph32395.staynow_datn.hieunt.widget.tap
import com.ph32395.staynow_datn.hieunt.widget.visible

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
                if (data.thayDoiBoiChuTro) {
                    tvNotification.visible()
                    llYesOrNo.visible()
                    llCancelAndLeave.gone()
                } else {
                    tvNotification.gone()
                    llYesOrNo.gone()
                    llCancelAndLeave.visible()
                }
                tvNameRenter.text = "Chủ trọ: ${data.tenChuTro}"
                tvNameRoom.text = "Tên phòng: ${data.tenPhong}"
                tvPhoneNumber.text = "SDT: ${data.sdtChuTro}"
                tvTime.text = "Thời gian: ${data.thoiGianDatPhong} ngày ${data.ngayDatPhong}"
                if (data.ghiChu.isNotEmpty()){
                    tvNote.text = data.ghiChu
                } else {
                    tvNote.gone()
                }
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
                tvNameRenter.text = "Chủ trọ: ${data.tenChuTro}"
                tvNameRoom.text = "Tên phòng: ${data.tenPhong}"
                tvPhoneNumber.text = "SDT: ${data.sdtChuTro}"
                tvTime.text = "Thời gian: ${data.thoiGianDatPhong} ngày ${data.ngayDatPhong}"
                if (data.ghiChu.isNotEmpty()){
                    tvNote.text = data.ghiChu
                } else {
                    tvNote.gone()
                }
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
                tvNameRenter.text = "Chủ trọ: ${data.tenChuTro}"
                tvNameRoom.text = "Tên phòng: ${data.tenPhong}"
                tvPhoneNumber.text = "SDT: ${data.sdtChuTro}"
                tvTime.text = "Thời gian: ${data.thoiGianDatPhong} ngày ${data.ngayDatPhong}"
            }
        }
    }

    inner class RoomCanceledVH(binding: TenantItemRoomCanceledBinding) :
        BaseViewHolder<ScheduleRoomModel, TenantItemRoomCanceledBinding>(binding) {
        override fun bindData(data: ScheduleRoomModel) {
            super.bindData(data)
            binding.apply {
                tvNameRenter.text = "Chủ trọ: ${data.tenChuTro}"
                tvNameRoom.text = "Tên phòng: ${data.tenPhong}"
                tvPhoneNumber.text = "SDT: ${data.sdtChuTro}"
                tvTime.text = "Thời gian: ${data.thoiGianDatPhong} ngày ${data.ngayDatPhong}"
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

    override fun layout(position: Int): Int = when (listData[position].trangThaiDatPhong) {
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