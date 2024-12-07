package com.ph32395.staynow.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.ph32395.staynow.ChucNangNhanTinCC.Chat
import com.ph32395.staynow.ChucNangNhanTinCC.TextingMessengeActivity
import com.ph32395.staynow.databinding.FragmentMessageBinding

class MessageFragment : Fragment() {

    private var TAG = "zzzMessageFragmentzzz"
    private lateinit var binding: FragmentMessageBinding
    private lateinit var adapterMessage: MessageAdapter
    private lateinit var adapterUserStatus: UserStatusOnOfAdapter
    private val listUser = mutableListOf<UserStatus>()
    private var currentPage = 0
    private val pageSize = 10
    private var isLoading = false
    private var hasMore = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessageBinding.inflate(inflater, container, false)

        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_message, container, false)
        binding.btnAdminSuport.setOnClickListener {
            val adminId = "BCvWcFi8M9PAeMnKLv2SefBzRe23" // ID của admin bạn muốn truyền
            val userId =
                FirebaseAuth.getInstance().currentUser?.uid  // Lấy ID của người dùng hiện tại
            Log.d(TAG, "onCreate: userId $userId")

            if (userId != null) {
                // Khi có người dùng đăng nhập, truyền ID người dùng là người gửi và admin là người nhận
                val intent = Intent(context, TextingMessengeActivity::class.java)
                intent.putExtra("userId", adminId)  // Truyền ID của admin là người nhận
                startActivity(intent)
            } else {
                // Nếu không có người dùng đăng nhập, có thể hiển thị thông báo lỗi
                Log.e("MessageFragment", "No user logged in")
            }
        }

        setupRecyclerView()

        return binding.root

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        Log.d(TAG, "onCreate:userId $userId")
        fetchChatList(userId!!) {
            Log.d(TAG, "onCreate:it List chat $it")
            adapterMessage = MessageAdapter(it) {
                Log.d(TAG, "onCreate: it.time $it")
                val intent = Intent(context, TextingMessengeActivity::class.java)
                intent.putExtra("chatId", it.chatId)
                intent.putExtra("userChat", it.otherUserId)
                startActivity(intent)
            }
            binding.rcvListTinNhan.layoutManager = LinearLayoutManager(context)
            binding.rcvListTinNhan.adapter = adapterMessage

        }
    }

    fun fetchChatList(userId: String, onResult: (List<Chat>) -> Unit) {
        val database = Firebase.database.reference
        database.child("ChatList").child(userId).orderByChild("lastMessageTime")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val chatList = mutableListOf<Chat>()
                    for (chatSnapshot in snapshot.children) {
                        val chat = chatSnapshot.getValue(Chat::class.java)
                        chat?.let { chatList.add(it) }
                    }
                    Log.d(TAG, "onDataChange:chatList $chatList")
                    val reversedChatList = chatList.reversed()
                    onResult(reversedChatList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Failed to fetch chat list: ${error.message}")
                }
            })
    }

    data class UserStatus(
        val ma_nguoidung: String = "",
        val ho_ten: String = "",
        val anh_daidien: String = "",
        val status: String = ""
    ) {
        constructor() : this("", "", "", "")
    }

    //On
    fun fetchListUser1(onResult: (List<UserStatus>) -> Unit) {
        val database = Firebase.database.reference
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        Log.d(TAG, "fetchListUser: database $database")

        database.child("NguoiDung")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var onlineCount = 0

                    // Lấy người dùng có trạng thái "online" trước, bỏ qua tài khoản của chính mình
                    for (snap in snapshot.children) {
                        val maNguoiDung =
                            snap.child("ma_nguoidung").getValue(String::class.java) ?: ""
                        val ho_ten = snap.child("ho_ten").getValue(String::class.java) ?: ""
                        val anhDaiDien =
                            snap.child("anh_daidien").getValue(String::class.java) ?: ""
                        val status = snap.child("status").getValue(String::class.java) ?: ""

                        // Bỏ qua tài khoản của chính mình
                        if (maNguoiDung == currentUserId) continue

                        // Chỉ thêm người dùng có status là "online"
                        if (status == "online") {
                            val user = UserStatus(maNguoiDung, ho_ten, anhDaiDien, status)
                            listUser.add(user)
                            onlineCount++
                        }
                    }

                    // Nếu chưa đủ 10 người, lấy thêm người dùng có trạng thái khác
                    if (onlineCount < 10) {
                        for (snap in snapshot.children) {
                            val maNguoiDung =
                                snap.child("ma_nguoidung").getValue(String::class.java) ?: ""
                            val anhDaiDien =
                                snap.child("anh_daidien").getValue(String::class.java) ?: ""
                            val status = snap.child("status").getValue(String::class.java) ?: ""
                            val ho_ten = snap.child("ho_ten").getValue(String::class.java) ?: ""

                            // Nếu đã đủ 10 người, thoát vòng lặp
                            if (listUser.size >= 10) break

                            // Bỏ qua tài khoản của chính mình
                            if (maNguoiDung == currentUserId) continue

                            // Chỉ thêm người dùng không phải "online"
                            if (status != "online") {
                                val user = UserStatus(maNguoiDung, ho_ten, anhDaiDien, status)
                                listUser.add(user)
                            }
                        }
                    }

                    // Xử lý danh sách người dùng đã lấy
                    Log.d(TAG, "List of users: $listUser")
                    // Ví dụ: cập nhật UI, gửi tới adapter, v.v.
                    onResult(listUser)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: ${error.message}")
                }
            })
    }

    fun observeUserChanges(
        currentPage: Int,
        pageSize: Int,
        onResult: (List<UserStatus>, Boolean) -> Unit
    ) {
        val database = Firebase.database.reference.child("NguoiDung")
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val onlineList = mutableListOf<UserStatus>()
                val offlineList = mutableListOf<UserStatus>()

                for (snap in snapshot.children) {
                    val maNguoiDung = snap.child("ma_nguoidung").getValue(String::class.java) ?: ""
                    val ho_ten = snap.child("ho_ten").getValue(String::class.java) ?: ""
                    val anhDaiDien = snap.child("anh_daidien").getValue(String::class.java) ?: ""
                    val status = snap.child("status").getValue(String::class.java) ?: ""

                    val user = UserStatus(maNguoiDung, ho_ten, anhDaiDien, status)
                    // Bỏ qua tài khoản của chính mình
                    if (maNguoiDung == currentUserId) continue
                    // Phân loại online và offline
                    if (status == "online") {
                        onlineList.add(user)
                    } else {
                        offlineList.add(user)
                    }
                }

                // Kết hợp danh sách và phân trang
                val combinedList = onlineList + offlineList
                val paginatedList = combinedList.drop(currentPage * pageSize).take(pageSize)
                val hasMore = (currentPage + 1) * pageSize < combinedList.size

                onResult(paginatedList, hasMore) // Trả về dữ liệu và trạng thái còn dữ liệu không
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error: ${error.message}")
            }
        })
    }

    // Khởi tạo RecyclerView
    private fun setupRecyclerView() {
        binding.rcvListUser.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val adapter = UserStatusOnOfAdapter(listUser) { itemUser ->
            // Xử lý khi nhấn vào item
            val intent = Intent(context, TextingMessengeActivity::class.java)
            intent.putExtra("userId", itemUser.ma_nguoidung)
            startActivity(intent)
        }
        binding.rcvListUser.adapter = adapter

        // Lắng nghe sự kiện cuộn
        binding.rcvListUser.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (!isLoading && hasMore && lastVisibleItem + 1 >= totalItemCount) {
                    // Tải thêm dữ liệu
                    loadMoreUsers(adapter)
                }
            }
        })

        // Tải trang đầu tiên
        loadMoreUsers(adapter)
    }

    // Hàm tải thêm dữ liệu
    @SuppressLint("NotifyDataSetChanged")
    private fun loadMoreUsers(adapter: UserStatusOnOfAdapter) {
        isLoading = true
        observeUserChanges(currentPage, pageSize) { newUsers, hasMoreData ->
            listUser.addAll(newUsers)
            adapter.notifyDataSetChanged()
            isLoading = false
            hasMore = hasMoreData
            currentPage++
        }
    }


}