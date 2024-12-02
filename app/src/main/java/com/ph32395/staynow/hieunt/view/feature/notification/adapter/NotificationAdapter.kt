package com.ph32395.staynow.hieunt.view.feature.notification.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.ph32395.staynow.R
import com.ph32395.staynow.databinding.ItemNotificationBinding
import com.ph32395.staynow.hieunt.base.BaseAdapter
import com.ph32395.staynow.hieunt.base.BaseViewHolder
import com.ph32395.staynow.hieunt.helper.Default.NotificationTitle.TITLE_CANCELED_BY_RENTER
import com.ph32395.staynow.hieunt.helper.Default.NotificationTitle.TITLE_CONFIRMED
import com.ph32395.staynow.hieunt.helper.Default.NotificationTitle.TITLE_LEAVED_BY_RENTER
import com.ph32395.staynow.hieunt.model.NotificationModel
import com.ph32395.staynow.hieunt.widget.layoutInflate
import com.ph32395.staynow.hieunt.widget.visible

class NotificationAdapter(
    private val onClickNotification: (NotificationModel) -> Unit
) : BaseAdapter<NotificationModel, NotificationAdapter.NotificationVH>() {

    inner class NotificationVH(binding: ItemNotificationBinding): BaseViewHolder<NotificationModel,ItemNotificationBinding>(binding){
        @SuppressLint("SetTextI18n")
        override fun bindData(data: NotificationModel) {
            super.bindData(data)
            binding.apply {
                when(data.title){
                    TITLE_CONFIRMED -> {
                        tvTitle.setTextColor(Color.parseColor("#00FF00"))
                    }
                    TITLE_CANCELED_BY_RENTER -> {
                        tvTitle.setTextColor(Color.parseColor("#FF0000"))
                    }
                    TITLE_LEAVED_BY_RENTER -> {
                        tvTitle.setTextColor(Color.parseColor("#FFCC00"))
                    }
                }
                tvTitle.text = data.title
                tvMessage.text = data.message
                tvDateTime.text = "${data.date} at ${data.time}"
                vSeen.visibility = if (data.isRead) GONE else VISIBLE
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
