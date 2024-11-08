package com.ph32395.staynow.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ph32395.staynow.Model.PhongTroModel

class RoomDetailViewModel : ViewModel() {

    private val _room = MutableLiveData<PhongTroModel>()
    val room: LiveData<PhongTroModel> get() = _room

    // Sử dụng MutableLiveData để lưu danh sách ảnh
    private val _images = MutableLiveData<List<String>>()
    val images: LiveData<List<String>> get() = _images  // Định nghĩa LiveData cho hình ảnh

//    Ham khoi tao du lieu ban dau
    fun setInitialData(
        maPhongTro: String,
        tenPhongTro: String,
        giaThue: Double,
        diaChi: String,
        dienTich: Double,
        tang: Int,
        soNguoi: Int,
        tienCoc: Double,
        motaChiTiet: String,
        danhSachAnh: ArrayList<String>,
        gioiTinh: String,
        trangThai: String
    ) {
        _room.value = PhongTroModel(
            maPhongTro = maPhongTro,
            tenPhongTro = tenPhongTro,
            giaThue = giaThue,
            diaChi = diaChi,
            dienTich = dienTich,
            tang = tang,
            soNguoi = soNguoi,
            tienCoc = tienCoc,
            motaChiTiet = motaChiTiet,
            danhSachAnh = danhSachAnh,
            gioiTinh = gioiTinh,
            trangThai = trangThai
        )
    }

//    Lay du lieu tren firebase
    fun fetchRoomDetail(maPhongTro: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("PhongTro/$maPhongTro")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(PhongTroModel::class.java)?.let { room ->
                    _room.value = room
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RoomDetailViewModel", "Error fetch room data", error.toException())
            }
        })
    }

    // Hàm này dùng để cập nhật danh sách ảnh
    fun setImages(newImages: List<String>) {
        _images.value = newImages
    }

}