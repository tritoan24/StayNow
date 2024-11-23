package com.ph32395.staynow.fragment.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denzcoskun.imageslider.models.SlideModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.ph32395.staynow.Model.ChiTietThongTin
import com.ph32395.staynow.Model.LoaiPhongTro
import com.ph32395.staynow.Model.PhongTroModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {
    private val _selectedLoaiPhongTro = MutableLiveData<String>()
    val selectedLoaiPhongTro: LiveData<String> get() = _selectedLoaiPhongTro

    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _roomList = MutableLiveData<List<Pair<String, PhongTroModel>>>()
    val roomList: LiveData<List<Pair<String, PhongTroModel>>> get() = _roomList
    private val cachedRooms = mutableMapOf<String, List<Pair<String, PhongTroModel>>>()

    private val _loaiPhongTroList = MutableLiveData<List<LoaiPhongTro>>()
    val loaiPhongTroList: LiveData<List<LoaiPhongTro>> get() = _loaiPhongTroList

    private val _imageList = MutableLiveData<List<SlideModel>>()
    val imageList: LiveData<List<SlideModel>> get() = _imageList

    fun selectLoaiPhongTro(idLoaiPhong: String) {
        _selectedLoaiPhongTro.value = idLoaiPhong
    }

    // Hàm để cập nhật số lượt xem phòng trong Firestore
    fun incrementRoomViewCount(roomId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val roomRef = firestore.collection("PhongTro").document(roomId)
                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(roomRef)
                    val currentViewCount = snapshot.getLong("So_luotxemphong") ?: 0
                    transaction.update(roomRef, "So_luotxemphong", currentViewCount + 1)
                }.await()
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Lỗi tăng số lượt xem: ${e.message}")
            }
        }
    }


    fun loadLoaiPhongTro() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection("LoaiPhong").get().await()
                val list = snapshot.documents.mapNotNull { it.toObject(LoaiPhongTro::class.java) }
                withContext(Dispatchers.Main) {
                    _loaiPhongTroList.value = list
                }
            } catch (e: Exception) {
                // Handle error
                Log.e("HomeViewModel", "Error loading LoaiPhongTro: ${e.message}", e)
            }
        }
    }

    fun loadImagesFromFirebase() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val imageList = ArrayList<SlideModel>()
                val storageRef = storage.reference.child("banners")
                val listResult = storageRef.listAll().await()

                for (item in listResult.items) {
                    val uri = item.downloadUrl.await()
                    imageList.add(SlideModel(uri.toString()))
                }

                withContext(Dispatchers.Main) {
                    _imageList.value = imageList
                }
            } catch (e: Exception) {
                // Handle error
                Log.e("HomeViewModel", "Error loading images: ${e.message}", e)
            }
        }
    }

    fun updateRoomList(maloaiPhongTro: String) {
        // Nếu dữ liệu đã có trong cache, sử dụng luôn
        cachedRooms[maloaiPhongTro]?.let {
            _roomList.postValue(it)
            return
        }

        // Bọc logic trong coroutine với Dispatchers.IO
        viewModelScope.launch(Dispatchers.IO) {
            firestore.collection("LoaiPhong")
                .whereEqualTo("Ma_loaiphong", maloaiPhongTro)
                .addSnapshotListener { loaiPhongSnapshot, exception ->

                    if (exception != null) {
                        Log.e("HomeViewModel", "Error listening for changes: ", exception)
                        return@addSnapshotListener
                    }

                    val tenLoaiPhong =
                        loaiPhongSnapshot?.documents?.firstOrNull()?.getString("Ten_loaiphong")
                    if (tenLoaiPhong != null) {
                        val roomsRef = firestore.collection("PhongTro")
                        val query = if (tenLoaiPhong == "Tất cả") roomsRef.whereEqualTo(
                            "Trang_thaiduyet",
                            "DaDuyet"
                        ) else roomsRef.whereEqualTo(
                            "Ma_loaiphong", maloaiPhongTro
                        ).whereEqualTo(
                            "Trang_thaiduyet",
                            "DaDuyet"
                        )
                        query.addSnapshotListener { snapshot, exception ->

                            if (exception != null) {
                                Log.e("HomeViewModel", "Error fetching rooms: ", exception)
                                return@addSnapshotListener
                            }

                            if (snapshot != null) {
                                handleRoomList(snapshot) // Giữ lại hàm này như yêu cầu
                            }

                            val rooms = snapshot?.documents?.map { doc ->
                                Pair(doc.id, doc.toObject(PhongTroModel::class.java)!!)
                            } ?: emptyList()

                            fetchChiTietThongTinForRoomList(rooms, maloaiPhongTro)
                            cachedRooms[maloaiPhongTro] = rooms
                            _roomList.postValue(rooms)
                        }
                    }
                }
        }
    }

    private fun handleRoomList(snapshot: QuerySnapshot) {
        // Bọc vào coroutine để đảm bảo thực thi trên IO thread
        viewModelScope.launch(Dispatchers.IO) {
            val roomList = mutableListOf<Pair<String, PhongTroModel>>()

            for (document in snapshot.documents) {
                val id = document.id
                val room = document.toObject(PhongTroModel::class.java)
                room?.let {
                    roomList.add(Pair(id, it))
                }
            }

            // Cập nhật LiveData phải thực hiện trên main thread
            withContext(Dispatchers.Main) {
                _roomList.value = roomList // Cập nhật LiveData
            }
        }
    }
    //    lay du lieu danh sach phong tro su dung Kotlin Coroutines

    fun updateRoomListWithCoroutines() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val roomRef = firestore.collection("PhongTro")
                val querySnapshot = roomRef.get().await() // Truy van  toan bo danh sach phong tro

