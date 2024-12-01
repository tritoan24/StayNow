package com.ph32395.staynow.hieunt.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ph32395.staynow.R
import com.ph32395.staynow.ThongBao.NotificationModel
import com.ph32395.staynow.hieunt.helper.Default.Collection.THONG_BAO

class NotificationService : Service() {
    private val channelId = "NotificationChannel"
    private var oldNotifications = mutableListOf<NotificationModel>()
    private lateinit var notificationManager: NotificationManager

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(2004, getNotification(NotificationModel()))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                "Download M3U8 Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }

    private fun getNotification(notificationModel: NotificationModel): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logoapp)
            .setContentTitle(notificationModel.title)
            .setContentText(notificationModel.message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification(notificationModel: NotificationModel) {
        notificationManager.notify(
            notificationModel.timestamp.toInt(),
            getNotification(notificationModel)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        listenForNotifications()
        return START_STICKY
    }

    private fun listenForNotifications() {
        val database = FirebaseDatabase.getInstance().reference.child(THONG_BAO)
        FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
            database.child(userId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val newNotifications = mutableListOf<NotificationModel>()
                    for (data in snapshot.children) {
                        val notification = data.getValue(NotificationModel::class.java)
                        notification?.let { newNoti -> newNotifications.add(newNoti) }
                    }

                    val newEntries = newNotifications.filterNot { new ->
                        oldNotifications.any { old -> old.timestamp == new.timestamp }
                    }

                    if (newEntries.isNotEmpty()) {
                        newEntries.forEach { entries ->
                            updateNotification(entries)
                        }
                        oldNotifications = newNotifications
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("NotificationServiceZZZ", "Error: ${error.message}")
                }
            })
        }
    }
}
