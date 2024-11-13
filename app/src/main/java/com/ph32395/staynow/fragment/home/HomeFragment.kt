package com.ph32395.staynow.fragment.home

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.ph32395.staynow.Model.LoaiPhongTro
import com.ph32395.staynow.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by activityViewModels()
    private lateinit var swipeFresh: SwipeRefreshLayout
    private lateinit var imageSlider: ImageSlider

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

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    val selectedPosition = it.position
                    if (selectedPosition < loaiPhongTroList.size) {
                        val selectedLoaiPhongTro = loaiPhongTroList[selectedPosition].Ma_loaiphong
                        homeViewModel.selectLoaiPhongTro(selectedLoaiPhongTro)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun refreshData() {
        homeViewModel.loadLoaiPhongTro()
        homeViewModel.loadImagesFromFirebase()

        Handler(Looper.getMainLooper()).postDelayed({
            swipeFresh.isRefreshing = false
        }, 2000)
    }
}
