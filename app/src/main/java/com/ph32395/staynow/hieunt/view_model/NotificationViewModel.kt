package com.ph32395.staynow.hieunt.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ph32395.staynow.hieunt.database.dao.NotificationDao
import com.ph32395.staynow.hieunt.model.NotificationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(private val dao: NotificationDao) : ViewModel() {

    private val _notificationsState = MutableStateFlow<List<NotificationModel>>(emptyList())
    val notificationsState: StateFlow<List<NotificationModel>> = _notificationsState

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

}
