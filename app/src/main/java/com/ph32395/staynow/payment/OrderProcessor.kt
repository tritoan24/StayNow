package com.ph32395.staynow.payment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow.Activity.SuccessPaymentActivity
import com.ph32395.staynow.TaoHopDong.HopDong
import com.ph32395.staynow.hieunt.helper.Default
import com.ph32395.staynow.hieunt.model.NotificationModel
import com.ph32395.staynow.hieunt.view_model.NotificationViewModel
import com.ph32395.staynow.hieunt.view_model.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import vn.zalopay.sdk.ZaloPayError
import vn.zalopay.sdk.ZaloPaySDK
import vn.zalopay.sdk.listeners.PayOrderListener
import java.util.Calendar

class OrderProcessor(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()

    fun checkAndCreateOrder(
        amount: Double,
        contractId: String,
        billId: String,
        items: String,
        typeBill: TypeBill,
        callback: (String?, String?, Long?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentTime = System.currentTimeMillis()

                // Lắng nghe thay đổi từ Firestore
                db.collection("PaymentTransaction")
                    .whereEqualTo("contractId", contractId)
                    .whereEqualTo("status", "PENDING")
                    .addSnapshotListener { querySnapshot, error ->
                        if (error != null) {
                            Log.e("FirestoreListener", "Listen failed.", error)
                            CoroutineScope(Dispatchers.Main).launch {
                                callback(null, null, null)
                            }
                            return@addSnapshotListener
                        }

                        if (querySnapshot == null || querySnapshot.isEmpty) {
                            // Không có dữ liệu phù hợp -> Tạo đơn mới
                            createOrder(
                                amount,
                                contractId,
                                billId,
                                items,
                                typeBill
                            ) { token, orderUrl ->
                                CoroutineScope(Dispatchers.Main).launch {
                                    callback(token, orderUrl, 900)
                                }
                            }
                            return@addSnapshotListener
                        }

                        val validOrder = querySnapshot.documents.firstOrNull { doc ->
                            val appTime = doc.getLong("app_time")
                            val expireDurationSeconds = doc.getLong("expire_duration_seconds")
                            if (appTime != null && expireDurationSeconds != null) {
                                val expireTime = appTime + expireDurationSeconds * 1000
                                expireTime > currentTime
                            } else {
                                false // Bỏ qua tài liệu nếu thiếu dữ liệu
                            }
                        }

                        if (validOrder != null) {
                            val token = validOrder.getString("zp_trans_token")
                            val orderUrl = validOrder.getString("order_url")

                            // Tính toán thời gian còn lại (remainTime)
                            val expireTime =
                                validOrder.getLong("app_time")!! + validOrder.getLong("expire_duration_seconds")!! * 1000
                            val remainTime = expireTime - currentTime

                            Log.d("remainTimeOrderProcessor", remainTime.toString())
                            CoroutineScope(Dispatchers.Main).launch {
                                callback(token, orderUrl, remainTime)
                            }
                        } else {
                            // Không tìm thấy hoặc hết hạn -> Tạo đơn mới
                            createOrder(
                                amount,
                                contractId,
                                billId,
                                items,
                                typeBill
                            ) { token, orderUrl ->
                                CoroutineScope(Dispatchers.Main).launch {
                                    callback(token, orderUrl, 900)
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    callback(null, null, null)
                }
            }
        }
    }

    private fun createOrder(
        amount: Double,
        contractId: String,
        billId: String,
        items: String,
        typeBill: TypeBill,
        callback: (String?, String?) -> Unit
    ) {
        val apiClient = ApiClient.create()
        val orderRequest = OrderRequest(amount, contractId, billId, items, typeBill)

        apiClient.createOrder(orderRequest).enqueue(object : Callback<OrderResponse> {
            override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {
                if (response.isSuccessful) {
                    val token = response.body()?.zalopay_response?.zp_trans_token
                    val orderUrl = response.body()?.zalopay_response?.order_url
                    callback(token, orderUrl)
                } else {
                    callback(null, null)
                }
            }

            override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                callback(null, null)
            }
        })
    }

    fun startPayment(zpToken: String?, contract: HopDong) {
        zpToken?.let {
            ZaloPaySDK.getInstance()
                .payOrder(context as Activity, it, "demozpdk://app", object : PayOrderListener {
                    override fun onPaymentSucceeded(s: String?, s1: String?, s2: String?) {
                        handlePayment(context, contract, "success")
                    }

                    override fun onPaymentCanceled(s: String?, s1: String?) {
                        Toast.makeText(context, "Thanh toán bị hủy", Toast.LENGTH_SHORT).show()
                    }

                    override fun onPaymentError(
                        zaloPayError: ZaloPayError?,
                        s: String?,
                        s1: String?
                    ) {
                        handlePayment(context, contract, "error")
                    }
                })
        }
    }
}

private fun handlePayment(context: Context, contract: HopDong, status: String) {

    val messageSuccess =
        "Thanh toán thành công cho hợp đồng ${contract.maHopDong}\nMã hóa đơn ${contract.hoaDonHopDong.idHoaDon}"
    val messageError =
        "Thanh toán không thành công cho hợp đồng ${contract.maHopDong}\nMã hóa đơn ${contract.hoaDonHopDong.idHoaDon}"

    val notification = NotificationModel(
        title = "Thanh toán hóa đơn hợp đồng",
        message = if (status == "success") messageSuccess else messageError,
        date = Calendar.getInstance().time.toString(),
        time = "0",
        mapLink = null,
        isRead = false,
        isPushed = true,
        idModel = contract.maHopDong,
        typeNotification = Default.TypeNotification.TYPE_NOTI_PAYMENT_CONTRACT
    )

    val factory = ViewModelFactory(context)
    val notificationViewModel = ViewModelProvider(
        context as AppCompatActivity,
        factory
    )[NotificationViewModel::class.java]

    // Gửi thông báo đến cả hai người
    val recipientIds = listOf(contract.nguoiThue.maNguoiDung, contract.chuNha.maNguoiDung)
    recipientIds.forEach { recipientId ->
        notificationViewModel.sendNotification(notification, recipientId)
    }
    // Giám sát trạng thái gửi thông báo
    notificationViewModel.notificationStatus.observe(context, Observer { isSuccess ->
        if (isSuccess) {
            // Thông báo thành công
            Toast.makeText(context, "Thông báo đã được gửi!", Toast.LENGTH_SHORT).show()
        } else {
            // Thông báo thất bại
            Toast.makeText(context, "Gửi thông báo thất bại!", Toast.LENGTH_SHORT).show()
        }
    })

    if (status == "success") {
        val intent = Intent(context, SuccessPaymentActivity::class.java)
        intent.putExtra("itemData", contract)
        context.startActivity(intent)
    }
}


enum class TypeBill {
    HoaDonHopDong,
    HoaDonHangThang
}
