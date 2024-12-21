package com.ph32395.staynow_datn.ChucNangNhanTinCC

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.ph32395.staynow_datn.databinding.ActivityTextingMessengeBinding
import com.ph32395.staynow_datn.hieunt.model.NotificationModel
import com.ph32395.staynow_datn.hieunt.view_model.NotificationViewModel
import com.ph32395.staynow_datn.hieunt.view_model.ViewModelFactory
import java.util.Calendar

class TextingMessengeActivity : AppCompatActivity() {

    private val TAG = "TextingMessenge"
    private lateinit var binding: ActivityTextingMessengeBinding
    private var databaseRef = FirebaseDatabase.getInstance().getReference("NguoiDung")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextingMessengeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Lấy thông tin từ Intent
        val userIdNguoiGui = FirebaseAuth.getInstance().currentUser?.uid // ID của user hiện tại
        val userIdNguoiNhan = intent.getStringExtra("userId") // ID của người nhận (nếu có)
        val userChat = intent.getStringExtra("userChat") // ID người chat (từ danh sách chat)

        // Xác định chatId duy nhất
        val chatId = getChatId(userIdNguoiGui, userIdNguoiNhan ?: userChat)

        Log.d(TAG, "onCreate: userIdNguoiGui $userIdNguoiGui")
        Log.d(TAG, "onCreate: userIdNguoiNhan $userIdNguoiNhan")
        Log.d(TAG, "onCreate: chatId $chatId")
        Log.d(TAG, "onCreate: $userChat")

        // Hiển thị thông tin người dùng
        fetchUser(userIdNguoiNhan ?: userChat, binding)

        // Lấy danh sách tin nhắn
        fetchChatMessages(chatId) { messages ->
            val adapter = TextingAdapter(messages, userIdNguoiGui!!)
            binding.rcvChat.layoutManager = LinearLayoutManager(this).apply {
                stackFromEnd = true // Tự động cuộn xuống cuối danh sách
            }
            binding.rcvChat.adapter = adapter
        }

        // Gửi tin nhắn
        binding.btnGuiTinNhan.setOnClickListener {
            val messageText = binding.edtSoanNhanTin.text.toString()
            if (messageText.isNotEmpty()) {
                sendMessage(chatId, userIdNguoiGui!!, userIdNguoiNhan ?: userChat!!, messageText)
                binding.edtSoanNhanTin.setText("")
            }
        }
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    // Hàm tạo chatId duy nhất
    private fun getChatId(userId1: String?, userId2: String?): String {
        return if (userId1!! < userId2!!) {
            "${userId1}_${userId2}"
        } else {
            "${userId2}_${userId1}"
        }
    }

    // Lấy thông tin người dùng
    private fun fetchUser(userId: String?, binding: ActivityTextingMessengeBinding) {
        userId?.let {
            databaseRef.child(it).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = snapshot.child("hoTen").value.toString()
                    val anhDaiDien = snapshot.child("anhDaiDien").value.toString()
                    val status = snapshot.child("trangThai").value.toString()
                    val statusDrawable = binding.vTrangThaiUser.background as GradientDrawable
                    statusDrawable.setColor(if (status == "online") Color.GREEN else Color.GRAY)
                    if (!this@TextingMessengeActivity.isDestroyed && !this@TextingMessengeActivity.isFinishing) {
                        binding.tvNameUser.text = userName
                        Glide.with(this@TextingMessengeActivity)
                            .load(anhDaiDien)
                            .circleCrop()
                            .into(binding.ivAvatarItemTinNhan)
                    } else {
                        Log.d(TAG, "fetchUser: Activity destroyed, skipping image load")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "fetchUser: Error ${error.message}")
                }
            })
        }
    }

    // Lấy danh sách tin nhắn
    private fun fetchChatMessages(chatId: String, onResult: (List<Messenger>) -> Unit) {
        val database = Firebase.database.reference
        database.child("Chats").child(chatId).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = mutableListOf<Messenger>()
                    for (messageSnapshot in snapshot.children) {
                        val message = messageSnapshot.getValue(Messenger::class.java)
                        message?.let { messages.add(it) }
                    }
                    onResult(messages)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "fetchChatMessages: Error ${error.message}")
                }
            })
    }

    // Gửi tin nhắn
    private fun sendMessage(
        chatId: String,
        senderId: String,
        receiverId: String,
        messageText: String
    ) {
        Log.e(TAG, "sendMessage: senderId $senderId")
        val factory = ViewModelFactory(applicationContext)
        val notificationViewModel = ViewModelProvider(this, factory).get(NotificationViewModel::class.java)


        val database = Firebase.database.reference
        val timestamp = System.currentTimeMillis()

        // Tạo tin nhắn
        val message = Messenger(senderId, messageText, timestamp)
        val messageId = database.child("Chats").child(chatId).child("messages").push().key!!

        // Lưu tin nhắn
        database.child("Chats").child(chatId).child("messages").child(messageId).setValue(message)


        // Lấy số tin nhắn chưa đọc hiện tại của người nhận
        database.child("ChatList").child(receiverId).child(chatId).get()
            .addOnSuccessListener { snapshot ->
                val currentUnreadCount =
                    snapshot.child("unreadCount").getValue(Int::class.java) ?: 0

                // Cập nhật ChatList cho receiver (tăng unreadCount)
                val receiverChat =
                    Chat(chatId, messageText, timestamp, currentUnreadCount + 1, senderId)
                database.child("ChatList").child(receiverId).child(chatId).setValue(receiverChat)

                // Cập nhật ChatList cho sender (không tăng unreadCount)
                val senderChat = Chat(chatId, messageText, timestamp, 0, receiverId)
                database.child("ChatList").child(senderId).child(chatId).setValue(senderChat)
            }

        //push notification
        val notificationMes = NotificationModel(
            tieuDe = "Bạn có 1 tin nhắn mới",
            tinNhan = messageText,
            ngayGuiThongBao = Calendar.getInstance().time.toString(),
            thoiGian = "0",
            mapLink = null,
            daDoc = false,
            daGui = true,
            loaiThongBao = "send_massage",
            idModel = senderId
        )
        Log.e(TAG, "sendMessage:notificationMes $notificationMes")
        notificationViewModel.sendNotification(notificationMes,receiverId)


    }

    private fun markMessagesAsRead(chatId: String, userId: String) {
        val database = Firebase.database.reference

        // Đặt unreadCount về 0 trong ChatList của người dùng
        database.child("ChatList").child(userId).child(chatId).child("unreadCount").setValue(0)
    }


    override fun onResume() {
        super.onResume()
        val userIdNguoiGui = FirebaseAuth.getInstance().currentUser?.uid // ID của user hiện tại
        val userIdNguoiNhan = intent.getStringExtra("userId") // ID của người nhận (nếu có)
        val userChat = intent.getStringExtra("userChat") // ID người chat (từ danh sách chat)

        if (userChat == null) {
            return
        } else {
            val chatId = getChatId(userIdNguoiGui, userIdNguoiNhan ?: userChat)
            markMessagesAsRead(chatId, userIdNguoiGui!!)

        }


    }


}
