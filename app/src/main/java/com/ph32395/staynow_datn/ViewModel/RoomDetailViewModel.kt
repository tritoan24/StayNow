package com.ph32395.staynow_datn.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow_datn.Model.PhongTroModel
import com.ph32395.staynow_datn.NoiThat.NoiThat
import com.ph32395.staynow_datn.TaoPhongTro.ChiTietThongTin
import com.ph32395.staynow_datn.TaoPhongTro.PhiDichVu
import com.ph32395.staynow_datn.TienNghi.TienNghi

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

    // paste

    private val _userId = MutableLiveData<Pair<String, String>>()
    val userId: LiveData<Pair<String, String>> get() = _userId

    private val _roomStatus = MutableLiveData<String>()
    val roomStatus: LiveData<String> get() = _roomStatus

    private val _chiTietList = MutableLiveData<List<com.ph32395.staynow_datn.TaoPhongTro.ChiTietThongTin>>()
    val chiTietList: LiveData<List<com.ph32395.staynow_datn.TaoPhongTro.ChiTietThongTin>> get() = _chiTietList

    private val _phiDichVuList = MutableLiveData<List<PhiDichVu>>()
    val phiDichVuList: LiveData<List<PhiDichVu>> get() = _phiDichVuList

    private val _noiThatList = MutableLiveData<List<NoiThat>>()
    val noiThatList: LiveData<List<NoiThat>> get() = _noiThatList

    private val _tienNghiList = MutableLiveData<List<TienNghi>>()
    val tienNghiList: LiveData<List<TienNghi>> get() = _tienNghiList

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData cho thông tin tiền cọc
    private val _tienCocInfo = MutableLiveData<com.ph32395.staynow_datn.TaoPhongTro.ChiTietThongTin?>()
    val tienCocInfo: LiveData<com.ph32395.staynow_datn.TaoPhongTro.ChiTietThongTin?> = _tienCocInfo

