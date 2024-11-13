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
import com.ph32395.staynow.Model.PhongTroModel

class HomeViewModel : ViewModel() {
    private val _selectedLoaiPhongTro = MutableLiveData<String>()
    val selectedLoaiPhongTro: LiveData<String> get() = _selectedLoaiPhongTro

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _roomList = MutableLiveData<List<PhongTroModel>>()
    val roomList: LiveData<List<PhongTroModel>> get() = _roomList

    private val _loaiPhongTroList = MutableLiveData<List<LoaiPhongTro>>()
    val loaiPhongTroList: LiveData<List<LoaiPhongTro>> get() = _loaiPhongTroList

    private val _imageList = MutableLiveData<List<SlideModel>>()
    val imageList: LiveData<List<SlideModel>> get() = _imageList

    fun selectLoaiPhongTro(id_loaiphong: String) {
        _selectedLoaiPhongTro.value = id_loaiphong
    }

    fun loadLoaiPhongTro() {
        firestore.collection("LoaiPhong")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { it.toObject(LoaiPhongTro::class.java) }
                _loaiPhongTroList.value = list
            }
            .addOnFailureListener { exception ->
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
        val roomsRef = firestore.collection("PhongTro")

        val query = if (maloaiPhongTro == "0") {
            roomsRef
        } else {
            roomsRef.whereEqualTo("maLoaiPhongTro", maloaiPhongTro)
        }

        query.get()
            .addOnSuccessListener { snapshot ->
                handleRoomList(snapshot)
            }
            .addOnFailureListener { exception ->
                Log.e("HomeViewModel", "Error getting documents: ", exception)
            }
    }

    private fun handleRoomList(snapshot: QuerySnapshot) {
        val roomList = mutableListOf<PhongTroModel>()
        for (document in snapshot.documents) {
            val room = document.toObject(PhongTroModel::class.java)
            room?.let { roomList.add(it) }
        }
        _roomList.value = roomList // Cập nhật LiveData
    }
}
