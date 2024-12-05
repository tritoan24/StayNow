package com.ph32395.staynow.hieunt.view_model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ph32395.staynow.hieunt.database.db.AppDatabase

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommonVM::class.java)){
            return CommonVM() as T
        } else if(modelClass.isAssignableFrom(ManageScheduleRoomVM::class.java)){
            return ManageScheduleRoomVM() as T
        } else if (modelClass.isAssignableFrom(NotificationViewModel::class.java)){
            val dao = AppDatabase.getInstance(context).notificationDao()
            return NotificationViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
        return super.create(modelClass)
    }
}