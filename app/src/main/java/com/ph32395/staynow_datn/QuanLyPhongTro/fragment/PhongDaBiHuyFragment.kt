package com.ph32395.staynow_datn.QuanLyPhongTro.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.fragment.home.HomeViewModel
import com.ph32395.staynow_datn.fragment.home.PhongTroAdapter


class PhongDaBiHuyFragment : Fragment() {
    private lateinit var viewModel: HomeViewModel
    private lateinit var roomAdapter: PhongTroAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var txtNoRoomDaBiHuy: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = inflater.inflate(R.layout.fragment_phong_da_bi_huy, container, false)

        // Khởi tạo các view
        recyclerView = binding.findViewById(R.id.recyclerViewPhongDahuy)
        txtNoRoomDaBiHuy = binding.findViewById(R.id.txtNoRoomDaBiHuy)

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
        viewModel.phongDaHuy.observe(viewLifecycleOwner) { roomList ->
            if (roomList.isEmpty()) {
                txtNoRoomDaBiHuy.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                txtNoRoomDaBiHuy.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                roomAdapter.updateRoomList(roomList)
            }
        }
    }
}