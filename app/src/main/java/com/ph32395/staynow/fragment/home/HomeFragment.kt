package com.ph32395.staynow.fragment.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ph32395.staynow.Model.LoaiPhongTro
import com.ph32395.staynow.ThongBao.NotificationActivity
import com.ph32395.staynow.ThongBao.NotificationViewModel
import com.ph32395.staynow.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by activityViewModels()
    private lateinit var swipeFresh: SwipeRefreshLayout
    private lateinit var imageSlider: ImageSlider
    private lateinit var notificationViewModel: NotificationViewModel

    private val currentUser = FirebaseAuth.getInstance().currentUser


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        swipeFresh = binding.swipeRefreshLayout
        imageSlider = binding.imageSlider

        // Quan sát LiveData từ ViewModel
        homeViewModel.loaiPhongTroList.observe(viewLifecycleOwner) { loaiPhongTroList ->
            setupTabs(loaiPhongTroList)
        }

        homeViewModel.imageList.observe(viewLifecycleOwner) { imageList ->
            imageSlider.setImageList(imageList, ScaleTypes.CENTER_CROP)
        }

        swipeFresh.setOnRefreshListener {
            refreshData()
        }
        // Load dữ liệu ban đầu
        homeViewModel.loadLoaiPhongTro()
        homeViewModel.loadImagesFromFirebase()

        binding.viewLocationSearch.searchLayout.setOnClickListener {
            Toast.makeText(context, "Tính năng đang chờ phát triển", Toast.LENGTH_SHORT).show()
        }






        //màn hình thông báo tritoancode
        binding.fNotification.setOnClickListener {
            startActivity(Intent(context, NotificationActivity::class.java))
        }
        //đếm số thông báo chưa đọc

        notificationViewModel = ViewModelProvider(requireActivity()).get(NotificationViewModel::class.java)
// Trong Fragment
        notificationViewModel.unreadCount.observe(viewLifecycleOwner) { count ->
            Log.d("Notification", "Unread count: $count")
            if (count > 0) {
                binding.notificationBadge.text = count.toString()
                binding.notificationBadge.visibility = View.VISIBLE
            } else {
                binding.notificationBadge.visibility = View.GONE
            }
        }

// Gọi fetchNotifications sau khi set observer
        notificationViewModel.fetchNotifications(currentUser?.uid ?: "")


        return binding.root
    }

    private fun setupTabs(loaiPhongTroList: List<LoaiPhongTro>) {
        val tabLayout: TabLayout = binding.tabLayoutHome
        val viewPager: ViewPager2 = binding.viewPagerHome
        val adapter = ViewPagerHomeAdapter(this, loaiPhongTroList)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            if (position < loaiPhongTroList.size) {
                tab.text = loaiPhongTroList[position].Ten_loaiphong
            }
        }.attach()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            @SuppressLint("SuspiciousIndentation")
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position < loaiPhongTroList.size) {
                    val selectedLoaiPhongTro = loaiPhongTroList[position].Ma_loaiphong
                        homeViewModel.selectLoaiPhongTro(selectedLoaiPhongTro)

                }
            }
        })

    }

    private fun refreshData() {
        homeViewModel.clearRoomCache()  // Reset cache
        homeViewModel.loadLoaiPhongTro()
        homeViewModel.loadImagesFromFirebase()
        homeViewModel.selectedLoaiPhongTro.value?.let { maloaiPhong ->
            homeViewModel.updateRoomList(maloaiPhong)  // Fetch lại danh sách phòng trọ theo loại
        }
        Handler(Looper.getMainLooper()).postDelayed({
            swipeFresh.isRefreshing = false
        }, 2000)
    }
}
