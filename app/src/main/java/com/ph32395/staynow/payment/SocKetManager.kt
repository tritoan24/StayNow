package com.ph32395.staynow.payment

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import io.socket.client.IO
import io.socket.client.Socket

class SocketManager {
    private var mSocket: Socket? = null

    // Khởi tạo kết nối
    fun initSocket(serverUrl: String) {
        try {
            mSocket = IO.socket(serverUrl)

            // Kết nối
            mSocket?.connect()

            // Lắng nghe sự kiện kết nối
            mSocket?.on(Socket.EVENT_CONNECT) {
                Log.d("SocketIO", "Connected to server")
            }

            // Lắng nghe sự kiện ngắt kết nối
            mSocket?.on(Socket.EVENT_DISCONNECT) {
                Log.d("SocketIO", "Disconnected from server")
            }

            // Lắng nghe sự kiện cập nhật hợp đồng
            mSocket?.on("contractPaymentUpdate") { args ->
                val data = args[0] as? Map<*, *>
                data?.let { contractData ->
                    val contractId = contractData["contractId"] as? String
                    val status = contractData["status"] as? String

                    // Xử lý cập nhật trạng thái hợp đồng
                    handleContractUpdate(contractId, status)
                }
            }

        } catch (e: Exception) {
            Log.e("SocketIO", "Error initializing socket: ${e.message}")
        }
    }

    // Xử lý logic cập nhật hợp đồng
    private fun handleContractUpdate(contractId: String?, status: String?) {
        contractId?.let { id ->
            // Thực hiện truy vấn Firestore để kiểm tra và cập nhật
            val contractRef = FirebaseFirestore.getInstance()
                .collection("HopDong")
                .document(id)

            // Truy vấn các hoá đơn của hợp đồng
            FirebaseFirestore.getInstance()
                .collection("hoaDonHopDong")
                .whereEqualTo("contractId", id)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val allPaid = querySnapshot.documents.all {
                        it.getString("status") == status
                    }

                    if (allPaid) {
                        // Cập nhật trạng thái hợp đồng
                        contractRef.update(
                            mapOf(
                                "status" to "ACTIVE",
                            )
                        )
                    }
                }
        }
    }

    fun disconnect() {
        mSocket?.disconnect()
    }
}