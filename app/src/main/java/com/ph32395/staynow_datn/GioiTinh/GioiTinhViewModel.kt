package com.ph32395.staynow_datn.GioiTinh

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class GioiTinhViewModel : ViewModel() {

    private val listGioiTinh = MutableLiveData<List<GioiTinh>>()

    init {
        // Lấy dữ liệu từ Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("GioiTinh")
            .get()
            .addOnSuccessListener { result ->
                val listgioiTinh = mutableListOf<GioiTinh>()
                for (document in result){
                    // Lấy các giá trị từ Firestore
                    val maLoaiPhong = document.getString("Ma_gioitinh") ?: ""
                    val tenLoaiPhong = document.getString("Ten_gioitinh") ?: ""
                    val imgUrl = document.getString("ImgUrl_gioitinh") ?: ""
                    val status = document.getBoolean("Status") ?: false

                    // Kiểm tra nếu Status là true thì mới thêm vào danh sách
                    if (status) {
                        listgioiTinh.add(
                            GioiTinh (
                                maGioiTinh = maLoaiPhong,
                                tenGioiTinh = tenLoaiPhong,
                                anhGioiTinh = imgUrl,
                                status = status
                            )
                        )
                    }
                }
                listGioiTinh.value = listgioiTinh
            }
            .addOnFailureListener { exception ->
                // Xử lý lỗi nếu có
                listGioiTinh.value = emptyList()
            }

    }
    fun getListGioiTinh(): MutableLiveData<List<GioiTinh>> = listGioiTinh
}
