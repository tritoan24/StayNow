package com.ph32395.staynow_datn.aiGenmini

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ph32395.staynow_datn.Activity.RoomDetailActivity
import com.ph32395.staynow_datn.R

class ChatActivity : AppCompatActivity() {

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: RecyclerView
    private lateinit var inputField: EditText
    private lateinit var sendButton: View
    private lateinit var btnBack: ImageView
    private lateinit var recommendedRoomsRecyclerView: RecyclerView
    private lateinit var recommendedRoomsContainer: LinearLayout
    private lateinit var recommendedRoomsAdapter: RecommendedRoomsAdapter

    private val chatViewModel: ChatViewModel by lazy { ChatViewModel() }
    private val recommendedViewModel: RecommendedRoomsViewModel by lazy { RecommendedRoomsViewModel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_with_ai)

        setupUI()
        setupRecommendedRooms()
        observeViewModel()
    }

    private fun setupUI() {
        // Ánh xạ các view từ layout
        messageList = findViewById(R.id.messageList)
        inputField = findViewById(R.id.inputField)
        sendButton = findViewById(R.id.sendButton)
        btnBack = findViewById(R.id.btnBack)
        recommendedRoomsRecyclerView = findViewById(R.id.recommendedRoomsRecyclerView)
        recommendedRoomsContainer = findViewById(R.id.recommendedRoomsContainer)

        // Cài đặt RecyclerView và Adapter cho tin nhắn
        messageAdapter = MessageAdapter()
        messageList.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        btnBack.setOnClickListener{
            finish()
        }
        messageList.adapter = messageAdapter

        // Gửi tin nhắn khi nhấn nút gửi
        sendButton.setOnClickListener {
            val message = inputField.text.toString()
            if (message.isNotEmpty()) {
                recommendedRoomsContainer.visibility = View.GONE
                chatViewModel.sendMessage(message)
                inputField.text.clear()
            }
        }

    }

    private fun setupRecommendedRooms() {
        recommendedRoomsAdapter = RecommendedRoomsAdapter { roomId, _ ->
            navigateToRoomDetail(roomId)
        }

        recommendedRoomsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = recommendedRoomsAdapter
        }
    }

    private fun handleAIResponse(message: String) {
        if (message.contains("RECOMMENDED_ROOMS:")) {
            // Trích xuất danh sách ID phòng từ tin nhắn
            val roomIdsPattern = "RECOMMENDED_ROOMS:(.+?):END_RECOMMENDED_ROOMS".toRegex()
            val matchResult = roomIdsPattern.find(message)

            matchResult?.groupValues?.get(1)?.let { roomIdsString ->
                val roomIds = roomIdsString.split(",").map { it.trim() }
                if (roomIds.isNotEmpty()) {
                    recommendedRoomsContainer.visibility = View.VISIBLE
                    recommendedViewModel.fetchRoomDetails(roomIds)
                }
            }
        } else {
            recommendedRoomsContainer.visibility = View.GONE
        }
    }

    private fun observeViewModel() {
        // Observe tin nhắn chat
        chatViewModel.messageList.observe(this) { messages ->
            messageAdapter.submitList(messages.toList())
            messageList.scrollToPosition(messages.size - 1)

            messages.lastOrNull()?.let { lastMessage ->
                if (lastMessage.role == "model") {
                    handleAIResponse(lastMessage.message)
                }
            }
        }

        // Observe phòng được đề xuất
        recommendedViewModel.recommendedRooms.observe(this) { rooms ->
            recommendedRoomsAdapter.updateRoomList(rooms)
        }

        // Observe trạng thái loading
        recommendedViewModel.isLoading.observe(this) { isLoading ->
            // Hiển thị loading indicator nếu cần
        }

        // Observe lỗi
        recommendedViewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToRoomDetail(roomId: String) {
        val intent = Intent(this, RoomDetailActivity::class.java).apply {
            putExtra("maPhongTro", roomId)
            putExtra("ManHome", "ManND")

        }
        startActivity(intent)
    }
}