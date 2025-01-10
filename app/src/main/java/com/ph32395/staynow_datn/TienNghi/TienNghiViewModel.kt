package com.ph32395.staynow_datn.TienNghi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class TienNghiViewModel : ViewModel() {

    private val listTienNghi = MutableLiveData<List<TienNghi>>()

    init {
        // Lấy dữ liệu từ Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("TienNghi")
            .get()
            .addOnSuccessListener { result ->
                val tienNghiList = mutableListOf<TienNghi>()
                for (document in result) {
                    // Lấy các giá trị từ Firestore
                    val maTienNghi = document.getString("maTienNghi") ?: ""
                    val tenTienNghi = document.getString("tenTienNghi") ?: ""
                    val iconTienNghi = document.getString("iconTienNghi") ?: ""
                    val status = document.getBoolean("trangThai") ?: false

                    // Kiểm tra nếu Status là true thì mới thêm vào danh sách
                    if (status) {
                        tienNghiList.add(
                            TienNghi(
                                maTienNghi = maTienNghi,
                                tenTienNghi = tenTienNghi,
                                iconTienNghi = iconTienNghi,
                                trangThai = status
                            )
                        )
                    }
                }
                // Cập nhật dữ liệu vào LiveData
                listTienNghi.value = tienNghiList
            }
            .addOnFailureListener { exception ->
                // Xử lý lỗi nếu có
                listTienNghi.value = emptyList()
            }
    }


    fun getListTienNghi(): LiveData<List<TienNghi>> = listTienNghi
}
