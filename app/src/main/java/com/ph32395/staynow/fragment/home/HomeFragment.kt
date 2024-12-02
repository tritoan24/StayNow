package com.ph32395.staynow.fragment.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieAnimationView
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.ph32395.staynow.Model.LoaiPhongTro
import com.ph32395.staynow.databinding.FragmentHomeBinding
import com.ph32395.staynow.hieunt.database.db.AppDatabase
import com.ph32395.staynow.hieunt.view.feature.notification.NotificationActivity
import com.ph32395.staynow.hieunt.widget.gone
import com.ph32395.staynow.hieunt.widget.visible
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by activityViewModels()
    private lateinit var swipeFresh: SwipeRefreshLayout
    private lateinit var imageSlider: ImageSlider
    private lateinit var loadingIndicator: LottieAnimationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        swipeFresh = binding.swipeRefreshLayout
        imageSlider = binding.imageSlider
        loadingIndicator = binding.loadingIndicator


        // Load dữ liệu ban đầu
        loadData()


        // Quan sát LiveData từ ViewModel
        homeViewModel.loaiPhongTroList.observe(viewLifecycleOwner) { loaiPhongTroList ->
            setupTabs(loaiPhongTroList)
        }

        homeViewModel.imageList.observe(viewLifecycleOwner) { imageList ->
            imageSlider.setImageList(imageList, ScaleTypes.CENTER_CROP)
        }


        // Quan sát trạng thái loading
        homeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                loadingIndicator.visibility = View.VISIBLE
                loadingIndicator.playAnimation()
                binding.viewPagerHome.visibility = View.GONE
                binding.tabLayoutHome.visibility = View.GONE
            } else {
                loadingIndicator.visibility = View.GONE
                binding.viewPagerHome.visibility = View.VISIBLE
                binding.tabLayoutHome.visibility = View.VISIBLE
            }
        }

        swipeFresh.setOnRefreshListener {
            refreshData()
        }

        binding.viewLocationSearch.searchLayout.setOnClickListener {
            Toast.makeText(context, "Tính năng đang chờ phát triển", Toast.LENGTH_SHORT).show()
        }


        //màn hình thông báo tritoancode
        binding.fNotification.setOnClickListener {
            startActivity(Intent(context, NotificationActivity::class.java))
        }

        return binding.root
    }

    private fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Gọi loadImagesFromFirebase và đợi kết quả
            val imagesDeferred = async { homeViewModel.loadImagesFromFirebase() }
            imagesDeferred.await()  // Đợi xong rồi mới tiếp tục

            // Gọi loadLoaiPhongTro sau khi tải ảnh
            homeViewModel.loadLoaiPhongTro()
        }
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
        }, 0)
    }

    override fun onResume() {
        super.onResume()
        val countNotificationNotSeen = AppDatabase.getInstance(requireContext()).notificationDao().countNotificationNotSeen()
        if (countNotificationNotSeen > 0){
            binding.notificationBadge.visible()
            binding.notificationBadge.text = countNotificationNotSeen.toString()
        } else {
            binding.notificationBadge.gone()
        }
    }

}
