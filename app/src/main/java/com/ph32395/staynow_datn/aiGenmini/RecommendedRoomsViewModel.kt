package com.ph32395.staynow_datn.aiGenmini

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow_datn.Model.PhongTroModel

class RecommendedRoomsViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    // LiveData cho danh sách phòng
    private val _recommendedRooms = MutableLiveData<List<Pair<String, PhongTroModel>>>()
    val recommendedRooms: LiveData<List<Pair<String, PhongTroModel>>> = _recommendedRooms

    // LiveData cho trạng thái loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData cho thông báo lỗi
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchRoomDetails(roomIds: List<String>) {
        _isLoading.value = true
        val rooms = mutableListOf<Pair<String, PhongTroModel>>()

        roomIds.forEach { roomId ->
            firestore.collection("PhongTro")
                .document(roomId)
                .get()
                .addOnSuccessListener { document ->
                    document.toObject(PhongTroModel::class.java)?.let { room ->
                        rooms.add(roomId to room)
                        // Kiểm tra nếu đã lấy đủ số phòng
                        if (rooms.size == roomIds.size) {
                            _recommendedRooms.value = rooms.sortedBy { it.second.tenPhongTro }
                            _isLoading.value = false
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    _error.value = "Không thể tải thông tin phòng: ${exception.message}"
                    _isLoading.value = false
                }
        }
    }

    fun clearRooms() {
        _recommendedRooms.value = emptyList()
    }
}