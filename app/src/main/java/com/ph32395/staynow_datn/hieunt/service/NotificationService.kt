package com.ph32395.staynow_datn.hieunt.service

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
import com.ph32395.staynow_datn.ChucNangNhanTinCC.TextingMessengeActivity
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.fragment.contract_tenant.BillContractActivity
import com.ph32395.staynow_datn.hieunt.database.db.AppDatabase
import com.ph32395.staynow_datn.hieunt.helper.Default.Collection.IS_PUSHED
import com.ph32395.staynow_datn.hieunt.helper.Default.Collection.THONG_BAO
import com.ph32395.staynow_datn.hieunt.helper.Default.TypeNotification.TYPE_CONTRACT_DONE
import com.ph32395.staynow_datn.hieunt.helper.Default.TypeNotification.TYPE_NOTI_BILL_MONTHLY_END_LANDLORD
import com.ph32395.staynow_datn.hieunt.helper.Default.TypeNotification.TYPE_NOTI_BILL_MONTHLY_END_TENANT
import com.ph32395.staynow_datn.hieunt.helper.Default.TypeNotification.TYPE_NOTI_BILL_MONTHLY_REMIND_LANDLORD
import com.ph32395.staynow_datn.hieunt.helper.Default.TypeNotification.TYPE_NOTI_BILL_MONTHLY_REMIND_TENANT
import com.ph32395.staynow_datn.hieunt.helper.Default.TypeNotification.TYPE_NOTI_CONTRACT
import com.ph32395.staynow_datn.hieunt.helper.Default.TypeNotification.TYPE_NOTI_MASSAGES
import com.ph32395.staynow_datn.hieunt.helper.Default.TypeNotification.TYPE_NOTI_PAYMENT_CONTRACT
import com.ph32395.staynow_datn.hieunt.helper.Default.TypeNotification.TYPE_NOTI_PAYMENT_INVOICE
import com.ph32395.staynow_datn.hieunt.helper.Default.TypeNotification.TYPE_NOTI_REMIND_STATUS_CONTRACT
import com.ph32395.staynow_datn.hieunt.helper.Default.TypeNotification.TYPE_NOTI_TERMINATED_CONFIRM_LANDLORD
import com.ph32395.staynow_datn.hieunt.helper.Default.TypeNotification.TYPE_NOTI_TERMINATED_CONFIRM_TENANT
import com.ph32395.staynow_datn.hieunt.helper.Default.TypeNotification.TYPE_NOTI_TERMINATED_DENY
import com.ph32395.staynow_datn.hieunt.helper.Default.TypeNotification.TYPE_NOTI_TERMINATED_REQUEST
import com.ph32395.staynow_datn.hieunt.helper.Default.TypeNotification.TYPE_SCHEDULE_ROOM_RENTER
import com.ph32395.staynow_datn.hieunt.helper.Default.TypeNotification.TYPE_SCHEDULE_ROOM_TENANT
import com.ph32395.staynow_datn.hieunt.model.NotificationModel
import com.ph32395.staynow_datn.hieunt.view.feature.notification.NotificationActivity
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
                    tieuDe = "Notification Manager",
                    tinNhan = "Notification service is running..."
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
        when (notificationModel.loaiThongBao) {
            TYPE_SCHEDULE_ROOM_TENANT -> {
                Intent(this, NotificationActivity::class.java)
            }

            TYPE_SCHEDULE_ROOM_RENTER -> {
                Intent(this, NotificationActivity::class.java)
            }

            TYPE_NOTI_BILL_MONTHLY_REMIND_LANDLORD, TYPE_NOTI_BILL_MONTHLY_REMIND_TENANT -> {
                Intent(this, NotificationActivity::class.java).apply {
                    putExtra("CONTRACT_ID", notificationModel.idModel)
                }
            }

            TYPE_NOTI_MASSAGES -> {
                Intent(this, TextingMessengeActivity::class.java).apply {
                    putExtra("userId", notificationModel.idModel)
                }
            }

            TYPE_CONTRACT_DONE -> {
                Intent(this, BillContractActivity::class.java).apply {
                    putExtra("contractId", notificationModel.idModel)
                }
            }

            TYPE_NOTI_CONTRACT -> {
                Intent(this, NotificationActivity::class.java)
            }

            TYPE_NOTI_BILL_MONTHLY_END_TENANT, TYPE_NOTI_BILL_MONTHLY_END_LANDLORD  -> {
                Intent(this, NotificationActivity::class.java)
            }

            TYPE_NOTI_PAYMENT_CONTRACT -> {
                Intent(this, NotificationActivity::class.java).apply {
                    putExtra("contractId", notificationModel.idModel)
                }
            }

            TYPE_NOTI_TERMINATED_REQUEST -> {
                Intent(this, NotificationActivity::class.java).apply {
                    putExtra("contractId", notificationModel.idModel)
                }
            }

            TYPE_NOTI_TERMINATED_CONFIRM_LANDLORD, TYPE_NOTI_TERMINATED_CONFIRM_TENANT -> {
                Intent(this, NotificationActivity::class.java).apply {
                    putExtra("contractId", notificationModel.idModel)
                }
            }

            TYPE_NOTI_TERMINATED_DENY -> {
                Intent(this, NotificationActivity::class.java).apply {
                    putExtra("contractId", notificationModel.idModel)
                }
            }


            TYPE_NOTI_PAYMENT_INVOICE -> {
                Intent(this, NotificationActivity::class.java).apply {
                    putExtra("invoiceId", notificationModel.idModel)
                }
            }

            TYPE_NOTI_REMIND_STATUS_CONTRACT -> {
                Intent(this, NotificationActivity::class.java).apply {
                    putExtra("contractId", notificationModel.idModel)
                }
            }

            else -> {
                null
            }
        }?.let {
            return NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.logoapp)
                .setContentTitle(notificationModel.tieuDe)
                .setContentText(notificationModel.tinNhan)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setContentIntent(
                    PendingIntent.getActivity(
                        this,
                        System.currentTimeMillis().toInt(),
                        it,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
                .build()
        } ?: return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logoapp)
            .setContentTitle(notificationModel.tieuDe)
            .setContentText(notificationModel.tinNhan)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOnlyAlertOnce(true)
            .build()

    }

    private fun pushNotification(notificationModel: NotificationModel) {
        notificationManager.notify(
            notificationModel.thoiGianGuiThongBao.toInt(),
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
                                if (dao.isNotificationExists(notification.thoiGianGuiThongBao) == 0 && !notification.daGui) {
                                    dao.insertNotification(notification)
                                    pushNotification(notification)
                                    updateNotificationIsPushed(notification) { isCompletion ->
                                        if (isCompletion) {
                                            launch(Dispatchers.Main) {
                                                Toast.makeText(
                                                    this@NotificationService,
                                                    "Thông báo mới",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        } else {
                                            launch(Dispatchers.Main) {
                                                Toast.makeText(
                                                    this@NotificationService,
                                                    "Lỗi khi hiển thị thông báo",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@NotificationService,
                        "Lỗi khi lấy thông báo",
                        Toast.LENGTH_SHORT
                    ).show()
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
                        if (existingNotification != null && existingNotification.thoiGianGuiThongBao == notification.thoiGianGuiThongBao) {
                            val notificationRef = dataSnapshot.ref
                            val updates = mapOf<String, Any>(
                                IS_PUSHED to true
                            )
                            notificationRef.updateChildren(updates).addOnSuccessListener {
                                onCompletion.invoke(true)
                                Log.d(
                                    "NotificationServiceZZZ",
                                    "Notification isPushed updated successfully."
                                )

                            }
                                .addOnFailureListener { exception ->
                                    onCompletion.invoke(false)
                                    Log.e(
                                        "NotificationServiceZZZ",
                                        "Failed to update isPushed: ${exception.message}"
                                    )
                                }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onCompletion.invoke(false)
                    Log.e(
                        "NotificationServiceZZZ",
                        "Error fetching notifications: ${error.message}"
                    )
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
