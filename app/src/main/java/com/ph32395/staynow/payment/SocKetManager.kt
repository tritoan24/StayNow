package com.ph32395.staynow.payment

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import io.socket.client.IO
import io.socket.client.Socket

class SocketManager {
    private var mSocket: Socket? = null

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

        } catch (e: Exception) {
            Log.e("SocketIO", "Error initializing socket: ${e.message}")
        }
    }

    fun disconnect() {
        mSocket?.disconnect()
    }
}