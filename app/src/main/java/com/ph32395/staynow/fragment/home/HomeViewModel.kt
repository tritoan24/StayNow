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
        val firestore = FirebaseFirestore.getInstance()
        val loaiPhongRef = firestore.collection("LoaiPhong")

        // Truy vấn LoaiPhong để lấy tên loại phòng dựa trên mã loại phòng
        val loaiPhongQuery = loaiPhongRef.whereEqualTo("Ma_loaiphong", maloaiPhongTro)

        loaiPhongQuery.get()
            .addOnSuccessListener { loaiPhongSnapshot ->
                if (!loaiPhongSnapshot.isEmpty) {
                    // Lấy tên loại phòng từ tài liệu LoaiPhong
                    val tenLoaiPhong =
                        loaiPhongSnapshot.documents.first().getString("Ten_loaiphong")

                    // Truy vấn PhongTro dựa trên tên loại phòng
                    val roomsRef = firestore.collection("PhongTro")
                    val query = if (tenLoaiPhong == "Tất cả") {
                        roomsRef
                    } else {
                        roomsRef.whereEqualTo("Ma_loaiphong", maloaiPhongTro)
                    }

                    // Thực hiện truy vấn PhongTro
                    query.get()
                        .addOnSuccessListener { snapshot ->
                            handleRoomList(snapshot)  // Xử lý danh sách phòng trọ
                        }
                        .addOnFailureListener { exception ->
                            Log.e("HomeViewModel", "Error getting rooms: ", exception)
                        }
                } else {
                    Log.d("HomeViewModel", "No matching LoaiPhong found")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("HomeViewModel", "Error getting LoaiPhong: ", exception)
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

    private val _chiTietThongTinList = MutableLiveData<List<ChiTietThongTin>>()
    val chiTietThongTinList: LiveData<List<ChiTietThongTin>> get() = _chiTietThongTinList

    fun fetchChiTietThongTin(maPhongTro: String) {
        firestore.collection("ChiTietThongTin")
            .whereEqualTo("ma_phongtro", maPhongTro) // Điều kiện 1: trùng mã phòng
            .whereEqualTo("ten_thongtin", "Diện tích") // Điều kiện 2: tên thông tin là "diện tích"
            .get()
            .addOnSuccessListener { documents ->
                val list = mutableListOf<ChiTietThongTin>()
                for (document in documents) {
                    // Chuyển mỗi document thành đối tượng ChiTietThongTin
                    val chiTiet = document.toObject(ChiTietThongTin::class.java)
                    list.add(chiTiet)
                    Log.d("HomeViewModel", "ChiTietThongTin: $chiTiet")
                }
                // Cập nhật LiveData với các kết quả phù hợp
                _chiTietThongTinList.postValue(list)
            }
            .addOnFailureListener { exception ->
                Log.e("HomeViewModel", "Error fetching ChiTietThongTin", exception)
                // Xử lý lỗi nếu cần
            }
    }

}