//    lay danh sach tien nghi
    fun fetchTienNghi(maPhongTro: String) {
        db.collection("PhongTroTienNghi")
            .whereEqualTo("maPhongTro", maPhongTro)
            .get()
            .addOnSuccessListener { phongTroTienNghiDocs ->
                val tienNghiIds = phongTroTienNghiDocs.map { it["maTienNghi"] as String }
                if (tienNghiIds.isNotEmpty()) {
                    db.collection("TienNghi")
                        .whereIn(FieldPath.documentId(), tienNghiIds)
                        .get()
                        .addOnSuccessListener { tienNghiDocs ->
                            val tienNghiList = tienNghiDocs.mapNotNull { it.toObject(TienNghi::class.java) }
                            _tienNghiList.value = tienNghiList
                            Log.d("RoomDetailModel", "Tien nghi list: $tienNghiList")
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
            .whereEqualTo("maPhongTro", maPhongTro)
            .get()
            .addOnSuccessListener { phongTroNoiThatDocs ->
                val noiThatIds = phongTroNoiThatDocs.map { it["maNoiThat"] as String }
                if (noiThatIds.isNotEmpty()) {
                    db.collection("NoiThat")
                        .whereIn(FieldPath.documentId(), noiThatIds)
                        .get()
                        .addOnSuccessListener { noiThatDocs ->
                            val noiThatList = noiThatDocs.mapNotNull { it.toObject(NoiThat::class.java) }
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
            .whereEqualTo("maPhongTro", maPhongTro)
            .get()
            .addOnSuccessListener { document ->
                val list = document.mapNotNull { it.toObject(com.ph32395.staynow_datn.TaoPhongTro.ChiTietThongTin::class.java) }
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
            .whereEqualTo("maPhongTro", maPhongTro)
            .get()
            .addOnSuccessListener { document ->
                val list = document.mapNotNull { it.toObject(PhiDichVu::class.java) }
                _phiDichVuList.value = list
            }
            .addOnFailureListener { exception ->
                Log.e("RoomDetailViewModel", "Loi khi lay du lieu phi dich vu", exception)
            }
    }

//Lay thong tin chi tiet phong tro
    fun fetchRoomDetail(maPhongTro: String) {
        _isLoading.value = true //Bat dau tai

//    Them kiem tra
        if (maPhongTro.isBlank()) {
            Log.e("RoomDetailViewModel", "Loi: ma phong tro trong")
            _isLoading.value = false
            return
        }

//    Them log de debug
        Log.d("RoomDetailViewModel", "Dang tai thong tin phong cos ma: $maPhongTro")

        val docRef = db.collection("PhongTro").document(maPhongTro)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    document.toObject(PhongTroModel::class.java)?.let { room ->
                        _room.value = room
                        Log.d("fetchRoomDetail", "room: $room")
                        fetchAdditionalInfo(room)
                    } ?: Log.d("RoomDetailViewModel", "Khong the chuyen doi du lieu qua PhongTroModel")
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
        room.maGioiTinh?.let { maGioiTinh ->
            db.collection("GioiTinh").document(maGioiTinh)
                .get()
                .addOnSuccessListener { document ->
                    document?.let {
                        val imgUrlGioiTinh = it.getString("imgUrlGioiTinh") ?: ""
                        val tenGioiTinh = it.getString("tenGioiTinh") ?: ""
                        _genderInfo.value = Pair(imgUrlGioiTinh, tenGioiTinh)
                    }
                }
        }

//        Truy van thong tin loai phong tro tu Ma_phongtro
        room.maLoaiNhaTro?.let { maLoaiPhong ->
            db.collection("LoaiPhong").document(maLoaiPhong)
                .get()
                .addOnSuccessListener { document ->
                    document?.let {
                        _roomType.value = it.getString("tenLoaiPhong") ?: ""
                    }
                }
        }

//        Truy van thong tin nguoi dung tu Ma_nguoidung
        room.maNguoiDung.let { maChuTro ->
            realtimeDb.child("NguoiDung").child(maChuTro)
                .get()
                .addOnSuccessListener { dataSnapshot ->
                    dataSnapshot?.let {
                        val anhDaiDien = it.child("anh_daidien").value as? String ?: ""
                        val hoTen = it.child("ho_ten").value as? String ?: ""
                        val ma_NguoiDung = room.maNguoiDung
                        _userId.value = Pair(ma_NguoiDung,hoTen)
                        _userInfo.value = Pair(anhDaiDien, hoTen)
                    }
                }
        }
    }
    // Hàm lấy thông tin tiền cọc từ danh sách chi tiết
    private fun updateTienCocInfo() {
        _tienCocInfo.value = _chiTietList.value?.find { it.tenThongTin == "Tiền cọc" }
    }

    // Hàm public để lấy giá trị tiền cọc
    fun getTienCocValue(): Double {
        return _tienCocInfo.value?.soLuongDonVi?.toDouble() ?: 0.0
    }

//    Ham sao chep phong tro
    fun copyRoom(originalRoom: PhongTroModel, onSuccess: () -> Unit, onError: (String) -> Unit) {
        _isLoading.value = true

//    Tao Id moi cho phong tro
        val newRoomId = db.collection("PhongTro").document().id

//    Tao ban sao cua phong voi Id moi
        val copiedRoom = originalRoom.copy(
            maPhongTro = newRoomId,
            thoiGianTaoPhong = System.currentTimeMillis(),
            ngayCapNhat = System.currentTimeMillis(),
            soLuotXemPhong = 0,
            thoiGianXem = 0L,
            trangThaiLuu = false,
            trangThaiYeuThich = false,
            thoiGianYeuThich = null
        )

//    Bat dau transaction de sao chep tat ca cac du lieu lien quan
        db.runTransaction { transaction ->
//            1. Luu thong tin phong moi
            transaction.set(db.collection("PhongTro").document(newRoomId), copiedRoom)

//            2. Sao chep chi tiet thong tin
            _chiTietList.value?.forEach { chiTiet ->
                val newChiTietId = db.collection("ChiTietThongTin").document().id
                val copiedChiTiet = chiTiet.copy(
                    maPhongTro = newRoomId
                )
                transaction.set(
                    db.collection("ChiTietThongTin").document(newChiTietId),
                    copiedChiTiet
                )
            }

//            3. Sao chep phi dich vu
            _phiDichVuList.value?.forEach { phiDichVu ->
                val newPhiDichVuId = db.collection("PhiDichVu").document().id
                val copiedPhiDichVu = phiDichVu.copy(
                    maPhongTro = newRoomId
                )
                transaction.set(
                    db.collection("PhiDichVu").document(newPhiDichVuId),
                    copiedPhiDichVu
                )
            }

//            4. Sao chep noi that
            _noiThatList.value?.forEach { noiThat ->
                val newPhongTroNoiThatId = db.collection("PhongTroNoiThat").document().id
                val phongTroNoiThat = hashMapOf(
                    "maPhongTro" to newRoomId,
                    "maNoiThat" to noiThat.maNoiThat
                )
                transaction.set(
                    db.collection("PhongTroNoithat").document(newPhongTroNoiThatId),
                    phongTroNoiThat
                )
            }

//            5. Sao chep tien nghi
            _tienNghiList.value?.forEach { tienNghi ->
                val newPhongTroTienNghiId = db.collection("PhongTroTienNghi").document().id
                val phongTroTienNghi = hashMapOf(
                    "maPhongTro" to newRoomId,
                    "maTienNghi" to tienNghi.maTienNghi
                )
                transaction.set(
                    db.collection("PhongTroTienNghi").document(newPhongTroTienNghiId),
                    phongTroTienNghi
                )
            }
        }.addOnSuccessListener {
            _isLoading.value = false
            onSuccess()
        }.addOnFailureListener { e ->
            _isLoading.value = false
            onError(e.message ?: "Co loi xay ra khi sao chep phong")
        }
    }
}

