package com.ph32395.staynow.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.ph32395.staynow.Model.ChiTietThongTinModel
import com.ph32395.staynow.Model.NoiThatModel
import com.ph32395.staynow.Model.PhiDichVuModel
import com.ph32395.staynow.Model.PhongTroModel
import com.ph32395.staynow.Model.TienNghiModel

class RoomDetailViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val realtimeDb = FirebaseDatabase.getInstance().reference

    private val _room = MutableLiveData<PhongTroModel>()
    val room: LiveData<PhongTroModel> get() = _room

    private val _genderInfo = MutableLiveData<Pair<String, String>>()
    val genderInfo: LiveData<Pair<String, String>> get() = _genderInfo

    private val _roomType = MutableLiveData<String>()
    val roomType: LiveData<String> get() = _roomType

    private val _userInfo = MutableLiveData<Pair<String, String>>()
    val userInfo: LiveData<Pair<String, String>> get() = _userInfo

    private val _roomStatus = MutableLiveData<String>()
    val roomStatus: LiveData<String> get() = _roomStatus

    private val _chiTietList = MutableLiveData<List<ChiTietThongTinModel>>()
    val chiTietList: LiveData<List<ChiTietThongTinModel>> get() = _chiTietList

    private val _phiDichVuList = MutableLiveData<List<PhiDichVuModel>>()
    val phiDichVuList: LiveData<List<PhiDichVuModel>> get() = _phiDichVuList

    private val _noiThatList = MutableLiveData<List<NoiThatModel>>()
    val noiThatList: LiveData<List<NoiThatModel>> get() = _noiThatList

    private val _tienNghiList = MutableLiveData<List<TienNghiModel>>()
    val tienNghiList: LiveData<List<TienNghiModel>> get() = _tienNghiList

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData cho thông tin tiền cọc
    private val _tienCocInfo = MutableLiveData<ChiTietThongTinModel?>()
    val tienCocInfo: LiveData<ChiTietThongTinModel?> = _tienCocInfo

