package com.ph32395.staynow_datn.QuanLyPhongTro.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.fragment.home.HomeViewModel
import com.ph32395.staynow_datn.fragment.home.PhongTroAdapter

class PhongDaDangFragment : Fragment() {
    private lateinit var viewModel: HomeViewModel
    private lateinit var roomAdapter: PhongTroAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //        Khoi tao viewModel
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        val viewModel: HomeViewModel by viewModels()
        viewModel.loadRoomByStatus(FirebaseAuth.getInstance().currentUser?.uid ?: "")

        // Inflate the layout for this fragment
        val binding = inflater.inflate(R.layout.fragment_phong_da_dang, container, false)

        val txtNoRoomDaDang = binding.findViewById<TextView>(R.id.txtNoRoomDaDang)

        recyclerView = binding.findViewById(R.id.recyclerViewPhongDaDang)
        Log.d("PhongDaDangFragment", "Setting up RecyclerView and Adapter")
        recyclerView.layoutManager = GridLayoutManager(context, 2)

        // Setup RecyclerView
        roomAdapter = PhongTroAdapter(mutableListOf(), viewModel)
        recyclerView.adapter = roomAdapter

        // Quan sat du lieu tu ViewModel
        viewModel.phongDaDang.observe(viewLifecycleOwner, Observer { roomList ->
            if (roomList.isEmpty()) {
                txtNoRoomDaDang.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                txtNoRoomDaDang.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                roomAdapter.updateRoomList(roomList)
            }

        })

        return binding
    }
}
