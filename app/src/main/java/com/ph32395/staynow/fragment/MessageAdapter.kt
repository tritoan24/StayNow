package com.ph32395.staynow.fragment

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ph32395.staynow.ChucNangNhanTinCC.Chat
import com.ph32395.staynow.databinding.ItemTinNhanNhaBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(private val itemList: List<Chat>, private val onClickItem: (Chat) -> Unit) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private val TAG = "zzzMessageAdapterzzz"
    private lateinit var binding: ItemTinNhanNhaBinding
    private var dataUserRef = FirebaseDatabase.getInstance().getReference("NguoiDung")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        binding = ItemTinNhanNhaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val item = itemList[position]
        Log.d(TAG, "onBindViewHolder: item $item")
        dataUserRef.child(item.otherUserId!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange: snapshot $snapshot")
                val name = snapshot.child("ho_ten").value
                val avatar = snapshot.child("anh_daidien").value
                holder.nameUser.text = name.toString()
                val context = holder.itemView.context
                if (context is Activity && !context.isDestroyed) {
                    Glide.with(context)
                        .load(avatar)
                        .circleCrop()
                        .into(holder.image)
                } else {
                    Log.d(TAG, "onDataChange: nulll content")
                }

            }

            override fun onCancelled(error: DatabaseError) {

                Log.e(TAG, "onCancelled: error ${error.message}")

            }
        })
        if (item.unreadCount == 0) {
            holder.nameUser.setTypeface(Typeface.SERIF, Typeface.NORMAL)
            holder.texting.setTypeface(Typeface.SERIF, Typeface.NORMAL)
            holder.timeGuiTin.setTypeface(Typeface.SERIF, Typeface.NORMAL)
        } else {
            holder.nameUser.setTypeface(Typeface.SERIF, Typeface.BOLD)
            holder.texting.setTypeface(Typeface.SERIF, Typeface.BOLD)
            holder.timeGuiTin.setTypeface(Typeface.SERIF, Typeface.BOLD)
        }
        val time = convertTimestampToTime(item.lastMessageTime!!)
        Log.d(TAG, "onBindViewHolder: time $time")
        holder.texting.text = item.lastMessage
        holder.timeGuiTin.text = time
        holder.tvNumberTexting.text = if (item.unreadCount != 0) item.unreadCount.toString() else " "

        holder.itemView.setOnClickListener {
            onClickItem(item)
        }

    }

    class MessageViewHolder(itemView: ItemTinNhanNhaBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val nameUser = itemView.userName
        val image = itemView.ivAvatarItemTinNhan
        val texting = itemView.tvTexting
        val timeGuiTin = itemView.tvTimeGuiTexting
        val tvNumberTexting = itemView.tvNumberTexting

    }

    fun convertTimestampToTime(timestamp: Long): String {
        // Chuyển đổi timestamp thành đối tượng Date
        val date = Date(timestamp)

        // Đặt định dạng thời gian bạn muốn hiển thị
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        // Chuyển đổi Date thành chuỗi theo định dạng đã chỉ định
        return dateFormat.format(date)
    }

}