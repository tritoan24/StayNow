package com.ph32395.staynow.fragment.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow.Model.PhongTroModel
import com.ph32395.staynow.R
import com.ph32395.staynow.databinding.FragmentTabHomeBinding

class HomeTabFragment : Fragment(R.layout.fragment_tab_home) {

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var binding: FragmentTabHomeBinding  // Đối tượng ViewBinding
    private val roomList = mutableListOf<PhongTroModel>()
    private lateinit var roomsRef: DatabaseReference
    private lateinit var roomAdapter: PhongTroAdapter
    private var roomEventListener: ValueEventListener? = null

    companion object {
        fun newInstance(id_LoaiPhong: String): HomeTabFragment {
            val fragment = HomeTabFragment()
            val args = Bundle()
            args.putString("categoryId", id_LoaiPhong)  // Truyền id của loại phòng trọ
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTabHomeBinding.bind(view)

        val id_LoaiPhong = arguments?.getString("categoryId")
        // Setup RecyclerView
        setupRecyclerView()

        // Initialize Firebase reference
        roomsRef = FirebaseDatabase.getInstance().getReference("PhongTro")

        id_LoaiPhong?.let {
            updateRoomList(it)
            Log.d("HomeTabFragment", "Selected Category ID: $it")
        }
        // Observe selected category from ViewModel
        sharedViewModel.selectedLoaiPhongTro.observe(viewLifecycleOwner) { idloaiPhongTro ->
            updateRoomList(idloaiPhongTro)
        }

    }

    private fun setupRecyclerView() {
        binding.roomRclView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            overScrollMode = View.OVER_SCROLL_NEVER
            roomAdapter = PhongTroAdapter(roomList)
            adapter = roomAdapter
        }
    }

    private fun updateRoomList(loaiPhongTro: String) {
        // Remove previous listener if exists
        roomEventListener?.let { roomsRef.removeEventListener(it) }

        roomEventListener = if (loaiPhongTro == "0") {
            roomsRef.addValueEventListener(createEventListener())
        } else {
            roomsRef.orderByChild("maLoaiPhongTro").equalTo(loaiPhongTro)
                .addValueEventListener(createEventListener())
        }
    }

    private fun createEventListener(): ValueEventListener {
        return object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                roomList.clear()
                for (roomSnapshot in snapshot.children) {
                    roomSnapshot.getValue(PhongTroModel::class.java)?.let { roomList.add(it) }
                }
                roomAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {
                error.toException().printStackTrace()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        roomEventListener?.let { roomsRef.removeEventListener(it) }
        roomEventListener = null
    }
}
