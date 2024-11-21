package com.ph32395.staynow.TienNghi

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
                    val maTienNghi = document.getString("Ma_tiennghi") ?: ""
                    val tenTienNghi = document.getString("Ten_tiennghi") ?: ""
                    val iconTienNghi = document.getString("Icon_tiennghi") ?: ""
                    val status = document.getBoolean("Status") ?: false

                    // Kiểm tra nếu Status là true thì mới thêm vào danh sách
                    if (status) {
                        tienNghiList.add(
                            TienNghi(
                                Ma_tiennghi = maTienNghi,
                                Ten_tiennghi = tenTienNghi,
                                Icon_tiennghi = iconTienNghi,
                                Status = status
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
