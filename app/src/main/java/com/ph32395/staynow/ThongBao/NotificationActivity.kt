package com.ph32395.staynow.ThongBao


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ph32395.staynow.databinding.ActivityNotificationBinding

class NotificationActivity : AppCompatActivity() {

    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var viewModel: NotificationViewModel
    private val notifications = mutableListOf<NotificationModel>()
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private lateinit var binding: ActivityNotificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sử dụng ViewBinding để thiết lập giao diện
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo ViewModel
        viewModel = ViewModelProvider(this).get(NotificationViewModel::class.java)

        // Khởi tạo RecyclerView
        val rvNotifications = binding.rvNotifications
        rvNotifications.layoutManager = LinearLayoutManager(this)

        notificationAdapter = NotificationAdapter(notifications) { notification ->
            notification.mapLink?.let { openMap(it) }
        }
        rvNotifications.adapter = notificationAdapter

        // Khởi tạo FirebaseAuth và DatabaseReference
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

        // Lấy UID của người dùng đã đăng nhập
        val userId = mAuth.currentUser?.uid

        if (userId != null) {
            viewModel.fetchNotifications(userId)

            // Lắng nghe LiveData và cập nhật RecyclerView
            viewModel.notifications.observe(this, Observer { notificationsList ->
                notifications.clear()
                notifications.addAll(notificationsList)
                notificationAdapter.notifyDataSetChanged()
            })
        }
    }

    private fun openMap(mapLink: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapLink))
        intent.setPackage("com.google.android.apps.maps")
        startActivity(intent)
    }
}