package com.ph32395.staynow_datn.aiGenmini

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ph32395.staynow_datn.R
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.ph32395.staynow_datn.Activity.RoomDetailActivity
import com.ph32395.staynow_datn.fragment.home.HomeViewModel
////
//class ChatActivity : AppCompatActivity() {
//
//    private lateinit var messageAdapter: MessageAdapter
//    private lateinit var messageList: RecyclerView
//    private lateinit var inputField: EditText
//    private lateinit var sendButton: Button
//    private lateinit var viewModelRoom: RecommendedRoomsViewModel
//    private lateinit var recommendedRoomsAdapter: RecommendedRoomsAdapter
//    private lateinit var recommendedRoomsLayout: LinearLayout
//
//    private val viewModel: ChatViewModel by lazy { ChatViewModel() }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_chat)
//
//        setupUI()
//        observeViewModel()
//    }
//
//    private fun setupUI() {
//        // Ánh xạ các view từ layout
//        messageList = findViewById(R.id.messageList)
//        inputField = findViewById(R.id.inputField)
//        sendButton = findViewById(R.id.sendButton)
//        recommendedRoomsLayout = findViewById(R.id.recommendedRoomsLayout)
//
//
//
//        // Cài đặt RecyclerView và Adapter
//        messageAdapter = MessageAdapter()
//        messageList.layoutManager = LinearLayoutManager(this).apply {
//            stackFromEnd = true
//        }
//        messageList.adapter = messageAdapter
//
//        // Gửi tin nhắn khi nhấn nút gửi
//        sendButton.setOnClickListener {
//            val message = inputField.text.toString()
//            if (message.isNotEmpty()) {
//                // Ẩn layout gợi ý khi gửi tin nhắn mới
//                recommendedRoomsLayout.visibility = View.GONE
//                viewModel.sendMessage(message)
//                inputField.text.clear()
//            }
//        }
//    }
//
//    private fun observeViewModel() {
//        viewModel.messageList.observe(this) { messages ->
//            messageAdapter.submitList(messages.toList())
//            messageList.scrollToPosition(messages.size - 1)
//
//            // Xử lý tin nhắn cuối cùng để tìm ID phòng
//            messages.lastOrNull()?.let { lastMessage ->
//                if (lastMessage.role == "model") {
//                    handleAIResponse(lastMessage.message)
//                }
//            }
//        }
//    }
//
//    private fun handleAIResponse(message: String) {
//        // Kiểm tra xem response có chứa danh sách phòng không
//        if (message.contains("RECOMMENDED_ROOMS:")) {
//            val roomIdsPattern = "RECOMMENDED_ROOMS:(.+?):END_RECOMMENDED_ROOMS".toRegex()
//            val matchResult = roomIdsPattern.find(message)
//
//            matchResult?.groupValues?.get(1)?.let { roomIdsString ->
//                val roomIds = roomIdsString.split(",")
//                showRecommendedRooms(roomIds)
//            }
//        } else {
//            // Ẩn layout gợi ý nếu không có phòng được đề xuất
//            recommendedRoomsLayout.visibility = View.GONE
//        }
//    }
//
//    private fun showRecommendedRooms(roomIds: List<String>) {
//        recommendedRoomsLayout.removeAllViews() // Xóa các view cũ
//
//        // Tạo title cho phần gợi ý
//        val titleView = TextView(this).apply {
//            text = "Phòng được gợi ý:"
//            setTextColor(Color.BLACK)
//            textSize = 16f
//            typeface = Typeface.DEFAULT_BOLD
//            setPadding(16, 16, 16, 8)
//        }
//        recommendedRoomsLayout.addView(titleView)
//
//        // Tạo các button cho từng phòng
//        roomIds.forEach { roomId ->
//            val roomButton = MaterialButton(this).apply {
//                text = "Xem phòng $roomId"
//                layoutParams = LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT
//                ).apply {
//                    setMargins(16, 4, 16, 4)
//                }
//                setOnClickListener {
//                    navigateToRoomDetail(roomId)
//                }
//            }
//            recommendedRoomsLayout.addView(roomButton)
//        }
//
//        // Hiển thị layout gợi ý
//        recommendedRoomsLayout.visibility = View.VISIBLE
//    }
//
//    private fun navigateToRoomDetail(roomId: String) {
//        // TODO: Thay thế bằng navigation thực tế đến màn hình chi tiết phòng
//        // Ví dụ:
//        val intent = Intent(this, RoomDetailActivity::class.java).apply {
//            putExtra("ROOM_ID", roomId)
//        }
//        startActivity(intent)
//    }
//}
//

class ChatActivity : AppCompatActivity() {
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: RecyclerView
    private lateinit var inputField: EditText
    private lateinit var sendButton: Button
    private lateinit var recommendedRoomsRecyclerView: RecyclerView
    private lateinit var recommendedRoomsContainer: LinearLayout
    private lateinit var recommendedRoomsAdapter: RecommendedRoomsAdapter
    private val chatViewModel: ChatViewModel by lazy { ChatViewModel() }
    private val recommendedViewModel: RecommendedRoomsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        setupUI()
        setupRecommendedRooms()
        observeViewModel()
    }

    private fun setupUI() {
        // Ánh xạ các view từ layout
        messageList = findViewById(R.id.messageList)
        inputField = findViewById(R.id.inputField)
        sendButton = findViewById(R.id.sendButton)
        recommendedRoomsRecyclerView = findViewById(R.id.recommendedRoomsRecyclerView)
        recommendedRoomsContainer = findViewById(R.id.recommendedRoomsContainer)

        // Cài đặt RecyclerView và Adapter cho tin nhắn
        messageAdapter = MessageAdapter()
        messageList.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
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
        recommendedRoomsAdapter = RecommendedRoomsAdapter { roomId, room ->
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
                    // Fetch và hiển thị thông tin chi tiết của các phòng
                    recommendedViewModel.fetchRoomDetails(roomIds)
                }
            }
        } else {
            // Ẩn container nếu không có phòng được đề xuất
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
            putExtra("ROOM_ID", roomId)
        }
        startActivity(intent)
    }
}