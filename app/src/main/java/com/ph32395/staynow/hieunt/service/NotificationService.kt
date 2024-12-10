package com.ph32395.staynow.hieunt.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ph32395.staynow.MainActivity
import com.ph32395.staynow.R
import com.ph32395.staynow.TaoHoaDon.CreateInvoice
import com.ph32395.staynow.hieunt.database.db.AppDatabase
import com.ph32395.staynow.hieunt.helper.Default.Collection.IS_PUSHED
import com.ph32395.staynow.hieunt.helper.Default.Collection.THONG_BAO
import com.ph32395.staynow.hieunt.helper.Default.IntentKeys.OPEN_MANAGE_SCHEDULE_ROOM_BY_NOTIFICATION
import com.ph32395.staynow.hieunt.helper.Default.TypeNotification.TYPE_NOTI_BILL_MONTHLY
import com.ph32395.staynow.hieunt.helper.Default.TypeNotification.TYPE_SCHEDULE_ROOM_RENTER
import com.ph32395.staynow.hieunt.helper.Default.TypeNotification.TYPE_SCHEDULE_ROOM_TENANT
import com.ph32395.staynow.hieunt.model.NotificationModel
import com.ph32395.staynow.hieunt.view.feature.manage_schedule_room.TenantManageScheduleRoomActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class NotificationService : Service() {
    private val channelId = "NotificationChannel"
    private lateinit var notificationManager: NotificationManager
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val dao by lazy { AppDatabase.getInstance(this@NotificationService).notificationDao() }
    private val realtimeNotification by lazy {
        FirebaseDatabase.getInstance().reference.child(
            THONG_BAO
        )
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(
            2004,
            getNotification(
                NotificationModel(
                    title = "Notification Manager",
                    message = "Notification service is running..."
                )
            )
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                "Notification Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }

    private fun getNotification(notificationModel: NotificationModel): Notification {
        Log.d("idHopDong", "idHopDongput: ${notificationModel.idModel}")
        when (notificationModel.typeNotification){
            TYPE_SCHEDULE_ROOM_TENANT -> {
                Intent(this, TenantManageScheduleRoomActivity::class.java)
            }
            TYPE_SCHEDULE_ROOM_RENTER -> {
                Intent(this, MainActivity::class.java).apply { putExtra(OPEN_MANAGE_SCHEDULE_ROOM_BY_NOTIFICATION, true) }
            }
            TYPE_NOTI_BILL_MONTHLY -> {
                Intent(this, CreateInvoice::class.java).apply {
                    putExtra("CONTRACT_ID", notificationModel.idModel)
                }
            }
            else -> {
                null
            }
        }?.let {
            return NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.logoapp)
                .setContentTitle(notificationModel.title)
                .setContentText(notificationModel.message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setContentIntent(PendingIntent.getActivity(this, System.currentTimeMillis().toInt(), it, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
                .build()
        } ?: return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logoapp)
            .setContentTitle(notificationModel.title)
            .setContentText(notificationModel.message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOnlyAlertOnce(true)
            .build()

    }

    private fun pushNotification(notificationModel: NotificationModel) {
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
        FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
            realtimeNotification.child(userId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        data.getValue(NotificationModel::class.java)?.let { notification ->
                            serviceScope.launch {
                                if (dao.isNotificationExists(notification.timestamp) == 0 && !notification.isPushed) {
                                    dao.insertNotification(notification)
                                    pushNotification(notification)
                                    updateNotificationIsPushed(notification) { isCompletion ->
                                        if (isCompletion) {
                                            launch(Dispatchers.Main) {
                                                Toast.makeText(this@NotificationService, "Thông báo mới", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            launch(Dispatchers.Main) {
                                                Toast.makeText(this@NotificationService, "Lỗi khi hiển thị thông báo", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@NotificationService, "Lỗi khi lấy thông báo", Toast.LENGTH_SHORT).show()
                    Log.e("NotificationServiceZZZ", "Error: ${error.message}")
                }
            })
        }
    }

    fun updateNotificationIsPushed(
        notification: NotificationModel,
        onCompletion: (Boolean) -> Unit
    ) {
        FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
            val notificationsRef = realtimeNotification.child(userId)
            notificationsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (dataSnapshot in snapshot.children) {
                        val existingNotification =
                            dataSnapshot.getValue(NotificationModel::class.java)
                        if (existingNotification != null && existingNotification.timestamp == notification.timestamp) {
                            val notificationRef = dataSnapshot.ref
                            val updates = mapOf<String, Any>(
                                IS_PUSHED to true
                            )
                            notificationRef.updateChildren(updates).addOnSuccessListener {
                                    onCompletion.invoke(true)
                                    Log.d("NotificationServiceZZZ", "Notification isPushed updated successfully.")

                            }
                                .addOnFailureListener { exception ->
                                        onCompletion.invoke(false)
                                        Log.e("NotificationServiceZZZ", "Failed to update isPushed: ${exception.message}")
                                }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onCompletion.invoke(false)
                    Log.e("NotificationServiceZZZ", "Error fetching notifications: ${error.message}")
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
