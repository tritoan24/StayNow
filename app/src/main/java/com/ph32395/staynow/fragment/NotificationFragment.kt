package com.ph32395.staynow.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ph32395.staynow.ThongBao.NotificationAdapter
import com.ph32395.staynow.ThongBao.NotificationModel
import com.ph32395.staynow.ThongBao.NotificationViewModel
import com.ph32395.staynow.databinding.FragmentNotificationBinding

class NotificationFragment : Fragment() {

    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var viewModel: NotificationViewModel
    private val notifications = mutableListOf<NotificationModel>()
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout cho fragment
        val binding = FragmentNotificationBinding.inflate(inflater, container, false)

        // Khởi tạo ViewModel
        viewModel = ViewModelProvider(this)[NotificationViewModel::class.java]



        // Khởi tạo RecyclerView
        val rvNotifications = binding.rvNotifications
        rvNotifications.layoutManager = LinearLayoutManager(context)

        notificationAdapter = NotificationAdapter(notifications) { notification ->
            notification.mapLink?.let { openMap(it) }
        }
        rvNotifications.adapter = notificationAdapter

        // Khởi tạo FirebaseAuth và DatabaseReference
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference



        // Lấy UID của người dùng đã đăng nhập
        val userId = mAuth.currentUser?.uid


        viewModel.fetchNotifications(userId!!)

        // Lắng nghe LiveData và cập nhật RecyclerView
        viewModel.notifications.observe(viewLifecycleOwner, Observer { notificationsList ->
            notifications.clear()
            notifications.addAll(notificationsList)
            notificationAdapter.notifyDataSetChanged()
        })

        return binding.root
    }

    private fun openMap(mapLink: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapLink))
        intent.setPackage("com.google.android.apps.maps")
        startActivity(intent)
    }
}
