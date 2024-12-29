package com.ph32395.staynow_datn.PhongTroDaXem

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ph32395.staynow_datn.Model.PhongTroModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class PhongTroDaXemViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    val roomHistoryLiveDate = MutableLiveData<List<PhongTroModel>>()
    val isLoading = MutableLiveData<Boolean>() //Trang thai tai du lieu


    private val viewModelScope = CoroutineScope(Dispatchers.Main)


    fun fetchRoomHistory(userId: String) {
        viewModelScope.launch {
            try {
                isLoading.postValue(true) //Bat dau tai du lieu
                // Lắng nghe thay đổi dữ liệu Firestore theo thời gian thực
                firestore.collection("PhongTroDaXem")
                    .whereEqualTo("idNguoiDung", userId)
                    .orderBy("thoiGianXem", Query.Direction.DESCENDING)
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
                                    idPhongTro = doc.getString("idPhongTro") ?: "",
                                    thoiGianXem = doc.getLong("thoiGianXem") ?: 0L
                                )
                            }

                            // Lấy chi tiết phòng sau khi đã có danh sách lịch sử
                            fetchRoomDetails(roomHistoryList)
                        } else {
                            Log.d("Firestore", "Không có dữ liệu lịch sử phòng.")
                            roomHistoryLiveDate.postValue(emptyList()) //Khong cao du lieu
                            isLoading.postValue(false) //Ket thuc tai
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
        viewModelScope.launch {
            try {
                val roomList = mutableListOf<PhongTroModel>()
                val tasks = roomHistory.map { room ->
                    async(Dispatchers.IO) {
                        val doc = firestore.collection("PhongTro")
                            .document(room.idPhongTro)
                            .get()
                            .await()

                        PhongTroModel(
                            maPhongTro = doc.id,
                            tenPhongTro = doc.getString("tenPhongTro") ?: "",
                            diaChi = doc.getString("diaChi") ?: "",
                            giaPhong = doc.getDouble("giaPhong") ?: 0.0,
                            dienTich = null,
                            imageUrls = (doc.get("imageUrls") as? ArrayList<String>) ?: ArrayList(),
                            trangThai = false,
                            thoiGianXem = room.thoiGianXem
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