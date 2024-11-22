package com.ph32395.staynow.ThongBao

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ph32395.staynow.R

class NotificationAdapter(
    private val notifications: MutableList<NotificationModel>, // Sử dụng MutableList để có thể thay đổi dữ liệu
    private val onClick: (NotificationModel) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)
        private val tvMessage = itemView.findViewById<TextView>(R.id.tvMessage)
        private val tvDateTime = itemView.findViewById<TextView>(R.id.tvDateTime)

        fun bind(notification: NotificationModel) {
            tvTitle.text = notification.title
            tvMessage.text = notification.message
            tvDateTime.text = "${notification.date} at ${notification.time}"

            // Kiểm tra nếu thông báo đã đọc
            if (notification.isRead) {
                // Hiển thị mờ chữ nếu đã đọc
                tvTitle.setTextColor(itemView.context.getColor(R.color.gray))
                tvMessage.setTextColor(itemView.context.getColor(R.color.gray))
                tvDateTime.setTextColor(itemView.context.getColor(R.color.gray))
                itemView.alpha = 0.5f // Làm mờ toàn bộ item
            } else {
                // Hiển thị bình thường nếu chưa đọc
                tvTitle.setTextColor(itemView.context.getColor(R.color.black))
                tvMessage.setTextColor(itemView.context.getColor(R.color.black))
                tvDateTime.setTextColor(itemView.context.getColor(R.color.black))
                itemView.alpha = 1f // Khôi phục độ sáng bình thường
            }

            itemView.setOnClickListener {
                onClick(notification)
                updateNotificationInDatabase(notification)
            }
        }




        private fun updateNotificationInDatabase(notification: NotificationModel) {
            val database = FirebaseDatabase.getInstance().getReference("ThongBao")
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            // Cập nhật thông báo trong Firebase
            if (userId != null && notification.id.isNotEmpty()) {
                val notificationRef = database.child(userId).child(notification.id)

                // Tạo một Map để cập nhật nhiều trường cùng lúc
                val updateMap = mapOf(
                    "isRead" to true, // Đánh dấu là đã đọc
                    "readTime" to System.currentTimeMillis().toString() // Thời gian đọc
                )

                // Sử dụng updateChildren để cập nhật nhiều trường
                notificationRef.updateChildren(updateMap).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("NotificationAdapter", "Notification marked as read successfully.")
                    } else {
                        Log.e("NotificationAdapter", "Failed to update notification: ${task.exception}")
                    }
                }
            }else{
                Log.e("NotificationAdapter", "User ID or notification ID is empty.")
            }
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
