package com.ph32395.staynow.ThongBao

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ph32395.staynow.R
import com.ph32395.staynow.hieunt.helper.Default
import com.ph32395.staynow.hieunt.helper.Default.NotificationTitle.TITLE_CANCELED_BY_RENTER
import com.ph32395.staynow.hieunt.helper.Default.NotificationTitle.TITLE_CONFIRMED
import com.ph32395.staynow.hieunt.helper.Default.NotificationTitle.TITLE_LEAVED_BY_RENTER

class NotificationAdapter(
    private val notifications: List<NotificationModel>,
    private val onClick: (NotificationModel) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)
        private val tvMessage = itemView.findViewById<TextView>(R.id.tvMessage)
        private val tvDateTime = itemView.findViewById<TextView>(R.id.tvDateTime)

        @SuppressLint("SetTextI18n")
        fun bind(notification: NotificationModel) {
            when(notification.title){
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
            tvTitle.text = notification.title
            tvMessage.text = notification.message
            tvDateTime.text = "${notification.date} at ${notification.time}"
            itemView.setOnClickListener { onClick(notification) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_thongbao, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount(): Int = notifications.size
}
