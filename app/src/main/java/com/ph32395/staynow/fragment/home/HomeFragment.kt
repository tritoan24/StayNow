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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.ph32395.staynow.Model.LoaiPhongTro
import com.ph32395.staynow.R
import com.ph32395.staynow.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private lateinit var storage: FirebaseStorage
    private lateinit var imageSlider: ImageSlider
    private lateinit var binding: FragmentHomeBinding
    private lateinit var onTabSelectedListener: OnTabSelectedListener

    // Danh sách mã loại phòng trọ để truyền đúng mã khi tab được chọn
    private val loaiPhongTroCodes = mutableListOf<String>()

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

        // Load banner images from Firebase Storage
        loadImagesFromFirebase()

        // Load LoaiPhongTro từ Firebase Realtime Database và setup TabLayout
        loadLoaiPhongTro()

        // Xử lý sự kiện nhấn vào tìm kiếm
        binding.viewLocationSearch.searchLayout.setOnClickListener {
            Toast.makeText(context, "Tính năng đang chờ phát triển", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun loadImagesFromFirebase() {
        val imageList = ArrayList<SlideModel>()
        val storageRef = storage.reference.child("banners")

        // Liệt kê tất cả ảnh trong thư mục "banners"
        storageRef.listAll().addOnSuccessListener { listResult ->
            listResult.items.forEach { item ->
                item.downloadUrl.addOnSuccessListener { uri ->
                    imageList.add(SlideModel(uri.toString()))
                    // Đặt danh sách ảnh vào slider sau khi tất cả ảnh đã tải
                    if (imageList.size == listResult.items.size) {
                        imageSlider.setImageList(imageList, ScaleTypes.CENTER_CROP)
                    }
                }.addOnFailureListener { exception ->
                    Log.e("HomeFragment", "Failed to get image URL", exception)
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("HomeFragment", "Failed to list images", exception)
        }
    }

    private fun loadLoaiPhongTro() {
        val database = FirebaseDatabase.getInstance().reference.child("LoaiPhongTro")
        val loaiPhongTroList = mutableListOf<LoaiPhongTro>()

        // Lấy tất cả LoaiPhongTro từ Firebase Realtime Database
        database.get().addOnSuccessListener { snapshot ->
            snapshot.children.forEach { childSnapshot ->
                val loaiPhongTro = childSnapshot.getValue(LoaiPhongTro::class.java)
                loaiPhongTro?.let {
                    loaiPhongTroList.add(it)
                    loaiPhongTroCodes.add(it.id)  // Thêm mã loại vào danh sách
                }
            }
            // Sau khi tải xong, setup các tab động
            setupTabs(loaiPhongTroList)
        }.addOnFailureListener { exception ->
            Log.e("HomeFragment", "Failed to fetch LoaiPhongTro", exception)
        }
    }

    private fun setupTabs(loaiPhongTroList: List<LoaiPhongTro>) {
        val tabLayout: TabLayout = binding.tabLayoutHome
        val viewPager: ViewPager2 = binding.viewPagerHome
        val adapter = ViewPagerHomeAdapter(this)
        viewPager.adapter = adapter

        // Kết nối TabLayout với ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // Thiết lập tên tab từ danh sách LoaiPhongTro
            if (position < loaiPhongTroList.size) {
                tab.text = loaiPhongTroList[position].tenLoaiPhong
            }
        }.attach()

        // Lắng nghe sự kiện chọn tab
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    val selectedPosition = it.position
                    if (selectedPosition < loaiPhongTroCodes.size) {
                        // Truyền mã loại phòng thay vì tên loại
                        onTabSelectedListener.onTabSelected(loaiPhongTroCodes[selectedPosition])
                        Log.d("HomeFragment", "Selected tab: ${loaiPhongTroCodes[selectedPosition]}")
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
}

// Interface để truyền sự kiện chọn tab đến Activity cha
interface OnTabSelectedListener {
    fun onTabSelected(maLoaiPhongTro: String)
}
