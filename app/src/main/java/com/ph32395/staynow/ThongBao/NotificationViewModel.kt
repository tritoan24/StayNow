package com.ph32395.staynow.ThongBao

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificationViewModel : ViewModel() {

    private val _notifications = MutableLiveData<List<NotificationModel>>()
    val notifications: LiveData<List<NotificationModel>> get() = _notifications

    private val database = FirebaseDatabase.getInstance().getReference("ThongBao")
    private val _unreadCount = MutableLiveData<Int>()
    val unreadCount: LiveData<Int> get() = _unreadCount

    fun fetchNotifications(userId: String) {
        database.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notificationsList = mutableListOf<NotificationModel>()
                var unread = 0
                for (data in snapshot.children) {
                    val notification = data.getValue(NotificationModel::class.java)
                    if (notification != null) {
                        notificationsList.add(notification)
                        if (!notification.isRead) unread++
                    }
                }
                _notifications.value = notificationsList
                _unreadCount.value = unread // Cập nhật số lượng thông báo chưa đọc
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("NotificationViewModel", "Error fetching notifications: ${error.message}")
            }
        })
    }
}
