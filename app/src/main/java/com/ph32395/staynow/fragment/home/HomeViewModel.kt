package com.ph32395.staynow.fragment.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.denzcoskun.imageslider.models.SlideModel
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
            .get()
            .addOnSuccessListener { loaiPhongSnapshot ->
                val tenLoaiPhong =
                    loaiPhongSnapshot.documents.firstOrNull()?.getString("Ten_loaiphong")
                if (tenLoaiPhong != null) {
                    val roomsRef = firestore.collection("PhongTro")
                    val query = if (tenLoaiPhong == "Tất cả") roomsRef else roomsRef.whereEqualTo(
                        "Ma_loaiphong",
                        maloaiPhongTro
                    )
                    query.get()
                        .addOnSuccessListener { snapshot ->
                            handleRoomList(snapshot)
                            val rooms = snapshot.documents.map { doc ->
                                Pair(doc.id, doc.toObject(PhongTroModel::class.java)!!)
                            }
                            // Lưu vào cache
                            cachedRooms[maloaiPhongTro] = rooms
                            _roomList.postValue(rooms)
                        }
                        .addOnFailureListener { exception ->
                            Log.e("HomeViewModel", "Error fetching rooms: ", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("HomeViewModel", "Error fetching LoaiPhong: ", exception)
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
        fetchChiTietThongTinForRoomList(roomList)
    }

    private fun fetchChiTietThongTinForRoomList(roomList: List<Pair<String, PhongTroModel>>) {
        val updatedRoomList = mutableListOf<Pair<String, PhongTroModel>>()

        roomList.forEach { (roomId, room) ->
            firestore.collection("ChiTietThongTin")
                .whereEqualTo("ma_phongtro", roomId)
                .whereEqualTo("ten_thongtin", "Diện tích")
                .get()
                .addOnSuccessListener { documents ->
                    val chiTiet = documents.firstOrNull()?.toObject(ChiTietThongTin::class.java)
                    chiTiet?.let {
                        room.Dien_tich = it.so_luong_donvi // Cập nhật diện tích cho phòng
                    }
                    updatedRoomList.add(Pair(roomId, room))

                    // Nếu đã xử lý xong tất cả phòng trọ, cập nhật LiveData
                    if (updatedRoomList.size == roomList.size) {
                        _roomList.value = updatedRoomList
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(
                        "HomeViewModel",
                        "Error fetching ChiTietThongTin for roomId $roomId",
                        exception
                    )
                }
        }
    }

}
