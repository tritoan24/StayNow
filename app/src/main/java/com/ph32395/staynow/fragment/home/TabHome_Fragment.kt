package com.ph32395.staynow.fragment.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.ph32395.staynow.Model.PhongTro
import com.ph32395.staynow.R
import com.ph32395.staynow.databinding.FragmentTabHomeBinding

class HomeTabFragment : Fragment(R.layout.fragment_tab_home), OnTabSelectedListener {
    // Implement method của interface để nhận sự thay đổi tab
    override fun onTabSelected(loaiPhongTro: String) {
        Log.d("HomeTabFragment", "Tab selected: $loaiPhongTro")
        val roomAdapter = PhongTroAdapter(roomList)
        getDataFromRealtimeDatabase(roomAdapter, loaiPhongTro)
        Log.d("HomeTabFragment", "Loading rooms for category: $loaiPhongTro")
    }

    private var tabPosition: Int = 0
    private lateinit var binding: FragmentTabHomeBinding  // Đối tượng ViewBinding
    private val roomList: MutableList<PhongTro> = mutableListOf()  // Danh sách phòng trọ

    private lateinit var database: FirebaseDatabase
    private lateinit var roomsRef: DatabaseReference

    companion object {
        fun newInstance(position: Int): HomeTabFragment {
            val fragment = HomeTabFragment()
            val args = Bundle()
            args.putInt("tab_position", position)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentTabHomeBinding.bind(view)

        // Lấy vị trí tab từ arguments
        tabPosition = arguments?.getInt("tab_position", 0) ?: 0

        // Cài đặt RecyclerView và adapter
        binding.roomRclView.layoutManager = GridLayoutManager(requireContext(), 2)  // 2 cột
        binding.roomRclView.setOverScrollMode(View.OVER_SCROLL_NEVER)
        val roomAdapter = PhongTroAdapter(roomList)
        binding.roomRclView.adapter = roomAdapter

        // Cài đặt Firebase
        database = FirebaseDatabase.getInstance()
        roomsRef = database.getReference("PhongTro")

        // Lấy dữ liệu từ Realtime Database
        getDataFromRealtimeDatabase(roomAdapter, "Tất cả")
    }

    private fun getDataFromRealtimeDatabase(roomAdapter: PhongTroAdapter) {
        // Lấy dữ liệu từ node "rooms"
        roomsRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                // Xóa danh sách cũ trước khi thêm dữ liệu mới
                roomList.clear()

                // Duyệt qua các node con trong "rooms"
                for (roomSnapshot in snapshot.children) {
                    val room = roomSnapshot.getValue(PhongTro::class.java)
                    room?.let {
                        roomList.add(it)
                    }
                }

                // Cập nhật adapter khi đã có dữ liệu
                roomAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi nếu có
                error.toException().printStackTrace()
            }
        })
    }

    private fun getDataFromRealtimeDatabase(roomAdapter: PhongTroAdapter, loaiPhongTro: String) {
        roomsRef.orderByChild("loaiPhongTro").equalTo(loaiPhongTro)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    roomList.clear()
                    for (roomSnapshot in snapshot.children) {
                        val room = roomSnapshot.getValue(PhongTro::class.java)
                        room?.let {
                            roomList.add(it)
                        }
                    }
                    roomAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    error.toException().printStackTrace()
                }
            })
    }


}
