package com.ph32395.staynow_datn.DichVu

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class DichVuViewModel : ViewModel() {
    private val listDichVu = MutableLiveData<List<DichVu>>()

    init {
        // Lấy dữ liệu từ Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("DichVu")
            .get()
            .addOnSuccessListener { result ->
                val dichvuList = mutableListOf<DichVu>()
                for (document in result) {
                    // Lấy các giá trị từ Firestore
                    val maDichvu = document.id
                    val tenDichvu = document.getString("Ten_dichvu") ?: ""
                    val iconDichvu = document.getString("Icon_dichvu") ?: ""

                    // Lấy Don_vi dưới dạng mảng
                    val donViList = document.get("Don_vi") as? List<String> ?: emptyList()
                    val status = document.getBoolean("Status") ?: false

                    // Kiểm tra nếu Status là true thì mới thêm vào danh sách
                    if (status) {
                        dichvuList.add(
                            DichVu(
                                Ma_dichvu = maDichvu,
                                Ten_dichvu = tenDichvu,
                                Icon_dichvu = iconDichvu,
                                Don_vi = donViList,  // Lưu mảng Don_vi vào đối tượng DichVu
                                Status = status
                            )
                        )
                    }
                }
                // Cập nhật dữ liệu vào LiveData
                listDichVu.value = dichvuList
            }
            .addOnFailureListener { exception ->
                // Xử lý lỗi nếu có
                listDichVu.value = emptyList()
            }
    }

    fun getListDichVu(): LiveData<List<DichVu>> = listDichVu
}
