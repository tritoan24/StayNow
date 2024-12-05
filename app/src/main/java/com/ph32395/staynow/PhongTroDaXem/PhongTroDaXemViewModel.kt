package com.ph32395.staynow.PhongTroDaXem

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.ph32395.staynow.Model.ChiTietThongTin
import com.ph32395.staynow.Model.PhongTroModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class PhongTroDaXemViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    val roomHistoryLiveDate = MutableLiveData<List<PhongTroModel>>()
    val isLoading = MutableLiveData<Boolean>() //Trang thai tai du lieu


    private val viewModelScope = CoroutineScope(Dispatchers.Main)


    fun fetchRoomHistory(userId: String) {
        viewModelScope.launch {
            try {
                // Lắng nghe thay đổi dữ liệu Firestore theo thời gian thực
                firestore.collection("PhongTroDaXem")
                    .whereEqualTo("Id_nguoidung", userId)
                    .orderBy("Thoi_gianxem", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, exception ->
                        if (exception != null) {
                            Log.e("Firestore", "Lỗi khi lắng nghe thay đổi", exception)
                            isLoading.postValue(false) //Ket thuc tai khi trang thai loi
                            return@addSnapshotListener
                        }

                        if (snapshot != null && !snapshot.isEmpty) {
                            // Chuyển đổi dữ liệu Firestore thành danh sách PhongTroDaXemModel
                            val roomHistoryList = snapshot.documents.map { doc ->
                                PhongTroDaXemModel(
                                    Id_phongtro = doc.getString("Id_phongtro") ?: "",
                                    Thoi_gianxem = doc.getLong("Thoi_gianxem") ?: 0L
                                )
                            }

                            // Lấy chi tiết phòng sau khi đã có danh sách lịch sử
                            fetchRoomDetails(roomHistoryList)
                        } else {
                            Log.d("Firestore", "Không có dữ liệu lịch sử phòng.")
                        }
                    }

            } catch (e: Exception) {
                Log.e("Firestore", "Lỗi khi lấy hoặc lắng nghe dữ liệu PhongTroDaXem", e)
                isLoading.postValue(false) //Ket thuc tai khi trang thai loi
            }
        }
    }



    private fun fetchRoomDetails(roomHistory: List<PhongTroDaXemModel>) {
        if (roomHistory.isEmpty()) {
            roomHistoryLiveDate.postValue(emptyList())
            isLoading.postValue(false) //Ket thuc tai khi trang thai loi
            return
        }

//        viewModelScope.launch {
//            val roomList = mutableListOf<PhongTroModel>()
//            val tasks = roomHistory.map { room ->
//                async(Dispatchers.IO) {
//                    val doc = firestore.collection("PhongTro")
//                        .document(room.Id_phongtro)
//                        .get()
//                        .await()
//
//                    PhongTroModel(
//                        Ma_phongtro = doc.id,
//                        Ten_phongtro = doc.getString("Ten_phongtro") ?: "",
//                        Dia_chi = doc.getString("Dia_chi") ?: "",
//                        Gia_phong = doc.getDouble("Gia_phong") ?: 0.0,
//                        Dien_tich = null,
//                        imageUrls = (doc.get("imageUrls") as? ArrayList<String>) ?: ArrayList(),
//                        Trang_thai = false,
//                        Thoi_gianxem = room.Thoi_gianxem
//                    )
//                }
//            }
//
//            // Chờ tất cả các tác vụ async hoàn thành
//            val rooms = tasks.awaitAll()
//            roomList.addAll(rooms)
//            roomHistoryLiveDate.postValue(roomList)
//        }
        viewModelScope.launch {
            try {
                val roomList = mutableListOf<PhongTroModel>()
                val tasks = roomHistory.map { room ->
                    async(Dispatchers.IO) {
                        val doc = firestore.collection("PhongTro")
                            .document(room.Id_phongtro)
                            .get()
                            .await()

                        PhongTroModel(
                            Ma_phongtro = doc.id,
                            Ten_phongtro = doc.getString("Ten_phongtro") ?: "",
                            Dia_chi = doc.getString("Dia_chi") ?: "",
                            Gia_phong = doc.getDouble("Gia_phong") ?: 0.0,
                            Dien_tich = null,
                            imageUrls = (doc.get("imageUrls") as? ArrayList<String>) ?: ArrayList(),
                            Trang_thai = false,
                            Thoi_gianxem = room.Thoi_gianxem
                        )
                    }
                }

                val rooms = tasks.awaitAll()
                roomList.addAll(rooms)
                roomHistoryLiveDate.postValue(roomList)
            } catch (e: Exception) {
                Log.e("Firestore", "Lỗi khi lấy chi tiết phòng", e)
            } finally {
                isLoading.postValue(false) // Kết thúc tải
            }
        }
    }

}