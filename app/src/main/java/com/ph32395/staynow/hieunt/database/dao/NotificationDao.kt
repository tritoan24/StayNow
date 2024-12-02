package com.ph32395.staynow.hieunt.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ph32395.staynow.hieunt.model.NotificationModel
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM NotificationModel")
    fun getAllNotificationFlow(): Flow<List<NotificationModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNotification(model: NotificationModel)

    @Update
    fun updateNotification(model: NotificationModel)

    @Query("SELECT COUNT(*) FROM NotificationModel WHERE timestamp = :timestamp")
    fun isNotificationExists(timestamp: Long): Int

    @Query("SELECT COUNT(*) FROM NotificationModel WHERE isRead = 0")
    fun countNotificationNotSeen(): Int
}