package com.ph32395.staynow.fragment.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.ph32395.staynow.R
import com.ph32395.staynow.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private lateinit var storage: FirebaseStorage
    private lateinit var imageSlider: ImageSlider
    private lateinit var binding: FragmentHomeBinding

    private lateinit var onTabSelectedListener: OnTabSelectedListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnTabSelectedListener) {
            onTabSelectedListener = context
        } else {
            throw ClassCastException("$context must implement OnTabSelectedListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        binding = FragmentHomeBinding.bind(view)
        imageSlider = view.findViewById(R.id.imageSlider)
        storage = Firebase.storage

        //load banner
        loadImagesFromFirebase()

        val tabLayout: TabLayout = binding.tabLayoutHome
        val viewPager: ViewPager2 = binding.viewPagerHome
        val adapter = ViewPagerHomeAdapter(this)
        viewPager.adapter = adapter

        //sự kiện ấn tìm kiếm chuyển màn
        binding.viewLocationSearch.searchLayout.setOnClickListener {
            Toast.makeText(context, "Tính năng đang chờ phát triển", Toast.LENGTH_SHORT).show()
        }

        // Kết nối TabLayout với ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // Cài đặt tên hoặc icon cho mỗi tab
            when (position) {
                0 -> tab.text = "Tất cả"
                1 -> tab.text = "Chung cư mini"
                2 -> tab.text = "Phòng trọ"
                3 -> tab.text = "HomeStay"
            }

        }.attach()

// Lắng nghe sự kiện tab được chọn
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    // Chỉ gọi phương thức khi tab được chọn
                    onTabSelectedListener.onTabSelected(it.text.toString())
                    Log.d("ONTABSELECTED", "Tab selected: ${it.text}")
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        return view
    }

    private fun loadImagesFromFirebase() {
        val imageList = ArrayList<SlideModel>()
        val storageRef = storage.reference.child("banners")

        // List tất cả các ảnh trong thư mục "avatars"
        storageRef.listAll().addOnSuccessListener { listResult ->
            listResult.items.forEach { item ->
                // Lấy URL của từng ảnh
                item.downloadUrl.addOnSuccessListener { uri ->
                    imageList.add(SlideModel(uri.toString()))

                    // Set danh sách ảnh cho slider sau khi thêm tất cả ảnh
                    if (imageList.size == listResult.items.size) {
                        imageSlider.setImageList(imageList, ScaleTypes.CENTER_CROP)
                    }

                }.addOnFailureListener { exception ->
                    Log.e("HomeFragment", "Failed to get image URL", exception)

                }.addOnFailureListener { exception ->
                    Log.e("HomeFragment", "Failed to get image URL", exception)
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("HomeFragment", "Failed to list images", exception)
        }
    }


}

// Interface để HomeFragment thông báo sự thay đổi tab cho HomeTabFragment
interface OnTabSelectedListener {
    fun onTabSelected(loaiPhongTro: String)
}


