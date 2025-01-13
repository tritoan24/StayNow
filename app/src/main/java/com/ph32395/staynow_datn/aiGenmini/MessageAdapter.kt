package com.ph32395.staynow_datn.aiGenmini

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.ph32395.staynow_datn.R
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.util.Date
import java.util.Locale

class MessageAdapter : ListAdapter<MessageModel, MessageAdapter.MessageViewHolder>(DIFF_CALLBACK) {
    private var lastPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.bind(getItem(position))
        // Chỉ animation cho item mới
        if (position > lastPosition) {
            setAnimation(holder.itemView, position, getItem(position).role == "model")
            lastPosition = position
        }
    }

    private fun setAnimation(view: View, position: Int, isModel: Boolean) {
        val animation = AnimationUtils.loadAnimation(
            view.context,
            if (isModel) R.anim.slide_in_left else R.anim.slide_in_right
        )
        view.startAnimation(animation)
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val messageContainer: LinearLayout = itemView.findViewById(R.id.messageContainer)

        fun bind(messageModel: MessageModel) {
            messageText.text = messageModel.message
            timeText.text = getCurrentTime()
            val isModel = messageModel.role == "model"

            // Đặt gravity cho container
            val layoutParams = messageContainer.layoutParams as FrameLayout.LayoutParams
            layoutParams.gravity = if (isModel) Gravity.START else Gravity.END
            messageContainer.layoutParams = layoutParams

            // Đặt màu chữ và background
            messageText.setTextColor(
                if (isModel)
                    itemView.context.getColor(android.R.color.white)
                else
                    itemView.context.getColor(android.R.color.white)
            )

            timeText.setTextColor(
                if (isModel)
                    itemView.context.getColor(android.R.color.white)
                else
                    itemView.context.getColor(android.R.color.white)
            )

            messageContainer.setBackgroundResource(
                if (isModel) R.drawable.bg_message_ai else R.drawable.bg_message_user
            )
        }

        private fun getCurrentTime(): String {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date())
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MessageModel>() {
            override fun areItemsTheSame(oldItem: MessageModel, newItem: MessageModel): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: MessageModel, newItem: MessageModel): Boolean {
                return oldItem.message == newItem.message && oldItem.role == newItem.role
            }
        }
    }

    override fun onViewDetachedFromWindow(holder: MessageViewHolder) {
        holder.itemView.clearAnimation()
        super.onViewDetachedFromWindow(holder)
    }
}
