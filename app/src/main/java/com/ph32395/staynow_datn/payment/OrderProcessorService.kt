package com.ph32395.staynow_datn.payment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow_datn.Activity.SuccessPaymentActivity
import com.ph32395.staynow_datn.TaoHoaDon.InvoiceMonthlyModel
import com.ph32395.staynow_datn.hieunt.helper.Default
import com.ph32395.staynow_datn.hieunt.model.NotificationModel
import com.ph32395.staynow_datn.hieunt.view_model.NotificationViewModel
import com.ph32395.staynow_datn.hieunt.view_model.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import vn.zalopay.sdk.ZaloPayError
import vn.zalopay.sdk.ZaloPaySDK
import vn.zalopay.sdk.listeners.PayOrderListener
import java.util.Calendar

class OrderProcessorService(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()

    fun checkAndCreateOrder(
        amount: Double,
        billId: String,
        items: String,
        typeBill: TypeBill,
        callback: (String?, String?, Long?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentTime = System.currentTimeMillis()

                // Truy vấn Firestore
                val querySnapshot = db.collection("PaymentTransactionService")
                    .whereEqualTo("billId", billId)
                    .whereEqualTo("status", "PENDING")
                    .get()
                    .await()

                val validOrder = querySnapshot.documents.firstOrNull { doc ->
                    val expireTime =
                        doc.getLong("app_time")!! + doc.getLong("expire_duration_seconds")!! * 1000
                    expireTime > currentTime
                }

                if (validOrder != null) {
                    val token = validOrder.getString("zp_trans_token")
                    val orderUrl = validOrder.getString("order_url")

                    // Tính toán thời gian còn lại (remainTime)
                    val expireTime =
                        validOrder.getLong("app_time")!! + validOrder.getLong("expire_duration_seconds")!! * 1000
                    val remainTime =
                        expireTime - currentTime // Thời gian còn lại
                    Log.d("remainTimeOrderProcessor", remainTime.toString())
                    CoroutineScope(Dispatchers.Main).launch {
                        callback(token, orderUrl, remainTime)
                    }
                } else {
                    // Không tìm thấy hoặc hết hạn -> Tạo đơn mới
                    createOrder(amount, billId, items, typeBill) { token, orderUrl ->
                        CoroutineScope(Dispatchers.Main).launch {
                            callback(token, orderUrl, 900)
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
        billId: String,
        items: String,
        typeBill: TypeBill,
        callback: (String?, String?) -> Unit
    ) {
        val apiClient = ApiClient.create()
        val orderRequest = OrderRequestService(amount, billId, items, typeBill)

        apiClient.createOrderService(orderRequest).enqueue(object : Callback<OrderResponse> {
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

    fun startPayment(zpToken: String?, billId: InvoiceMonthlyModel) {
        zpToken?.let {
            ZaloPaySDK.getInstance()
                .payOrder(context as Activity, it, "demozpdk://app", object : PayOrderListener {
                    override fun onPaymentSucceeded(s: String?, s1: String?, s2: String?) {
                        handlePayment(context, billId, "success")
                    }

                    override fun onPaymentCanceled(s: String?, s1: String?) {
                        Toast.makeText(context, "Thanh toán bị hủy", Toast.LENGTH_SHORT).show()
                    }

                    override fun onPaymentError(
                        zaloPayError: ZaloPayError?,
                        s: String?,
                        s1: String?
                    ) {
                        handlePayment(context, billId, "error")
                    }
                })
        }
    }

}


private fun handlePayment(
    context: Context,
    bill: InvoiceMonthlyModel,
    status: String
) {
    val messageSuccess =
        "Thanh toán thành công cho hóa đơn $bill"
    val messageError =
        "Thanh toán không thành công cho hóa đơn $bill"

    val notification = NotificationModel(
        title = "Thanh toán hóa đơn hàng tháng",
        message = if (status == "success") messageSuccess else messageError,
        date = Calendar.getInstance().time.toString(),
        time = "0",
        mapLink = null,
        isRead = false,
        isPushed = true,
        idModel = bill.idHoaDon,
        typeNotification = Default.TypeNotification.TYPE_NOTI_PAYMENT_INVOICE
    )

    val factory = ViewModelFactory(context)
    val notificationViewModel = ViewModelProvider(
        context as AppCompatActivity,
        factory
    )[NotificationViewModel::class.java]

    val recipientId = bill.idNguoiNhan

    notificationViewModel.sendNotification(notification, recipientId)

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
        intent.putExtra("invoiceId", bill)
        context.startActivity(intent)
    }

}