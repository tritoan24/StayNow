package com.ph32395.staynow.hieunt.view.feature.notification


import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ph32395.staynow.databinding.ActivityNotificationBinding
import com.ph32395.staynow.hieunt.base.BaseActivity
import com.ph32395.staynow.hieunt.helper.SystemUtils
import com.ph32395.staynow.hieunt.model.NotificationWithDateModel
import com.ph32395.staynow.hieunt.view.feature.notification.adapter.NotificationWithDateAdapter
import com.ph32395.staynow.hieunt.view_model.NotificationViewModel
import com.ph32395.staynow.hieunt.widget.tap
import com.ph32395.staynow.hieunt.widget.toast
import com.ph32395.staynow.hieunt.widget.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationActivity : BaseActivity<ActivityNotificationBinding, NotificationViewModel>() {
    private lateinit var notificationWithDateAdapter: NotificationWithDateAdapter

    override fun setViewBinding(): ActivityNotificationBinding {
        return ActivityNotificationBinding.inflate(layoutInflater)
    }

    override fun initViewModel(): Class<NotificationViewModel> = NotificationViewModel::class.java

    override fun initView() {
        notificationWithDateAdapter = NotificationWithDateAdapter { notification ->
            if (!notification.isRead){
                viewModel.updateNotification(notification.copy(isRead = true)){
                    lifecycleScope.launch {
                        toast("Đã xem!")
                    }
                }
            }
        }
        binding.rvNotifications.adapter = notificationWithDateAdapter
        binding.ivBack.tap {
            finish()
        }
    }

    override fun initClickListener() {

    }

    override fun dataObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.notificationsState.collect{ listNotification ->
                    showLoading()
                    if (listNotification.isNotEmpty()){
                        launch(Dispatchers.IO) {
                            val listNotificationWithDate = listNotification.groupBy { SystemUtils.currentDateFormattedFromMillis(it.timestamp) }.map { (date, histories) ->
                                NotificationWithDateModel().apply {
                                    this.date = date
                                    this.listNotification.addAll(histories.reversed())
                                }
                            }
                            withContext(Dispatchers.Main){
                                binding.tvNoData.visibility = if (listNotificationWithDate.isNotEmpty()) GONE else VISIBLE
                                notificationWithDateAdapter.addListObserver(listNotificationWithDate.reversed())
                                binding.rvNotifications.scrollToPosition(0)
                                dismissLoading()
                            }
                        }
                    } else {
                        dismissLoading()
                        binding.tvNoData.visible()
                    }
                }
            }
        }
    }
}