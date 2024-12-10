package com.ph32395.staynow.hieunt.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import com.ph32395.staynow.hieunt.database.dao.NotificationDao
import com.ph32395.staynow.hieunt.model.NotificationModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NotificationViewModel(private val dao: NotificationDao) : ViewModel() {

    private val _notificationsState = MutableStateFlow<List<NotificationModel>>(emptyList())
    val notificationsState: StateFlow<List<NotificationModel>> = _notificationsState

    //tritoancode noti chung
    private val database = FirebaseDatabase.getInstance().reference
    private val _notificationStatus = MutableLiveData<Boolean>()
    val notificationStatus: LiveData<Boolean> get() = _notificationStatus

    init {
        viewModelScope.launch (Dispatchers.IO){
            dao.getAllNotificationFlow().collect {
                _notificationsState.value = it
            }
        }
    }

    fun updateNotification(notification: NotificationModel, onCompletion: () -> Unit){
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateNotification(notification)
        }.invokeOnCompletion {
            onCompletion.invoke()
        }
    }


    //tritoan code
    // Gửi thông báo và lưu vào Firebase Realtime Database
    fun sendNotification(notification: NotificationModel, recipientId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Tạo ID duy nhất cho mỗi thông báo dưới ID người nhận
                val notificationRef = database.child("ThongBao").child(recipientId).push()
                val notificationId = notificationRef.key ?: ""

                // Cập nhật timestamp cho thông báo
                notification.timestamp = System.currentTimeMillis()

                // Tạo dữ liệu thông báo theo cấu trúc bạn yêu cầu
                val notificationData = mapOf(
                    "date" to notification.date,
                    "message" to notification.message,
                    "time" to notification.time,
                    "timestamp" to notification.timestamp,
                    "title" to notification.title,
                    "typeNotification" to notification.typeNotification
                )

                // Lưu thông báo vào Firebase dưới ID người nhận
                notificationRef.setValue(notificationData).await()

                // Cập nhật trạng thái thành công
                _notificationStatus.postValue(true)
            } catch (e: Exception) {
                // Nếu có lỗi, thông báo trạng thái là thất bại
                _notificationStatus.postValue(false)
            }
        }
    }

}
