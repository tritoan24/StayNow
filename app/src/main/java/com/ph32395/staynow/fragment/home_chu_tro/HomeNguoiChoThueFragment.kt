package com.ph32395.staynow.fragment.home_chu_tro

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.ph32395.staynow.ChucNangTimKiem.SearchActivity
import com.ph32395.staynow.hieunt.view.feature.notification.NotificationActivity
import com.ph32395.staynow.hieunt.view_model.NotificationViewModel
import com.ph32395.staynow.databinding.FragmentHomeNguoiChoThueBinding
import com.ph32395.staynow.fragment.home.HomeViewModel
import com.ph32395.staynow.hieunt.view_model.ViewModelFactory
import com.ph32395.staynow.hieunt.widget.gone
import com.ph32395.staynow.hieunt.widget.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeNguoiChoThueFragment : Fragment() {

    private lateinit var binding: FragmentHomeNguoiChoThueBinding
    private val homeViewModel: HomeViewModel by activityViewModels()
    private lateinit var swipeFresh: SwipeRefreshLayout // keo de lam moiw noi dung
    private lateinit var imageSliderChuTro: ImageSlider
    private lateinit var roomNguoiChoThueAdapter: RoomNguoiChoThueAdapter
    private lateinit var notificationViewModel: NotificationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_home_nguoi_cho_thue, container, false)
        binding = FragmentHomeNguoiChoThueBinding.inflate(inflater, container, false)
        swipeFresh = binding.swipeLayoutChuTro
        imageSliderChuTro = binding.imageSliderChuTro

        notificationViewModel = ViewModelProvider(this, ViewModelFactory(requireContext()))[NotificationViewModel::class.java]
        lifecycleScope.launch (Dispatchers.IO){
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                notificationViewModel.notificationsState.collect {
                    val countNotificationNotSeen = async { it.filter { notificationModel -> !notificationModel.isRead }.size }.await()
                    withContext(Dispatchers.Main){
                        if (countNotificationNotSeen > 0){
                            binding.notificationBadge.visible()
                            binding.notificationBadge.text = countNotificationNotSeen.toString()
                        } else {
                            binding.notificationBadge.gone()
                        }
                    }
                }
            }
        }

        //tritoan code thong bao
        binding.fNotification.setOnClickListener {
            startActivity(Intent(context, NotificationActivity::class.java))
        }
        // Quan sát số lượng thông báo chưa đọc
//        notificationViewModel.unreadCount.observe(viewLifecycleOwner) { count ->
//            if (count > 0) {
//                binding.notificationBadge.text = count.toString()
//                binding.notificationBadge.visibility = View.VISIBLE
//            } else {
//                binding.notificationBadge.visibility = View.GONE
//            }
//        }


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