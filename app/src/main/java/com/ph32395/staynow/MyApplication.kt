package com.ph32395.staynow

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
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
        FirebaseApp.initializeApp(this)

        // Đặt trạng thái người dùng
        setOnlineStatusListener()
    }

    private fun setOnlineStatusListener() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userStatusRef = Firebase.database.reference.child("NguoiDung").child(userId).child("status")

        // Lắng nghe trạng thái kết nối với Firebase
        val connectedRef = Firebase.database.reference.child(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isConnected = snapshot.getValue(Boolean::class.java) ?: false
                if (isConnected) {
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
        val userStatusRef = Firebase.database.reference.child("NguoiDung").child(userId).child("status")
        if (isOnline) {
            userStatusRef.setValue("online")
                .addOnFailureListener { Log.e("StatusUpdate", "Failed to update status") }
        } else {
            userStatusRef.setValue("offline")
                .addOnFailureListener { Log.e("StatusUpdate", "Failed to update status") }
        }
    }
}
