package com.ph32395.staynow_datn.ThongTin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class ThongTinViewModel : ViewModel() {
    private val listThongTin= MutableLiveData<List<ThongTin>>()

    init {
        // Lấy dữ liệu từ Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("ThongTin")
            .get()
            .addOnSuccessListener { result ->
                val thongtinList = mutableListOf<ThongTin>()
                for (document in result) {
                    // Lấy các giá trị từ Firestore
                    val maThongTin = document.getString("maThongTin") ?: ""
                    val tenThongTin = document.getString("tenThongTin") ?: ""
                    val iconThongTin = document.getString("iconThongTin") ?: ""
                    val donVi = document.getString("donVi") ?: ""
                    val status = document.getBoolean("trangThai") ?: false

                    // Kiểm tra nếu Status là true thì mới thêm vào danh sách
                    if (status) {
                       thongtinList.add(
                            ThongTin(
                                maThongTin = maThongTin,
                                tenThongTin = tenThongTin,
                                iconThongTin = iconThongTin,
                                donVi = donVi,
                                trangThai = status
                            )
                       )
                    }
                }
                // Cập nhật dữ liệu vào LiveData
                listThongTin.value = thongtinList
            }
            .addOnFailureListener { exception ->
                // Xử lý lỗi nếu có
                listThongTin.value = emptyList()
            }
    }

    fun getListThongTin(): LiveData<List<ThongTin>> = listThongTin
}