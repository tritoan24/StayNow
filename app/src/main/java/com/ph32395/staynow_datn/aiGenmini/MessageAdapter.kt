package com.ph32395.staynow_datn.aiGenmini

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.ph32395.staynow_datn.R
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
class MessageAdapter : ListAdapter<MessageModel, MessageAdapter.MessageViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val messageContainer: View = itemView.findViewById(R.id.messageContainer)

        fun bind(messageModel: MessageModel) {
            messageText.text = messageModel.message
            val isModel = messageModel.role == "model"

            // Đặt trọng lực cho View
            val gravity = if (isModel) Gravity.START else Gravity.END
            (messageText.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
                marginStart = if (isModel) 16 else 64
                marginEnd = if (isModel) 64 else 16
            }
            messageText.gravity = gravity

//            // Đặt màu nền
//            messageContainer.setBackgroundResource(
//                if (isModel) R.drawable. else R.drawable.addphoto
//            )
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
}
