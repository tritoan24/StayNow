package com.ph32395.staynow_datn.LoaiPhong

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class LoaiPhongViewModel : ViewModel() {
    private val listLoaiPhong = MutableLiveData<List<LoaiPhong>>()

    init {
        // Lấy dữ liệu từ Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("LoaiPhong")
            .get()
            .addOnSuccessListener { result ->
                val loaiPhongList = mutableListOf<LoaiPhong>()
                for (document in result){
                    // Lấy các giá trị từ Firestore
                    val maLoaiPhong = document.getString("maLoaiPhong") ?: ""
                    val tenLoaiPhong = document.getString("tenLoaiPhong") ?: ""
                    val status = document.getBoolean("trangThai") ?: false

                    // Kiểm tra nếu Status là true thì mới thêm vào danh sách
                    if (status) {
                        loaiPhongList.add(
                            LoaiPhong(
                                maLoaiPhong = maLoaiPhong,
                                tenLoaiPhong = tenLoaiPhong,
                                trangThai = status
                            )
                        )
                    }
                    }
                    listLoaiPhong.value = loaiPhongList
                }
                    .addOnFailureListener { exception ->
                        // Xử lý lỗi nếu có
                        listLoaiPhong.value = emptyList()
                    }

            }
        fun getListLoaiPhong(): MutableLiveData<List<LoaiPhong>> = listLoaiPhong
    }
