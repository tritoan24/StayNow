package com.ph32395.staynow.QuanLyPhongTro.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ph32395.staynow.Model.PhongTroModel
import com.ph32395.staynow.R
import com.ph32395.staynow.databinding.FragmentPhongChoDuyetBinding
import com.ph32395.staynow.fragment.home.HomeViewModel
import com.ph32395.staynow.fragment.home.PhongTroAdapter


class PhongChoDuyetFragment : Fragment() {
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
        val binding = inflater.inflate(R.layout.fragment_phong_cho_duyet, container, false)

        val txtNoRoomChoDuyet = binding.findViewById<TextView>(R.id.txtNoRoomChoDuyet)

        recyclerView = binding.findViewById(R.id.recyclerViewPhongChoDuyet)

        Log.d("PhongChoDuyetFragment", "Setting up RecyclerView and Adapter")
        // Gán LayoutManager cho RecyclerView
        recyclerView.layoutManager = GridLayoutManager(context, 2)

        // Gán Adapter cho RecyclerView
        roomAdapter = PhongTroAdapter(mutableListOf(), viewModel) // Cập nhật lại list phòng từ viewModel
        recyclerView.adapter = roomAdapter

        // Lắng nghe thay đổi từ ViewModel
        viewModel.phongChoDuyet.observe(viewLifecycleOwner) { rooms ->
            if (rooms.isEmpty()) {
                txtNoRoomChoDuyet.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                txtNoRoomChoDuyet.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                roomAdapter.updateRoomList(rooms)
            }

        }

        return binding
    }
}