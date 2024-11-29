package com.ph32395.staynow.QuanLyPhongTro.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ph32395.staynow.Model.PhongTroModel
import com.ph32395.staynow.R
import com.ph32395.staynow.databinding.FragmentPhongDangLuuBinding
import com.ph32395.staynow.fragment.home.HomeViewModel
import com.ph32395.staynow.fragment.home.PhongTroAdapter

class PhongDangLuuFragment : Fragment() {
    private lateinit var viewModel: HomeViewModel
    private lateinit var roomAdapter: PhongTroAdapter
    private lateinit var recyclerView: RecyclerView

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //        Khoi tao viewModel
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        val viewModel: HomeViewModel by viewModels()
        viewModel.loadRoomByStatus()

        // Inflate the layout for this fragment
        val binding = inflater.inflate(R.layout.fragment_phong_dang_luu, container, false)

        val txtNoRoomDangLuu = binding.findViewById<TextView>(R.id.txtNoRoomDangLuu)

       recyclerView = binding.findViewById(R.id.recyclerViewPhongDangLuu)
        Log.d("PhongDangLuuFragment", "Setting up RecyclerView and Adapter")
        recyclerView.layoutManager = GridLayoutManager(context, 2)

        // Setup RecyclerView
        roomAdapter = PhongTroAdapter(mutableListOf(), viewModel)
        recyclerView.adapter = roomAdapter

        // Observe phongDangLuu LiveData from ViewModel
        viewModel.phongDangLuu.observe(viewLifecycleOwner, Observer { roomList ->
            if (roomList.isEmpty()) {
                txtNoRoomDangLuu.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                txtNoRoomDangLuu.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                roomAdapter.updateRoomList(roomList)
            }

        })

        return binding
    }
}