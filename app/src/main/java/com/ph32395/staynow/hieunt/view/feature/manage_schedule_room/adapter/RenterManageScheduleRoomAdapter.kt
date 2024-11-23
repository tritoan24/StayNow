package com.ph32395.staynow.hieunt.view.feature.manage_schedule_room.adapter

import android.annotation.SuppressLint
import android.net.Uri
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.ph32395.staynow.R
import com.ph32395.staynow.databinding.RenterItemRoomCanceledBinding
import com.ph32395.staynow.databinding.RenterItemRoomConfirmedBinding
import com.ph32395.staynow.databinding.RenterItemRoomSeenBinding
import com.ph32395.staynow.databinding.RenterItemRoomWaitBinding
import com.ph32395.staynow.hieunt.base.BaseAdapter
import com.ph32395.staynow.hieunt.base.BaseViewHolder
import com.ph32395.staynow.hieunt.model.ScheduleRoomModel
import com.ph32395.staynow.hieunt.widget.layoutInflate
import com.ph32395.staynow.hieunt.widget.tap


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
                tvNameTenant.text = "Người thuê: ${data.tenantName}"
                tvNameRoom.text = "Tên phòng: ${data.roomName}"
                tvPhoneNumber.text = "SDT: ${data.tenantPhoneNumber}"
                tvTime.text = "Thời gian: ${data.time} ngày ${data.date}"
                tvConfirm.tap {
                    onClickConfirm.invoke(data)
                    val notificationData = hashMapOf(
                        "title" to "Lịch hẹn đã được xác nhận",
                        "message" to "Phòng: ${data.roomName}, Địa chỉ: ${data.roomAddress}",
                        "date" to data.date,
                        "time" to data.time,
                        "mapLink" to "geo:0,0?q=${Uri.encode(data.roomAddress)}",
                        "timestamp" to System.currentTimeMillis()
                    )
                    val database = FirebaseDatabase.getInstance()
                    val thongBaoRef = database.getReference("ThongBao")

                    val userId = data.tenantId
                    val userThongBaoRef = thongBaoRef.child(userId)

                    val newThongBaoId = userThongBaoRef.push().key
                    if (newThongBaoId != null) {
                        userThongBaoRef.child(newThongBaoId).setValue(notificationData)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Thông báo đã được lưu!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(context, "Lỗi: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
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
                tvNameTenant.text = "Người thuê: ${data.tenantName}"
                tvNameRoom.text = "Tên phòng: ${data.roomName}"
                tvPhoneNumber.text = "SDT: ${data.tenantPhoneNumber}"
                tvTime.text = "Thời gian: ${data.time} ngày ${data.date}"
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
                tvNameTenant.text = "Người thuê: ${data.tenantName}"
                tvNameRoom.text = "Tên phòng: ${data.roomName}"
                tvPhoneNumber.text = "SDT: ${data.tenantPhoneNumber}"
                tvTime.text = "Thời gian: ${data.time} ngày ${data.date}"
            }
        }
    }

    inner class RoomCanceledVH(binding: RenterItemRoomCanceledBinding) :
        BaseViewHolder<ScheduleRoomModel, RenterItemRoomCanceledBinding>(binding) {
        override fun bindData(data: ScheduleRoomModel) {
            super.bindData(data)
            binding.apply {
                tvNameTenant.text = "Người thuê: ${data.tenantName}"
                tvNameRoom.text = "Tên phòng: ${data.roomName}"
                tvPhoneNumber.text = "SDT: ${data.tenantPhoneNumber}"
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

    override fun layout(position: Int): Int = when (listData[position].status) {
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