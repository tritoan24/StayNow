package com.ph32395.staynow_datn.NoiThat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class NoiThatViewModel : ViewModel() {

    private val listNoiThat = MutableLiveData<List<NoiThat>>()

    init {
        // Lấy dữ liệu từ Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("NoiThat")
            .get()
            .addOnSuccessListener { result ->
                val noiThatList = mutableListOf<NoiThat>()
                for (document in result) {
                    // Lấy các giá trị từ Firestore
                    val maNoiThat = document.id
                    val tenNoiThat = document.getString("tenNoiThat") ?: ""
                    val iconNoiThat = document.getString("iconNoiThat") ?: ""
                    val status = document.getBoolean("trangThai") ?: false

                    // Kiểm tra nếu Status là true thì mới thêm vào danh sách
                    if (status) {
                        noiThatList.add(
                            NoiThat(
                                maNoiThat = maNoiThat,
                                tenNoiThat = tenNoiThat,
                                iconNoiThat = iconNoiThat,
                                trangThai = status
                            )
                        )
                    }
                }
                // Cập nhật dữ liệu vào LiveData
                listNoiThat.value = noiThatList
            }
            .addOnFailureListener { exception ->
                // Xử lý lỗi nếu có
                listNoiThat.value = emptyList()
            }
    }

    fun getListNoiThat(): LiveData<List<NoiThat>> = listNoiThat
}
