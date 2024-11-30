package com.ph32395.staynow.payment

import android.app.Activity
import android.content.Context
import android.content.Intent
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
        callback: (String?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentTime = System.currentTimeMillis()

                // Truy vấn Firestore
                val querySnapshot = db.collection("PaymentTransaction")
                    .whereEqualTo("contractId", contractId)
                    .get()
                    .await()

                val validOrder = querySnapshot.documents.firstOrNull { doc ->
                    val expireTime =
                        doc.getLong("app_time")!! + doc.getLong("expire_duration_seconds")!! * 1000
                    expireTime > currentTime
                }

                if (validOrder != null) {
                    val token = validOrder.getString("zp_trans_token")
                    CoroutineScope(Dispatchers.Main).launch {
                        callback(token)
                    }
                } else {
                    // Không tìm thấy hoặc hết hạn -> Tạo đơn mới
                    createOrder(amount, contractId, billId, items) { token ->
                        CoroutineScope(Dispatchers.Main).launch {
                            callback(token)
                        }
                    }
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    callback(null)
                }
            }
        }

    }

    private fun createOrder(
        amount: Double,
        contractId: String,
        billId: String,
        items: String,
        callback: (String?) -> Unit
    ) {
        val apiClient = ApiClient.create()
        val orderRequest = OrderRequest(amount, contractId, billId, items)

        apiClient.createOrder(orderRequest).enqueue(object : Callback<OrderResponse> {
            override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {
                if (response.isSuccessful) {
                    val token = response.body()?.zalopay_response?.zp_trans_token
                    callback(token)
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                callback(null)
            }
        })
    }

    fun startPayment(zpToken: String?, contract: HopDong?) {
        zpToken?.let {
            ZaloPaySDK.getInstance()
                .payOrder(context as Activity, it, "demozpdk://app", object : PayOrderListener {
                    override fun onPaymentSucceeded(s: String?, s1: String?, s2: String?) {
                        Toast.makeText(context, "Thanh toán thành công!", Toast.LENGTH_SHORT).show()

                        CoroutineScope(Dispatchers.IO).launch {
                            updateContractStatus(
                                contract!!.maHopDong,
                                contract.hoaDonHopDong.idHoaDon
                            )
                        }
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

    suspend fun updateContractStatus(contractId: String, billId: String) {
        val db = FirebaseFirestore.getInstance()

        try {
            val hopDongRef = db.collection("HopDong").document(contractId)
            val hoaDonHopDongRef = db.collection("hoaDonHopDong").document(billId)

            db.runBatch { batch ->
                batch.update(hopDongRef, "status", "ACTIVE")
                batch.update(hoaDonHopDongRef, "status", "PAID")
            }.await()

            println("Trạng thái đã được cập nhật.")
        } catch (e: Exception) {
            println("Lỗi khi cập nhật trạng thái: ${e.message}")
        }
    }
}
