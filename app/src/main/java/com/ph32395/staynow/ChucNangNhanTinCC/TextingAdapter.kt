package com.ph32395.staynow.ChucNangNhanTinCC

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ph32395.staynow.databinding.ItemTinNhanLeftBinding
import com.ph32395.staynow.databinding.ItemTinNhanRightBinding

class TextingAdapter(private val messages: List<Messenger>, private val currentUserId: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var TAG = "zzzTextingAdapterzzz"


    companion object {
        private const val VIEW_TYPE_SEND = 1
        private const val VIEW_TYPE_RECEIVE = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SEND) {
            val view =
                ItemTinNhanRightBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            Log.d(TAG, "onCreateViewHolder: viewType $viewType")
            SendViewHolder(view)
        } else {
            val view =
                ItemTinNhanLeftBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            Log.d(TAG, "onCreateViewHolder: viewType else $viewType")
            ReceiveViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        Log.d(TAG, "getItemViewType: $position")
        return if (messages[position].senderId == currentUserId) VIEW_TYPE_SEND else VIEW_TYPE_RECEIVE
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        Log.d(TAG, "onBindViewHolder:message $message")
        if (holder is SendViewHolder) {
            holder.bind(message)
        } else if (holder is ReceiveViewHolder) {
            holder.bind(message)
        }


    }


    class SendViewHolder(itemView: ItemTinNhanRightBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        private val textMessageSend = itemView.tvTinNhanRight
        fun bind(message: Messenger) {
            textMessageSend.text = message.message
        }
    }

    class ReceiveViewHolder(itemView: ItemTinNhanLeftBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        private val textMessageReceive = itemView.tvTinNhanLeft
        fun bind(message: Messenger) {
            textMessageReceive.text = message.message
        }
    }

}