//                Chuyen doi du lieu Firebase thanh danh sach Rm
                val rooms = querySnapshot.documents.mapNotNull { doc ->
                    val roomModel = doc.toObject(PhongTroModel::class.java)
                    roomModel?.let { Pair(doc.id, it) } // Chỉ thêm khi không null
                }

//                cap nhat Livedata tren MainThread
                withContext(Dispatchers.Main) {
                    if (rooms.isNotEmpty()) {
                        _roomList.value = rooms
//                        Goi phuong thuc lay dien tich moi phòng
                        fetchDienTichForRoomList(rooms)
                    } else {
                        Log.w("HomeViewModel", "Danh sách phòng trọ rỗng.")
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching room: ${e.message}")
            }
        }
    }

    //    lay dien tich trong Home Chu Tro
    private fun fetchDienTichForRoomList(
        roomList: List<Pair<String, PhongTroModel>>
    ) {
        clearRoomCache()
        // Lưu trữ danh sách đã cập nhật
        val updatedRoomList = mutableListOf<Pair<String, PhongTroModel>>()

        // Bọc toàn bộ logic vào coroutine chạy trên Dispatchers.IO
        viewModelScope.launch(Dispatchers.IO) {
            roomList.forEach { (roomId, room) ->
                firestore.collection("ChiTietThongTin")
                    .whereEqualTo("ma_phongtro", roomId)
                    .whereEqualTo("ten_thongtin", "Diện tích")
                    .addSnapshotListener { documents, exception ->
                        if (exception != null) {
                            Log.e(
                                "HomeViewModel",
                                "Error listening for ChiTietThongTin for roomId $roomId",
                                exception
                            )
                            return@addSnapshotListener
                        }

                        // Nếu có thay đổi thông tin diện tích, cập nhật lại vào phòng trọ
                        val chiTiet = documents?.firstOrNull()?.toObject(ChiTietThongTin::class.java)
                        chiTiet?.let {
                            room.Dien_tich = it.so_luong_donvi
                        }

                        // Cập nhật lại thông tin trong danh sách đã thay đổi
                        updatedRoomList.add(Pair(roomId, room))

                        // Nếu đã cập nhật diện tích cho tất cả phòng trọ, thực hiện cập nhật LiveData và cache
                        if (updatedRoomList.size == roomList.size) {
                            // Cập nhật lại LiveData với danh sách mới
                            // Chuyển về main thread để cập nhật LiveData
                            viewModelScope.launch(Dispatchers.Main) {
                                _roomList.postValue(updatedRoomList)

                                // Chỉ lưu vào cache sau khi LiveData đã được cập nhật
                                cachedRooms["someMaloaiPhongTro"] = updatedRoomList
                            }
                        }
                    }
            }
        }
    }


    //    lay dien tich trong Home Nguoi Thue
    private fun fetchChiTietThongTinForRoomList(
        roomList: List<Pair<String, PhongTroModel>>,
        maloaiPhongTro: String
    ) {
        clearRoomCache()
        // Lưu trữ danh sách đã cập nhật
        val updatedRoomList = mutableListOf<Pair<String, PhongTroModel>>()

        // Bọc toàn bộ logic vào coroutine chạy trên Dispatchers.IO
        viewModelScope.launch(Dispatchers.IO) {
            roomList.forEach { (roomId, room) ->
                firestore.collection("ChiTietThongTin")
                    .whereEqualTo("ma_phongtro", roomId)
                    .whereEqualTo("ten_thongtin", "Diện tích")
                    .addSnapshotListener { documents, exception ->
                        if (exception != null) {
                            Log.e(
                                "HomeViewModel",
                                "Error listening for ChiTietThongTin for roomId $roomId",
                                exception
                            )
                            return@addSnapshotListener
                        }

                        // Nếu có thay đổi thông tin diện tích, cập nhật lại vào phòng trọ
                        val chiTiet = documents?.firstOrNull()?.toObject(ChiTietThongTin::class.java)
                        chiTiet?.let {
                            room.Dien_tich = it.so_luong_donvi
                        }

                        // Cập nhật lại thông tin trong danh sách đã thay đổi
                        updatedRoomList.add(Pair(roomId, room))

                        // Nếu đã cập nhật diện tích cho tất cả phòng trọ, thực hiện cập nhật LiveData và cache
                        if (updatedRoomList.size == roomList.size) {
                            // Cập nhật lại LiveData với danh sách mới
                            // Chuyển về main thread để cập nhật LiveData
                            viewModelScope.launch(Dispatchers.Main) {
                                _roomList.postValue(updatedRoomList)

                                // Chỉ lưu vào cache sau khi LiveData đã được cập nhật
                                cachedRooms[maloaiPhongTro] = updatedRoomList
                            }
                        }
                    }
            }
        }
    }

    fun clearRoomCache() {
        cachedRooms.clear()  // Xóa cache trước khi tải lại dữ liệu
        Log.d("HomeViewModel", "Cache cleared")  // Để debug log khi cache bị xóa
    }

}
