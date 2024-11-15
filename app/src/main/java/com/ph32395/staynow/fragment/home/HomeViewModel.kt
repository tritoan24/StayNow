package com.ph32395.staynow.fragment.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.denzcoskun.imageslider.models.SlideModel
import com.google.firebase.firestore.FieldValue
import com.ph32395.staynow.Model.LoaiPhongTro
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.ph32395.staynow.Model.ChiTietThongTin
import com.ph32395.staynow.Model.PhongTroModel

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
        val roomRef = firestore.collection("PhongTro").document(roomId)

        // Tăng giá trị So_luotxemphong trong Firestore
        roomRef.update("So_luotxemphong", FieldValue.increment(1))
            .addOnSuccessListener {
                Log.d("HomeViewModel", "Successfully incremented So_luotxemphong")
                // Sau khi cập nhật thành công, lấy lại danh sách phòng để làm mới UI
                updateRoomList(roomId)
            }
            .addOnFailureListener { exception ->
                Log.e("HomeViewModel", "Error updating So_luotxemphong: ", exception)
            }
    }

    fun loadLoaiPhongTro() {
        firestore.collection("LoaiPhong")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { it.toObject(LoaiPhongTro::class.java) }
                _loaiPhongTroList.value = list
            }
            .addOnFailureListener {
                // Handle error
            }
    }

    fun loadImagesFromFirebase() {
        val imageList = ArrayList<SlideModel>()
        val storageRef = storage.reference.child("banners")

        storageRef.listAll().addOnSuccessListener { listResult ->
            listResult.items.forEach { item ->
                item.downloadUrl.addOnSuccessListener { uri ->
                    imageList.add(SlideModel(uri.toString()))
                    if (imageList.size == listResult.items.size) {
                        _imageList.value = imageList
                    }
                }
            }
        }
    }

    fun updateRoomList(maloaiPhongTro: String) {
        cachedRooms[maloaiPhongTro]?.let {
            _roomList.postValue(it)
            return
        }
        firestore.collection("LoaiPhong")
            .whereEqualTo("Ma_loaiphong", maloaiPhongTro)
            .addSnapshotListener { loaiPhongSnapshot, exception ->
                if (exception != null) {
                    Log.e("HomeViewModel", "Error listening for changes: ", exception)
                    return@addSnapshotListener
                }

                // Xử lý khi có thay đổi dữ liệu
                val tenLoaiPhong =
                    loaiPhongSnapshot?.documents?.firstOrNull()?.getString("Ten_loaiphong")
                if (tenLoaiPhong != null) {
                    val roomsRef = firestore.collection("PhongTro")
                    val query = if (tenLoaiPhong == "Tất cả") roomsRef else roomsRef.whereEqualTo(
                        "Ma_loaiphong", maloaiPhongTro
                    )
                    query.addSnapshotListener { snapshot, exception ->
                        if (exception != null) {
                            Log.e("HomeViewModel", "Error fetching rooms: ", exception)
                            return@addSnapshotListener
                        }

                        if (snapshot != null) {
                            handleRoomList(snapshot)
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

    private fun handleRoomList(snapshot: QuerySnapshot) {
        val roomList = mutableListOf<Pair<String, PhongTroModel>>()
        for (document in snapshot.documents) {
            val id = document.id
            val room = document.toObject(PhongTroModel::class.java)
            room?.let {
                roomList.add(Pair(id, it))
            }
        }
        _roomList.value = roomList // Cập nhật LiveData
    }

    private fun fetchChiTietThongTinForRoomList(
        roomList: List<Pair<String, PhongTroModel>>,
        maloaiPhongTro: String
    ) {
        clearRoomCache()
        // Lưu trữ danh sách đã cập nhật
        val updatedRoomList = mutableListOf<Pair<String, PhongTroModel>>()

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
                        _roomList.postValue(updatedRoomList)

                        // Chỉ lưu vào cache sau khi LiveData đã được cập nhật
                        cachedRooms[maloaiPhongTro] = updatedRoomList

                    }
                }
        }
    }

    fun clearRoomCache() {
        cachedRooms.clear()  // Xóa cache trước khi tải lại dữ liệu
        Log.d("HomeViewModel", "Cache cleared")  // Để debug log khi cache bị xóa
    }

}
