package com.ph32395.staynow_datn.QuanLyPhongTro.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.fragment.home.HomeViewModel
import com.ph32395.staynow_datn.fragment.home.PhongTroAdapter


class PhongChoDuyetFragment : Fragment() {
    private lateinit var viewModel: HomeViewModel
    private lateinit var roomAdapter: PhongTroAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var txtNoRoomChoDuyet: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = inflater.inflate(R.layout.fragment_phong_cho_duyet, container, false)

        // Khởi tạo các view
        recyclerView = binding.findViewById(R.id.recyclerViewPhongChoDuyet)
        txtNoRoomChoDuyet = binding.findViewById(R.id.txtNoRoomChoDuyet)

        // Lấy ViewModel được chia sẻ từ Activity
        viewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        setupRecyclerView()
        observeRoomData()

        return binding
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        roomAdapter = PhongTroAdapter(mutableListOf(), viewModel)
        recyclerView.adapter = roomAdapter
    }

    private fun observeRoomData() {
        viewModel.phongChoDuyet.observe(viewLifecycleOwner) { roomList ->
            if (roomList.isEmpty()) {
                txtNoRoomChoDuyet.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                txtNoRoomChoDuyet.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                roomAdapter.updateRoomList(roomList)
            }
        }
    }
}