package com.ph32395.staynow.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.ph32395.staynow.ChucNangNhanTinCC.Chat
import com.ph32395.staynow.ChucNangNhanTinCC.TextingMessengeActivity
import com.ph32395.staynow.R
import com.ph32395.staynow.databinding.FragmentMessageBinding

class MessageFragment : Fragment() {

    private var TAG = "zzzMessageFragmentzzz"
    private lateinit var binding: FragmentMessageBinding
    private lateinit var adapterMessage: MessageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessageBinding.inflate(inflater, container, false)

        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_message, container, false)

        return binding.root

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        Log.d(TAG, "onCreate:userId $userId")
        if (userId != null) {
            fetchChatList(userId) {
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
        } else {
            return

        }

    }

    fun fetchChatList(userId: String, onResult: (List<Chat>) -> Unit) {
        val database = Firebase.database.reference
        database.child("ChatList").child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatList = mutableListOf<Chat>()
                for (chatSnapshot in snapshot.children) {
                    val chat = chatSnapshot.getValue(Chat::class.java)
                    chat?.let { chatList.add(it) }
                }
                Log.d(TAG, "onDataChange:chatList $chatList")
                onResult(chatList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to fetch chat list: ${error.message}")
            }
        })
    }


}