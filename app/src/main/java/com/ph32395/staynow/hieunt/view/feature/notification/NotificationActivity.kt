package com.ph32395.staynow.hieunt.view.feature.notification


import android.content.Intent
import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ph32395.staynow.databinding.ActivityNotificationBinding
import com.ph32395.staynow.hieunt.base.BaseActivity
import com.ph32395.staynow.hieunt.view_model.NotificationViewModel
import com.ph32395.staynow.hieunt.widget.gone
import com.ph32395.staynow.hieunt.widget.visible
import kotlinx.coroutines.launch

class NotificationActivity : BaseActivity<ActivityNotificationBinding, NotificationViewModel>() {
    private lateinit var notificationAdapter: NotificationAdapter

    private fun openMap(mapLink: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapLink))
        intent.setPackage("com.google.android.apps.maps")
        startActivity(intent)
    }

    override fun setViewBinding(): ActivityNotificationBinding {
        return ActivityNotificationBinding.inflate(layoutInflater)
    }

    override fun initViewModel(): Class<NotificationViewModel> = NotificationViewModel::class.java

    override fun initView() {
        notificationAdapter = NotificationAdapter { notification ->
            notification.mapLink?.let { openMap(it) }
        }

        binding.rvNotifications.adapter = notificationAdapter
        binding.rvNotifications.layoutManager = LinearLayoutManager(this)

    }

    override fun initClickListener() {

    }

    override fun dataObserver() {
        showLoading()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.notificationsState.collect{
                    if (it.isNotEmpty()) {
                        binding.tvNoData.gone()
                    } else {
                        binding.tvNoData.visible()
                    }
                    notificationAdapter.addListObserver(it.reversed())
                    binding.rvNotifications.scrollToPosition(0)
                    dismissLoading()
                }
            }
        }
    }
}