//    lay danh sach tien nghi
    fun fetchTienNghi(maPhongTro: String) {
        db.collection("PhongTroTienNghi")
            .whereEqualTo("ma_phongtro", maPhongTro)
            .get()
            .addOnSuccessListener { phongTroTienNghiDocs ->
                val tienNghiIds = phongTroTienNghiDocs.map { it["ma_tiennghi"] as String }
                if (tienNghiIds.isNotEmpty()) {
                    db.collection("TienNghi")
                        .whereIn(FieldPath.documentId(), tienNghiIds)
                        .get()
                        .addOnSuccessListener { tienNghiDocs ->
                            val tienNghiList = tienNghiDocs.mapNotNull { it.toObject(TienNghiModel::class.java) }
                            _tienNghiList.value = tienNghiList
                        }
                        .addOnFailureListener { exception ->
                            Log.e("RoomDetailModel", "Lỗi khi lấy dữ liệu tien nghi", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("RoomDetailModel", "Lỗi khi lấy dữ liệu PhongTroTienNghi", exception)
            }
    }

//    Lay danh sach thong tin noi that
    fun fetchNoiThat(maPhongTro: String) {
        db.collection("PhongTroNoiThat")
            .whereEqualTo("ma_phongtro", maPhongTro)
            .get()
            .addOnSuccessListener { phongTroNoiThatDocs ->
                val noiThatIds = phongTroNoiThatDocs.map { it["ma_noithat"] as String }
                if (noiThatIds.isNotEmpty()) {
                    db.collection("NoiThat")
                        .whereIn(FieldPath.documentId(), noiThatIds)
                        .get()
                        .addOnSuccessListener { noiThatDocs ->
                            val noiThatList = noiThatDocs.mapNotNull { it.toObject(NoiThatModel::class.java) }
                            _noiThatList.value = noiThatList
                        }
                        .addOnFailureListener { exception ->
                            Log.e("RoomDetailModel", "Lỗi khi lấy dữ liệu nội thất", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("RoomDetailModel", "Lỗi khi lấy dữ liệu PhongTroNoiThat", exception)
            }
    }

//    Lay thong tin chi tiet
    fun fetchChiTietThongTin(maPhongTro: String) {
        db.collection("ChiTietThongTin")
            .whereEqualTo("ma_phongtro", maPhongTro)
            .get()
            .addOnSuccessListener { document ->
                val list = document.mapNotNull { it.toObject(ChiTietThongTinModel::class.java) }
                _chiTietList.value = list
                updateTienCocInfo()
            }
            .addOnFailureListener { exception ->
                Log.e("RoomDetailViewModel", "Lỗi khi lấy dữ liệu chi tiết thông tin", exception)
            }
    }

//    Lay thong tin phi dich vu
    fun fetchPhiDichVu(maPhongTro: String) {
        db.collection("PhiDichVu")
            .whereEqualTo("ma_phongtro", maPhongTro)
            .get()
            .addOnSuccessListener { document ->
                val list = document.mapNotNull { it.toObject(PhiDichVuModel::class.java) }
                _phiDichVuList.value = list
            }
            .addOnFailureListener { exception ->
                Log.e("RoomDetailViewModel", "Loi khi lay du lieu phi dich vu", exception)
            }
    }

//Lay thong tin chi tiet phong tro
    fun fetchRoomDetail(maPhongTro: String) {
        _isLoading.value = true //Bat dau tai
        val docRef = db.collection("PhongTro").document(maPhongTro)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    document.toObject(PhongTroModel::class.java)?.let { room ->
                        _room.value = room
                        Log.d("fetchRoomDetail", "room: $room")
                        fetchAdditionalInfo(room)
                    }
                } else {
                    Log.d("RoomDetailViewModel", "Không có tài liệu này")
                }
            }
            .addOnFailureListener {
                Log.d("RoomDetailViewModel", "Lỗi khi truy vấn dữ liệu phòng trọ", it)
            }
            .addOnCompleteListener {
                _isLoading.value = false //Ket thuc tai du lieu
            }
    }

    private fun fetchAdditionalInfo(room: PhongTroModel) {
//        Truy van thong tin gioi tinh tu Ma_gioitinh
        room.Ma_gioiTinh?.let { maGioiTinh ->
            db.collection("GioiTinh").document(maGioiTinh)
                .get()
                .addOnSuccessListener { document ->
                    document?.let {
                        val imgUrlGioiTinh = it.getString("ImgUrl_gioitinh") ?: ""
                        val tenGioiTinh = it.getString("Ten_gioitinh") ?: ""
                        _genderInfo.value = Pair(imgUrlGioiTinh, tenGioiTinh)
                    }
                }
        }

//        Truy van thong tin loai phong tro tu Ma_phongtro
        room.Ma_loaiphong?.let { maLoaiPhong ->
            db.collection("LoaiPhong").document(maLoaiPhong)
                .get()
                .addOnSuccessListener { document ->
                    document?.let {
                        _roomType.value = it.getString("Ten_loaiphong") ?: ""
                    }
                }
        }

//        Truy van thong tin nguoi dung tu Ma_nguoidung
        room.Ma_nguoidung.let { maChuTro ->
            realtimeDb.child("NguoiDung").child(maChuTro)
                .get()
                .addOnSuccessListener { dataSnapshot ->
                    dataSnapshot?.let {
                        val anhDaiDien = it.child("anh_daidien").value as? String ?: ""
                        val hoTen = it.child("ho_ten").value as? String ?: ""
                        _userInfo.value = Pair(anhDaiDien, hoTen)
                    }
                }
        }
    }
    // Hàm lấy thông tin tiền cọc từ danh sách chi tiết
    private fun updateTienCocInfo() {
        _tienCocInfo.value = _chiTietList.value?.find { it.ten_thongtin == "Tiền cọc" }
    }

    // Hàm public để lấy giá trị tiền cọc
    fun getTienCocValue(): Double {
        return _tienCocInfo.value?.so_luong_donvi?.toDouble() ?: 0.0
    }
}