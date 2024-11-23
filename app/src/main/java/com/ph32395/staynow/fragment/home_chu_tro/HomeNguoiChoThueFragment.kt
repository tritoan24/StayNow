package com.ph32395.staynow.fragment.home_chu_tro

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.ph32395.staynow.ChucNangTimKiem.SearchActivity
import com.ph32395.staynow.R
import com.ph32395.staynow.databinding.FragmentHomeBinding
import com.ph32395.staynow.databinding.FragmentHomeNguoiChoThueBinding
import com.ph32395.staynow.fragment.home.HomeViewModel

class HomeNguoiChoThueFragment : Fragment() {

    private lateinit var binding: FragmentHomeNguoiChoThueBinding
    private val homeViewModel: HomeViewModel by activityViewModels()
    private lateinit var swipeFresh: SwipeRefreshLayout // keo de lam moiw noi dung
    private lateinit var imageSliderChuTro: ImageSlider
    private lateinit var roomNguoiChoThueAdapter: RoomNguoiChoThueAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_home_nguoi_cho_thue, container, false)
        binding = FragmentHomeNguoiChoThueBinding.inflate(inflater, container, false)
        swipeFresh = binding.swipeLayoutChuTro
        imageSliderChuTro = binding.imageSliderChuTro

//        Khoi tao recyclerView
        setupRecyclerView()
//        Quan sat du lieu
        observeData()

//        cap nhat du lieu ngay khi Fragment duoc tao
        homeViewModel.updateRoomListWithCoroutines()

//        Quan sat du lieu tu ViewModel
        homeViewModel.imageList.observe(viewLifecycleOwner) { imageList ->
            imageSliderChuTro.setImageList(imageList, ScaleTypes.CENTER_CROP)
        }

        swipeFresh.setOnRefreshListener {
            refreshData()
        }

//        Nhan vao thanh tim kiem
        binding.viewLocationSearch.searchLayout.setOnClickListener {
            startActivity(Intent(context, SearchActivity::class.java))
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        roomNguoiChoThueAdapter = RoomNguoiChoThueAdapter(homeViewModel)
        binding.recyclerViewDoiTac.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = roomNguoiChoThueAdapter
        }
    }

    private fun observeData() {
        homeViewModel.roomList.observe(viewLifecycleOwner) { rooms ->
            roomNguoiChoThueAdapter.submitList(rooms)
            swipeFresh.isRefreshing = false //Dung hieu ung lam moi
        }
    }


//    refresh lai Data tu Firebase
    private fun refreshData() {
        homeViewModel.updateRoomListWithCoroutines()
        swipeFresh.isRefreshing = false //tat hieu ung lam moi khi du lieu duoc tai xong
    }


}