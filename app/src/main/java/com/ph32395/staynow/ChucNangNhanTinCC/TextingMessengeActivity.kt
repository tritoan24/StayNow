package com.ph32395.staynow.ChucNangNhanTinCC

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.ph32395.staynow.databinding.ActivityTextingMessengeBinding

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
                    val userName = snapshot.child("ho_ten").value.toString()
                    val anhDaiDien = snapshot.child("anh_daidien").value.toString()

                    binding.tvNameUser.text = userName
                    Glide.with(this@TextingMessengeActivity)
                        .load(anhDaiDien)
                        .circleCrop()
                        .into(binding.ivAvatar)
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
    private fun sendMessage(chatId: String, senderId: String, receiverId: String, messageText: String) {
        val database = Firebase.database.reference
        val timestamp = System.currentTimeMillis()

        // Tạo tin nhắn
        val message = Messenger(senderId, messageText, timestamp)
        val messageId = database.child("Chats").child(chatId).child("messages").push().key!!

        // Lưu tin nhắn
        database.child("Chats").child(chatId).child("messages").child(messageId).setValue(message)

        // Cập nhật ChatList cho sender
        val senderChat = Chat(chatId, messageText, timestamp, 0, receiverId)
        database.child("ChatList").child(senderId).child(chatId).setValue(senderChat)

        // Cập nhật ChatList cho receiver
        val receiverChat = Chat(chatId, messageText, timestamp, 1, senderId)
        database.child("ChatList").child(receiverId).child(chatId).setValue(receiverChat)
    }
}
