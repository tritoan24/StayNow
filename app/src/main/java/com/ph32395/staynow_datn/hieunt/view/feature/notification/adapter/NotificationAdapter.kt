package com.ph32395.staynow_datn.hieunt.view.feature.notification.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.databinding.ItemNotificationBinding
import com.ph32395.staynow_datn.hieunt.base.BaseAdapter
import com.ph32395.staynow_datn.hieunt.base.BaseViewHolder
import com.ph32395.staynow_datn.hieunt.helper.Default.NotificationTitle.TITLE_CANCELED_BY_OVER_TIME
import com.ph32395.staynow_datn.hieunt.helper.Default.NotificationTitle.TITLE_CANCELED_BY_RENTER
import com.ph32395.staynow_datn.hieunt.helper.Default.NotificationTitle.TITLE_CANCELED_BY_TENANT
import com.ph32395.staynow_datn.hieunt.helper.Default.NotificationTitle.TITLE_CONFIRMED
import com.ph32395.staynow_datn.hieunt.helper.Default.NotificationTitle.TITLE_LEAVED_BY_RENTER
import com.ph32395.staynow_datn.hieunt.helper.Default.NotificationTitle.TITLE_LEAVED_BY_TENANT
import com.ph32395.staynow_datn.hieunt.model.NotificationModel
import com.ph32395.staynow_datn.hieunt.widget.layoutInflate

class NotificationAdapter(
    private val onClickNotification: (NotificationModel) -> Unit
) : BaseAdapter<NotificationModel, NotificationAdapter.NotificationVH>() {

    inner class NotificationVH(binding: ItemNotificationBinding): BaseViewHolder<NotificationModel,ItemNotificationBinding>(binding){
        @SuppressLint("SetTextI18n")
        override fun bindData(data: NotificationModel) {
            super.bindData(data)
            binding.apply {
                when(data.tieuDe){
                    TITLE_CONFIRMED -> {
                        tvTitle.setTextColor(Color.parseColor("#00FF00"))
                    }
                    TITLE_CANCELED_BY_RENTER, TITLE_CANCELED_BY_TENANT, TITLE_CANCELED_BY_OVER_TIME-> {
                        tvTitle.setTextColor(Color.parseColor("#FF0000"))
                    }
                    TITLE_LEAVED_BY_RENTER, TITLE_LEAVED_BY_TENANT -> {
                        tvTitle.setTextColor(Color.parseColor("#FFCC00"))
                    }
                }
                tvTitle.text = data.tieuDe
                tvMessage.text = data.tinNhan
                tvDateTime.text = "${data.ngayGuiThongBao} at ${data.thoiGian}"
                vSeen.visibility = if (data.daDoc) GONE else VISIBLE

            }
        }

        override fun onItemClickListener(data: NotificationModel) {
            super.onItemClickListener(data)
            onClickNotification.invoke(data)
        }
    }

    override fun viewHolder(viewType: Int, parent: ViewGroup): NotificationVH = NotificationVH(
        ItemNotificationBinding.inflate(parent.layoutInflate(), parent, false))

    override fun layout(position: Int): Int = R.layout.item_notification

}
