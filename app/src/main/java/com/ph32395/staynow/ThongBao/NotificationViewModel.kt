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

    fun fetchNotifications(userId: String) {
        // Lắng nghe thay đổi trong cơ sở dữ liệu Firebase
        database.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notificationsList = mutableListOf<NotificationModel>()
                for (data in snapshot.children) {
                    val notification = data.getValue(NotificationModel::class.java)
                    notification?.let { notificationsList.add(it) }
                }
                // Cập nhật LiveData với danh sách thông báo mới
                _notifications.value = notificationsList
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi nếu có
                Log.e("NotificationViewModel", "Error fetching notifications: ${error.message}")
            }
        })
    }
}
