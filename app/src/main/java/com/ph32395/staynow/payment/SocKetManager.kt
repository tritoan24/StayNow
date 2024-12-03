package com.ph32395.staynow.payment

import android.util.Log
import com.ph32395.staynow.utils.Constants
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException

class SocketManager {

    private lateinit var socket: Socket

    // Kết nối socket tới server
    fun connect() {
        try {
            socket = IO.socket(Constants.URL_SERVER_QUYET) // Thay URL và Port
            socket.connect()
            Log.d("com.ph32395.staynow.payment.SocketManager", "Socket connected: ${socket.id()}")
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            Log.e("com.ph32395.staynow.payment.SocketManager", "Socket connection error: ${e.message}")
        }
    }

    // Ngắt kết nối socket
    fun disconnect() {
        if (::socket.isInitialized && socket.connected()) {
            socket.disconnect()
            Log.d("com.ph32395.staynow.payment.SocketManager", "Socket disconnected")
        }
    }

    // Lắng nghe sự kiện từ server
    fun on(event: String, callback: (data: JSONObject) -> Unit) {
        if (::socket.isInitialized) {
            socket.on(event) { args ->
                if (args.isNotEmpty() && args[0] is JSONObject) {
                    callback(args[0] as JSONObject)
                }
            }
        }
    }

}
