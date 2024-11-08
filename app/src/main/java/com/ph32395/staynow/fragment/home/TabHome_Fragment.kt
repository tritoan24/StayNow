package com.ph32395.staynow.fragment.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.*
import com.ph32395.staynow.Model.PhongTro
import com.ph32395.staynow.R
import com.ph32395.staynow.databinding.FragmentTabHomeBinding

class HomeTabFragment : Fragment(R.layout.fragment_tab_home) {

    private var tabPosition: Int = 0
    private lateinit var binding: FragmentTabHomeBinding
    private val roomList: MutableList<PhongTro> = mutableListOf()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var valueEventListener: ValueEventListener? = null

    private lateinit var database: FirebaseDatabase
    private lateinit var roomsRef: DatabaseReference
    private lateinit var roomAdapter: PhongTroAdapter

    companion object {
        fun newInstance(tabPosition: Int): HomeTabFragment {
            val fragment = HomeTabFragment()
            val args = Bundle()
            args.putInt("tab_position", tabPosition)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentTabHomeBinding.bind(view)

        // Lấy vị trí tab từ arguments
        tabPosition = arguments?.getInt("tab_position", 0) ?: 0

        binding.roomRclView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.roomRclView.overScrollMode = View.OVER_SCROLL_NEVER
        roomAdapter = PhongTroAdapter(roomList)
        binding.roomRclView.adapter = roomAdapter

        database = FirebaseDatabase.getInstance()
        roomsRef = database.getReference("PhongTro")
        getFullListRoom()
        sharedViewModel.selectedTab.observe(viewLifecycleOwner) { loaiPhongTro ->
            if (loaiPhongTro == "0") {
                getFullListRoom()

            } else {
                getListByCategory(loaiPhongTro)
            }
        }
    }

    private fun getFullListRoom() {
        roomsRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                roomList.clear()
                for (roomSnapshot in snapshot.children) {
                    val room = roomSnapshot.getValue(PhongTro::class.java)
                    room?.let { roomList.add(it) }
                    Log.d("HomeTabFragment", "Room: $room")
                }
                roomAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                error.toException().printStackTrace()
            }
        })
    }

    private fun getListByCategory(maLoaiPhongTro: String) {
        // Hủy listener cũ nếu có
        valueEventListener?.let { roomsRef.removeEventListener(it) }

        valueEventListener = roomsRef.orderByChild("maLoaiPhongTro").equalTo(maLoaiPhongTro)
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
