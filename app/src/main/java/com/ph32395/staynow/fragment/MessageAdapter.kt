package com.ph32395.staynow.fragment

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
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
import com.ph32395.staynow.R
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

        // Kiểm tra xem `item.otherUserId` có null hay không trước khi truy cập
        val otherUserId = item.otherUserId ?: ""
        if (otherUserId.isNotEmpty()) {
            dataUserRef.child(otherUserId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "onDataChange: snapshot $snapshot")
                    // Lấy dữ liệu từ Firebase, nếu thiếu thì dùng giá trị mặc định
                    val name = snapshot.child("ho_ten").value?.toString() ?: "Tên người dùng không có"
                    val avatar = snapshot.child("anh_daidien").value?.toString() ?: ""
                    val status = snapshot.child("status").value?.toString()
                    Log.d(TAG, "onDataChange:status $status")

                    val statusDrawable = holder.statusUser.background as GradientDrawable
                    statusDrawable.setColor(if (status == "online") Color.GREEN else Color.GRAY)

                    holder.nameUser.text = name
                    val context = holder.itemView.context

                    if (context is Activity && !context.isDestroyed) {
                        if (avatar.isNotEmpty()) {
                            Glide.with(context)
                                .load(avatar)
                                .circleCrop()
                                .into(holder.image)
                        } else {
                            // Nếu không có avatar, sử dụng hình ảnh mặc định
                            holder.image.setImageResource(R.drawable.ic_user)
                        }
                    } else {
                        Log.d(TAG, "onDataChange: null content or activity destroyed")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: error ${error.message}")
                }
            })
        } else {
            Log.e(TAG, "onBindViewHolder: otherUserId is null or empty")
        }

        // Kiểm tra `unreadCount` để thay đổi kiểu chữ nếu có tin nhắn chưa đọc
        if (item.unreadCount == 0) {
            holder.nameUser.setTypeface(Typeface.SERIF, Typeface.NORMAL)
            holder.texting.setTypeface(Typeface.SERIF, Typeface.NORMAL)
            holder.timeGuiTin.setTypeface(Typeface.SERIF, Typeface.NORMAL)
        } else {
            holder.nameUser.setTypeface(Typeface.SERIF, Typeface.BOLD)
            holder.texting.setTypeface(Typeface.SERIF, Typeface.BOLD)
            holder.timeGuiTin.setTypeface(Typeface.SERIF, Typeface.BOLD)
        }

        // Chuyển đổi thời gian và hiển thị tin nhắn
        val time = convertTimestampToTime(item.lastMessageTime ?: 0L)
        Log.d(TAG, "onBindViewHolder: time $time")
        holder.texting.text = item.lastMessage
        holder.timeGuiTin.text = time
        holder.tvNumberTexting.text = if (item.unreadCount != 0) item.unreadCount.toString() else " "

        // Xử lý sự kiện click item
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
        val statusUser = itemView.vTrangThaiUser
    }

    // Hàm chuyển đổi timestamp thành giờ phút giây
    fun convertTimestampToTime(timestamp: Long): String {
        // Chuyển đổi timestamp thành đối tượng Date
        val date = Date(timestamp)

        // Đặt định dạng thời gian bạn muốn hiển thị
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        // Chuyển đổi Date thành chuỗi theo định dạng đã chỉ định
        return dateFormat.format(date)
    }
}
