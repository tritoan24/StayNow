package com.ph32395.staynow.payment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow.Activity.SuccessPaymentActivity
import com.ph32395.staynow.TaoHopDong.HopDong
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

class OrderProcessor(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()

    fun checkAndCreateOrder(
        amount: Double,
        contractId: String,
        billId: String,
        items: String,
        callback: (String?, String?, Long?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentTime = System.currentTimeMillis()

                // Truy vấn Firestore
                val querySnapshot = db.collection("PaymentTransaction")
                    .whereEqualTo("contractId", contractId)
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
                    createOrder(amount, contractId, billId, items) { token, orderUrl ->
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
        contractId: String,
        billId: String,
        items: String,
        callback: (String?, String?) -> Unit
    ) {
        val apiClient = ApiClient.create()
        val orderRequest = OrderRequest(amount, contractId, billId, items)

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
                        Toast.makeText(context, "Thanh toán thành công!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(context, SuccessPaymentActivity::class.java)
                        intent.putExtra("itemData", contract)
                        context.startActivity(intent)
                    }

                    override fun onPaymentCanceled(s: String?, s1: String?) {
                        Toast.makeText(context, "Thanh toán bị hủy", Toast.LENGTH_SHORT).show()
                    }

                    override fun onPaymentError(
                        zaloPayError: ZaloPayError?,
                        s: String?,
                        s1: String?
                    ) {
                        Toast.makeText(context, "Lỗi thanh toán", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

}
