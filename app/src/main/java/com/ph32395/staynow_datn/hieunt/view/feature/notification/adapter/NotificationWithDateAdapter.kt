package com.ph32395.staynow_datn.hieunt.view.feature.notification.adapter

import android.view.ViewGroup
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.databinding.ItemNotificationWithDateBinding
import com.ph32395.staynow_datn.hieunt.base.BaseAdapter
import com.ph32395.staynow_datn.hieunt.base.BaseViewHolder
import com.ph32395.staynow_datn.hieunt.model.NotificationModel
import com.ph32395.staynow_datn.hieunt.model.NotificationWithDateModel
import com.ph32395.staynow_datn.hieunt.widget.layoutInflate

class NotificationWithDateAdapter(
    private val onClickItem : (NotificationModel) -> Unit
): BaseAdapter<NotificationWithDateModel, NotificationWithDateAdapter.NotificationWithDateVH>() {
    inner class NotificationWithDateVH (binding: ItemNotificationWithDateBinding): BaseViewHolder<NotificationWithDateModel, ItemNotificationWithDateBinding>(binding){
        override fun bindData(data: NotificationWithDateModel) {
            super.bindData(data)
            binding.tvDate.text = data.date
            binding.rvNotification.adapter = NotificationAdapter{
                onClickItem.invoke(it)
            }.apply { addListObserver(data.listNotification) }
        }
    }

    override fun viewHolder(viewType: Int, parent: ViewGroup): NotificationWithDateVH = NotificationWithDateVH(ItemNotificationWithDateBinding.inflate(parent.layoutInflate(),parent,false))

    override fun layout(position: Int): Int = R.layout.item_notification_with_date
}