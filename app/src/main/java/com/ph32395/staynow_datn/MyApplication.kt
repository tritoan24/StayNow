package com.ph32395.staynow_datn

import android.app.Application
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Khởi tạo Firebase
//        FirebaseApp.initializeApp(this)

        // Đặt trạng thái người dùng
        setOnlineStatusListener()
    }

    private fun setOnlineStatusListener() {
        Log.e("StatusUpdate", "da goi den ham set onnnn")
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userStatusRef = Firebase.database.reference.child("NguoiDung").child(userId).child("trangThai")

        // Lắng nghe trạng thái kết nối với Firebase
        val connectedRef = Firebase.database.reference.child(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.e("StatusUpdate", "dang vao ondatachange")
                val isConnected = snapshot.getValue(Boolean::class.java) ?: false
                if (isConnected) {
                    Log.e("StatusUpdate", "chua tao tai khoanr da tao id usser ")
                    // Khi kết nối, cập nhật trạng thái online
                    userStatusRef.setValue("online")
                        .addOnFailureListener { Log.e("StatusUpdate", "Failed to update status") }

                    // Xóa trạng thái khi ứng dụng bị tắt
                    userStatusRef.onDisconnect().setValue("offline")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("StatusUpdate", "Failed to listen to connection status: ${error.message}")
            }
        })
    }

    fun setOnlineStatus(isOnline: Boolean) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userStatusRef = Firebase.database.reference.child("NguoiDung").child(userId).child("trangThai")
        if (isOnline) {
            userStatusRef.setValue("online")
                .addOnFailureListener { Log.e("StatusUpdate", "Failed to update status") }
        } else {
            userStatusRef.setValue("offline")
                .addOnFailureListener { Log.e("StatusUpdate", "Failed to update status") }
        }
    }
}
