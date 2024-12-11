package com.ph32395.staynow.fragment

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
import com.ph32395.staynow.ChucNangNhanTinCC.Chat
import com.ph32395.staynow.ChucNangNhanTinCC.TextingMessengeActivity
import com.ph32395.staynow.databinding.FragmentMessageBinding

class MessageFragment : Fragment() {

    private var TAG = "zzzMessageFragmentzzz"
    private lateinit var binding: FragmentMessageBinding
    private lateinit var adapterMessage: MessageAdapter
    private val listUser = mutableListOf<UserStatus>()
    private val data = FirebaseFirestore.getInstance()
    private val statusMessageRef = data.collection("StatusMessages")

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
        Log.d(TAG, "onCreateView:  vao onCreateView")

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        Log.d(TAG, "onCreate:userId $userId")
        fetchChatList(userId!!) {
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
                    intent.putExtra("chatId", it.chatId)
                    intent.putExtra("userChat", it.otherUserId)
                    startActivity(intent)
                }
                binding.rcvListTinNhan.layoutManager = LinearLayoutManager(context)
                binding.rcvListTinNhan.adapter = adapterMessage

            }
        }
        fetchStatusMessage {
            val adapter = UserStatusOnOfAdapter(it) {
                val intent = Intent(context, TextingMessengeActivity::class.java)
                intent.putExtra("userId", it.ma_nguoidung)
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

    data class StatusMessage(
        val landlordId: String = "",
        val tenantId: String = ""
    ) {
        constructor() : this("", "")
    }

    fun fetchStatusMessage(onResult: (List<UserStatus>) -> Unit) {
        val idUser = FirebaseAuth.getInstance().currentUser?.uid
        val database = Firebase.database.reference
        Log.e(TAG, "fetchStatusMessage:idUser uid $idUser")
        val listUserId = mutableListOf<String>()
        statusMessageRef.get().addOnSuccessListener {

            for (document in it.documents) {
                //Log.d(TAG, "fetchStatusMessage:documentForIn $document")
                val statusMessage = document.toObject(StatusMessage::class.java)
                Log.d(TAG, "fetchStatusMessage:statusMessage $statusMessage")
                if (statusMessage?.tenantId == idUser) {
                    Log.e(TAG, "fetchStatusMessage:statusMessage $statusMessage")
                    Log.e(TAG, "fetchStatusMessage: idUser Khac ${statusMessage?.landlordId}")
                    statusMessage?.landlordId?.let { it1 -> listUserId.add(it1) }
                }

            }
            Log.d(TAG, "fetchStatusMessage: listUserId $listUserId")
            val newlistUserId = listUserId.distinct()
            Log.e(TAG, "fetchStatusMessage: newlistUserId $newlistUserId")
            listUser.clear()
            newlistUserId.forEach {
                database.child("NguoiDung").child(it)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            Log.d(TAG, "onDataChange: ${snapshot.value}")
                            val maNguoiDung =
                                snapshot.child("ma_nguoidung").value.toString()
                            val ho_ten = snapshot.child("ho_ten").value.toString()
                            val anhDaiDien =
                                snapshot.child("anh_daidien").value.toString()
                            val status = snapshot.child("status").value.toString()
                            val user = UserStatus(maNguoiDung, ho_ten, anhDaiDien, status)
                            Log.e(TAG, "onDataChange: user$user")
                            listUser.add(user)
                            Log.d(TAG, "fetchStatusMessage:listUser $listUser")
                            onResult(listUser)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e(TAG, "onCancelled:Error RealTime ${error.message} ")
                        }
                    })
            }


        }.addOnFailureListener {
            Log.e(TAG, "fetchStatusMessage: Error Fetch Status Messages ${it.message.toString()}")
        }

    }


}