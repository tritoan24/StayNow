package com.ph32395.staynow.QuanLyPhongTro.fragment

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
import com.ph32395.staynow.databinding.FragmentPhongDaBiHuyBinding
import com.ph32395.staynow.fragment.home.HomeViewModel
import com.ph32395.staynow.fragment.home.PhongTroAdapter

class PhongDaBiHuyFragment : Fragment() {
    private lateinit var viewModel: HomeViewModel
    private lateinit var roomAdapter: PhongTroAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //        Khoi tao viewModel
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        val sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val maNguoiDung = sharedPreferences.getString("Ma_nguoidung", null)

        if (maNguoiDung != null) {
            val viewModel: HomeViewModel by viewModels()
            viewModel.loadRoomByStatus(maNguoiDung)
        } else {
            Log.e("QuanLyPhongTroActivity", "Ma_nguoidung is null. Cannot load rooms.")
        }

        // Inflate the layout for this fragment
        val binding = inflater.inflate(R.layout.fragment_phong_da_bi_huy, container, false)

        val txtNoRoomDaBiHuy = binding.findViewById<TextView>(R.id.txtNoRoomDaBiHuy)

        recyclerView = binding.findViewById(R.id.recyclerViewPhongDahuy)

        Log.d("PhongDaBiHuyFragment", "Setting up RecyclerView and Adapter")
        recyclerView.layoutManager = GridLayoutManager(context, 2)

        // Setup RecyclerView
        roomAdapter = PhongTroAdapter(mutableListOf(), viewModel)
        recyclerView.adapter = roomAdapter

        // Observe phongDaHuy LiveData from ViewModel
        viewModel.phongDaHuy.observe(viewLifecycleOwner, Observer { roomList ->
            if (roomList.isEmpty()) {
                txtNoRoomDaBiHuy.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                txtNoRoomDaBiHuy.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                roomAdapter.updateRoomList(roomList)
            }

        })

        return binding

    }
}