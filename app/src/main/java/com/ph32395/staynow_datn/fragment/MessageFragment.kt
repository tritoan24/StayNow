package com.ph32395.staynow_datn.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow_datn.ChucNangNhanTinCC.Chat
import com.ph32395.staynow_datn.ChucNangNhanTinCC.TextingMessengeActivity
import com.ph32395.staynow_datn.databinding.FragmentMessageBinding

class MessageFragment : Fragment() {

    private var TAG = "zzzMessageFragmentzzz"
    private lateinit var binding: FragmentMessageBinding
    private lateinit var adapterMessage: MessageAdapter
    private val data = FirebaseFirestore.getInstance()
    private val database = Firebase.database.reference
    private val statusMessageRef = data.collection("DieuKienChat")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessageBinding.inflate(inflater, container, false)

        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_message, container, false)
        binding.btnAdminSuport.setOnClickListener {
            val adminId = "oNzHlt3U8gNirb9DmT7sf72HXb92" // ID của admin bạn muốn truyền
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
        Log.d(TAG, "onCreateView:  vao onCreateView")

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        Log.d(TAG, "onCreate:userId $userId")
        if (userId == null) {
            Log.d("MessagerFragment", "Khong co iuser")
        } else {
            fetchChatList(userId) {
                if (it.isEmpty()) {
                    binding.layoutMessageNull.visibility = View.VISIBLE
                    binding.rcvListTinNhan.visibility = View.GONE
                } else {
                    binding.layoutMessageNull.visibility = View.GONE
                    binding.rcvListTinNhan.visibility = View.VISIBLE
                    Log.d(TAG, "onCreate:it List chat $it")
                    adapterMessage = MessageAdapter(it) {
                        Log.d(TAG, "onCreate: it.time $it")
                        val intent = Intent(context, TextingMessengeActivity::class.java)
                        intent.putExtra("chatId", it.maTinNhan)
                        intent.putExtra("userChat", it.maNguoiDungKhac)
                        startActivity(intent)
                    }
                    binding.rcvListTinNhan.layoutManager = LinearLayoutManager(context)
                    binding.rcvListTinNhan.adapter = adapterMessage

                }
            }
        }

        fetchStatusMessage {
            val adapter = UserStatusOnOfAdapter(it) {
                val intent = Intent(context, TextingMessengeActivity::class.java)
                intent.putExtra("userId", it.maNguoiDung)
                startActivity(intent)
            }
            val linearLayoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.rcvListUser.layoutManager = linearLayoutManager
            binding.rcvListUser.adapter = adapter
        }


        return binding.root

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: vao onCreate")
    }

    fun fetchChatList(userId: String, onResult: (List<Chat>) -> Unit) {
        val database = Firebase.database.reference
        database.child("DanhSachTroChuyen").child(userId).orderByChild("thoiGianTinNhanCuoi")
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
        val maNguoiDung: String = "",
        val hoTen: String = "",
        val anhDaiDien: String = "",
        val status: String = ""
    ) {
        constructor() : this("", "", "", "")
    }

    data class StatusMessage(
        val maNguoiThue: String = "",
        val maNguoiChoThue: String = ""
    ) {
        constructor() : this("", "")
    }

    fun fetchStatusMessage(onResult: (List<UserStatus>) -> Unit) {
        val idUser = FirebaseAuth.getInstance().currentUser?.uid
        Log.e(TAG, "fetchStatusMessage:idUser uid $idUser")

        val listUserId = mutableListOf<String>()
        val listUser = mutableListOf<UserStatus>()

        statusMessageRef.get().addOnSuccessListener { documents ->
            for (document in documents.documents) {
                val statusMessage = document.toObject(StatusMessage::class.java)
                if (statusMessage?.maNguoiThue == idUser) {
                    statusMessage?.maNguoiChoThue?.let { listUserId.add(it) }
                }
            }

            val newlistUserId = listUserId.distinct()
            Log.e(TAG, "fetchStatusMessage: newlistUserId $newlistUserId")

            newlistUserId.forEach { userId ->
                database.child("NguoiDung").child(userId)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val maNguoiDung = snapshot.child("maNguoiDung").value.toString()
                            val hoTen = snapshot.child("hoTen").value.toString()
                            val anhDaiDien = snapshot.child("anhDaiDien").value.toString()
                            val status = snapshot.child("status").value.toString()

                            // Tìm user đã tồn tại và cập nhật trạng thái mới
                            val existingUserIndex = listUser.indexOfFirst { it.maNguoiDung == maNguoiDung }
                            if (existingUserIndex != -1) {
                                listUser[existingUserIndex] = UserStatus(maNguoiDung, hoTen, anhDaiDien, status)
                            } else {
                                listUser.add(UserStatus(maNguoiDung, hoTen, anhDaiDien, status))
                            }

                            // Gọi hàm onResult mỗi khi dữ liệu thay đổi
                            onResult(listUser)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e(TAG, "onCancelled: Error Realtime ${error.message}")
                        }
                    })
            }
        }.addOnFailureListener {
            Log.e(TAG, "fetchStatusMessage: Error Fetch Status Messages ${it.message}")
        }
    }


}