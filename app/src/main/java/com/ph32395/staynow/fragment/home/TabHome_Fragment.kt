package com.ph32395.staynow.fragment.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow.Model.PhongTroModel
import com.ph32395.staynow.R
import com.ph32395.staynow.databinding.FragmentTabHomeBinding

class HomeTabFragment : Fragment(R.layout.fragment_tab_home) {

    private val homeViewModel: HomeViewModel by activityViewModels()
    private lateinit var binding: FragmentTabHomeBinding  // Đối tượng ViewBinding
    private val roomList = mutableListOf<Pair<String, PhongTroModel>>()
    private lateinit var db: FirebaseFirestore
    private lateinit var roomAdapter: PhongTroAdapter

    companion object {
        fun newInstance(maLoaiPhong: String): HomeTabFragment {
            val fragment = HomeTabFragment()
            val args = Bundle()
            args.putString("id_loaiphong", maLoaiPhong)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTabHomeBinding.bind(view)

        // Initialize Firebase reference
        db = FirebaseFirestore.getInstance()

        // Setup RecyclerView
        setupRecyclerView()

        homeViewModel.selectedLoaiPhongTro.observe(viewLifecycleOwner) { idloaiPhongTro ->
            homeViewModel.updateRoomList(idloaiPhongTro)
        }
        homeViewModel.roomList.observe(viewLifecycleOwner) { roomList ->
            handleRoomList(roomList)
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


    @SuppressLint("NotifyDataSetChanged")
    private fun handleRoomList(roomList: List<Pair<String, PhongTroModel>>) {
        this.roomList.clear()
        this.roomList.addAll(roomList)
        roomAdapter.notifyDataSetChanged()
    }


}
