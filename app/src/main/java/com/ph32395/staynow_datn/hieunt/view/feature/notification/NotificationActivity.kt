package com.ph32395.staynow_datn.hieunt.view.feature.notification


import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ph32395.staynow_datn.ChucNangNhanTinCC.TextingMessengeActivity
import com.ph32395.staynow_datn.MainActivity
import com.ph32395.staynow_datn.TaoHoaDon.CreateInvoice
import com.ph32395.staynow_datn.TaoHoaDon.CreateInvoiceEnd
import com.ph32395.staynow_datn.TaoHopDong.ChiTietHopDong
import com.ph32395.staynow_datn.databinding.ActivityNotificationBinding
import com.ph32395.staynow_datn.fragment.contract_tenant.ContractActivity
import com.ph32395.staynow_datn.hieunt.base.BaseActivity
import com.ph32395.staynow_datn.hieunt.helper.Default.IntentKeys.OPEN_MANAGE_SCHEDULE_ROOM_BY_NOTIFICATION
import com.ph32395.staynow_datn.hieunt.helper.Default.NotificationTitle.TITLE_CANCELED_BY_RENTER
import com.ph32395.staynow_datn.hieunt.helper.Default.NotificationTitle.TITLE_CANCELED_BY_TENANT
import com.ph32395.staynow_datn.hieunt.helper.Default.NotificationTitle.TITLE_CONFIRMED
import com.ph32395.staynow_datn.hieunt.helper.Default.NotificationTitle.TITLE_LEAVED_BY_RENTER
import com.ph32395.staynow_datn.hieunt.helper.Default.NotificationTitle.TITLE_LEAVED_BY_TENANT
import com.ph32395.staynow_datn.hieunt.helper.Default.NotificationTitle.TITLE_SCHEDULE_ROOM_SUCCESSFULLY
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
import com.ph32395.staynow_datn.hieunt.helper.SystemUtils
import com.ph32395.staynow_datn.hieunt.model.NotificationWithDateModel
import com.ph32395.staynow_datn.hieunt.view.feature.manage_schedule_room.TenantManageScheduleRoomActivity
import com.ph32395.staynow_datn.hieunt.view.feature.notification.adapter.NotificationWithDateAdapter
import com.ph32395.staynow_datn.hieunt.view_model.NotificationViewModel
import com.ph32395.staynow_datn.hieunt.widget.launchActivity
import com.ph32395.staynow_datn.hieunt.widget.tap
import com.ph32395.staynow_datn.hieunt.widget.toast
import com.ph32395.staynow_datn.hieunt.widget.visible
import com.ph32395.staynow_datn.quanlyhoadon.BillManagementActivity
import com.ph32395.staynow_datn.quanlyhoadon.DetailBillActivity
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
            if (!notification.daDoc) {
                viewModel.updateNotification(notification.copy(daDoc = true)) {
                    lifecycleScope.launch {
                        toast("Đã xem!")
                    }
                }
            }
            when (notification.loaiThongBao) {
                TYPE_SCHEDULE_ROOM_TENANT -> {
                    when (notification.tieuDe) {
                        TITLE_CONFIRMED -> {
                            notification.mapLink?.let { openMap(it) }
                        }

                        TITLE_CANCELED_BY_RENTER, TITLE_LEAVED_BY_RENTER -> {
                            launchActivity(TenantManageScheduleRoomActivity::class.java)
                        }
                    }
                }

                TYPE_SCHEDULE_ROOM_RENTER -> {
                    when (notification.tieuDe) {
                        TITLE_CONFIRMED -> {
                            notification.mapLink?.let { openMap(it) }
                        }

                        TITLE_CANCELED_BY_TENANT, TITLE_LEAVED_BY_TENANT, TITLE_SCHEDULE_ROOM_SUCCESSFULLY -> {
                            startActivity(Intent(this, MainActivity::class.java).apply {
                                putExtra(
                                    OPEN_MANAGE_SCHEDULE_ROOM_BY_NOTIFICATION,
                                    true
                                )
                            })
                        }
                    }
                }

                TYPE_NOTI_MASSAGES -> {
                    // Navigate to textingMassages
                    val intent = Intent(this, TextingMessengeActivity::class.java).apply {
                        putExtra("userId", notification.idModel)
                    }
                    startActivity(intent)
                }

                // thanh toán hóa đơn thành công
                TYPE_NOTI_PAYMENT_INVOICE -> {
                    val intent = Intent(this, DetailBillActivity::class.java).apply {
                        putExtra("invoiceId", notification.idModel)
                    }
                    startActivity(intent)
                }

                //Hợp đồng mới
                TYPE_NOTI_CONTRACT -> {
                    val intent = Intent(this, ChiTietHopDong::class.java).apply {
                        putExtra("CONTRACT_ID", notification.idModel)
                    }
                    startActivity(intent)
                }

                // yêu cầu chấm dứt
                TYPE_NOTI_TERMINATED_REQUEST -> {
                    val intent = Intent(this, ContractActivity::class.java).apply {
                        putExtra("contractId", notification.idModel)
                    }
                    startActivity(intent)
                }

                // nhắc nhở hóa đơn hàng tháng cho chủ nhà-> Tạo hóa đơn sau đó noti cho người thuê
                TYPE_NOTI_BILL_MONTHLY_REMIND_LANDLORD -> {
                    val intent = Intent(this, CreateInvoice::class.java).apply {
                        putExtra("CONTRACT_ID", notification.idModel)
                        putExtra("hoaDonHangThang", "true")
                    }
                    startActivity(intent)
                }

                //nhắc nhở có hóa đơn hàng tháng hoặc tháng cuối cùng cho người thuê, hoặc khi chấm dứt hợp đồng
                TYPE_NOTI_BILL_MONTHLY_REMIND_TENANT, TYPE_NOTI_BILL_MONTHLY_END_TENANT, TYPE_NOTI_TERMINATED_CONFIRM_TENANT -> {
                    val intent = Intent(this, BillManagementActivity::class.java)
                    startActivity(intent)
                }

                // nhắc nhở hóa đơn cuối cùng + chủ trọ tạo hóa đơn khi xác nhận chấm dứt hợp đồng. Nhảy vào màn tạo hóa đơn cuối cùng
                TYPE_NOTI_BILL_MONTHLY_END_LANDLORD, TYPE_NOTI_TERMINATED_CONFIRM_LANDLORD -> {
                    val intent = Intent(this, CreateInvoiceEnd::class.java).apply {
                        putExtra("CONTRACT_ID", notification.idModel)
                    }
                    startActivity(intent)
                }

                //từ chối chấm dứt hđ
                TYPE_NOTI_TERMINATED_DENY -> {
                    val intent = Intent(this, ContractActivity::class.java).apply {
                        putExtra("contractId", notification.idModel)
                        putExtra("notify", "notify")
                    }
                    startActivity(intent)
                }

                // thanh toán hợp đồng thành công
                TYPE_NOTI_PAYMENT_CONTRACT -> {
                    // Navigate to invoice creation
                    val intent = Intent(this, ChiTietHopDong::class.java).apply {
                        putExtra("CONTRACT_ID", notification.idModel)
                    }
                    startActivity(intent)
                }

                // nhắc nhở thời hạn hợp đồng
                TYPE_NOTI_REMIND_STATUS_CONTRACT -> {
                    val intent = Intent(this, ChiTietHopDong::class.java).apply {
                        putExtra("CONTRACT_ID", notification.idModel)
                    }
                    startActivity(intent)
                }

                else -> {
                }
            }
            Log.d("hiweuyhr", "Notification clicked: ${notification.idModel}")
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
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.notificationsState.collect { listNotification ->
                    showLoading()
                    if (listNotification.isNotEmpty()) {
                        launch(Dispatchers.IO) {
                            val listNotificationWithDate = listNotification.groupBy {
                                SystemUtils.currentDateFormattedFromMillis(it.thoiGianGuiThongBao)
                            }.map { (date, histories) ->
                                NotificationWithDateModel().apply {
                                    this.date = date
                                    this.listNotification.addAll(histories.reversed())
                                }
                            }
                            withContext(Dispatchers.Main) {
                                binding.tvNoData.visibility =
                                    if (listNotificationWithDate.isNotEmpty()) GONE else VISIBLE
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

    private fun openMap(diaChiPhong: String) {
        // Tạo URI từ địa chỉ đã mã hóa
        val geoUri = "geo:0,0?q=${Uri.encode(diaChiPhong)}"

        // Tạo một Intent để mở Google Maps
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
        intent.setPackage("com.google.android.apps.maps") // Chỉ định ứng dụng Google Maps

        // Kiểm tra xem thiết bị có ứng dụng Google Maps không
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent) // Mở ứng dụng Google Maps
        } else {
            // Nếu không có ứng dụng Google Maps, bạn có thể chuyển hướng đến trình duyệt web
            val webUri =
                Uri.parse("https://www.google.com/maps/search/?q=${Uri.encode(diaChiPhong)}")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
            startActivity(webIntent)
        }
    }
}