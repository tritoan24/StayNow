package com.ph32395.staynow.ChatWithAdmin


import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ph32395.staynow.R
class ChatWithAdminActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private var messageList = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actyvity_chat_with_admin)

        // Khởi tạo Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Lấy các view
        messageInput = findViewById(R.id.editTextMessage)
        sendButton = findViewById(R.id.btnSendMessage)
        recyclerView = findViewById(R.id.recyclerViewMessages)

        // Cấu hình RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        messageAdapter = MessageAdapter(messageList)
        recyclerView.adapter = messageAdapter

        // Lắng nghe sự kiện gửi tin nhắn
        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
            }
        }

        // Lắng nghe các tin nhắn mới
        listenForNewMessages()
    }

    private fun sendMessage(messageText: String) {
        val userId = auth.currentUser?.uid ?: return
        val messageId = database.child("messages").push().key ?: return

        val message = Message(userId, messageText)
        database.child("messages").child(messageId).setValue(message)
        messageInput.text.clear()
    }

    private fun listenForNewMessages() {
        database.child("messages").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                if (message != null) {
                    messageList.add(message)
                    messageAdapter.notifyItemInserted(messageList.size - 1)
                    recyclerView.scrollToPosition(messageList.size - 1)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
