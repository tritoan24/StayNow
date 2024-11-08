package com.ph32395.staynow.fragment.home

import android.annotation.SuppressLint
import android.content.Intent
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
import com.ph32395.staynow.Activity.RoomDetailActivity
import com.ph32395.staynow.Model.PhongTroModel
import com.google.firebase.database.*
import com.ph32395.staynow.Model.PhongTro
import com.ph32395.staynow.R
import com.ph32395.staynow.databinding.FragmentTabHomeBinding

class HomeTabFragment : Fragment(R.layout.fragment_tab_home), OnTabSelectedListener {
    // Implement method của interface để nhận sự thay đổi tab
    override fun onTabSelected(loaiPhongTro: String) {
        Log.d("HomeTabFragment", "Tab selected: $loaiPhongTro")
        val roomAdapter = PhongTroAdapter(roomList) { room ->
            val intent = Intent(requireContext(), RoomDetailActivity::class.java).apply {
                putExtra("tenPhongTro", room.tenPhongTro)
                putExtra("giaThue", room.giaThue)
                putExtra("diaChi", room.diaChi)
                putExtra("dienTich", room.dienTich)
                putExtra("maPhongTro", room.maPhongTro)
                putExtra("tang", room.tang)
                putExtra("soNguoi", room.soNguoi)
                putExtra("tienCoc", room.tienCoc)
                putExtra("motaChiTiet", room.motaChiTiet)
                putStringArrayListExtra("danhSachAnh", ArrayList(room.danhSachAnh))
                putExtra("gioiTinh", room.gioiTinh)
                putExtra("trangThai", room.trangThai)
            }
            startActivity(intent)
        }
        getDataFromRealtimeDatabase(roomAdapter, loaiPhongTro)
        Log.d("HomeTabFragment", "Loading rooms for category: $loaiPhongTro")
    }
class HomeTabFragment : Fragment(R.layout.fragment_tab_home) {

    private lateinit var binding: FragmentTabHomeBinding
    private val roomList = mutableListOf<PhongTro>()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var tabPosition: Int = 0
    private lateinit var binding: FragmentTabHomeBinding  // Đối tượng ViewBinding
    private val roomList: MutableList<PhongTroModel> = mutableListOf()  // Danh sách phòng trọ

    private lateinit var database: FirebaseDatabase
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
        }
        // Observe selected category from ViewModel
        sharedViewModel.selectedLoaiPhongTro.observe(viewLifecycleOwner) { idloaiPhongTro ->
            updateRoomList(idloaiPhongTro)
        }
    }

        // Cài đặt RecyclerView và adapter
        binding.roomRclView.layoutManager = GridLayoutManager(requireContext(), 2)  // 2 cột
        binding.roomRclView.setOverScrollMode(View.OVER_SCROLL_NEVER)
        val roomAdapter = PhongTroAdapter(roomList) { room ->
            val intent = Intent(requireContext(), RoomDetailActivity::class.java).apply {
                putExtra("tenPhongTro", room.tenPhongTro)
                putExtra("giaThue", room.giaThue)
                putExtra("diaChi", room.diaChi)
                putExtra("dienTich", room.dienTich)
                putExtra("maPhongTro", room.maPhongTro)
                putExtra("tang", room.tang)
                putExtra("soNguoi", room.soNguoi)
                putExtra("tienCoc", room.tienCoc)
                putExtra("motaChiTiet", room.motaChiTiet)
                putStringArrayListExtra("danhSachAnh", ArrayList(room.danhSachAnh))
                putExtra("gioiTinh", room.gioiTinh)
                putExtra("trangThai", room.trangThai)
            }
            startActivity(intent)
        }
        binding.roomRclView.adapter = roomAdapter
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
                    roomSnapshot.getValue(PhongTro::class.java)?.let { roomList.add(it) }
                    val room = roomSnapshot.getValue(PhongTroModel::class.java)
                    room?.let {
                        roomList.add(it)
                    }
                }
                roomAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                error.toException().printStackTrace()
            }
        }
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
                        val room = roomSnapshot.getValue(PhongTroModel::class.java)
                        room?.let {
                            roomList.add(it)
                        }
                    }
                    roomAdapter.notifyDataSetChanged()
                }

    override fun onDestroyView() {
        super.onDestroyView()
        roomEventListener?.let { roomsRef.removeEventListener(it) }
        roomEventListener = null
    }
}
