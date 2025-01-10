package com.ph32395.staynow_datn.fragment.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denzcoskun.imageslider.models.SlideModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.ph32395.staynow_datn.Model.ChiTietThongTin
import com.ph32395.staynow_datn.Model.LoaiPhongTro
import com.ph32395.staynow_datn.Model.PhongTroModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Suppress("LABEL_NAME_CLASH", "NAME_SHADOWING")
class HomeViewModel : ViewModel() {
    private val _selectedLoaiPhongTro = MutableLiveData<String>()
    val selectedLoaiPhongTro: LiveData<String> get() = _selectedLoaiPhongTro

    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _roomList = MutableLiveData<List<Pair<String, PhongTroModel>>>()
    val roomList: LiveData<List<Pair<String, PhongTroModel>>> get() = _roomList
    private val cachedRooms = mutableMapOf<String, List<Pair<String, PhongTroModel>>>()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _loaiPhongTroList = MutableLiveData<List<LoaiPhongTro>>()
    val loaiPhongTroList: LiveData<List<LoaiPhongTro>> get() = _loaiPhongTroList

    private val _imageList = MutableLiveData<List<SlideModel>>()
    val imageList: LiveData<List<SlideModel>> get() = _imageList

    //    LiveData lay phong tro theo trang thai
    private val _phongDaDang = MutableLiveData<List<Pair<String, PhongTroModel>>>()
    val phongDaDang: LiveData<List<Pair<String, PhongTroModel>>> get() = _phongDaDang

    private val _phongDangLuu = MutableLiveData<List<Pair<String, PhongTroModel>>>()
    val phongDangLuu: LiveData<List<Pair<String, PhongTroModel>>> get() = _phongDangLuu

    private val _phongChoDuyet = MutableLiveData<List<Pair<String, PhongTroModel>>>()
    val phongChoDuyet: LiveData<List<Pair<String, PhongTroModel>>> get() = _phongChoDuyet

    private val _phongDaHuy = MutableLiveData<List<Pair<String, PhongTroModel>>>()
    val phongDaHuy: LiveData<List<Pair<String, PhongTroModel>>> get() = _phongDaHuy

    private val _phongDaChoThue = MutableLiveData<List<Pair<String, PhongTroModel>>>()
    val phongDaChoThue: LiveData<List<Pair<String, PhongTroModel>>> get() = _phongDaChoThue

    private val _roomListCT = MutableLiveData<List<Pair<String, PhongTroModel>>>()
    val roomListCT: LiveData<List<Pair<String, PhongTroModel>>> get() = _roomListCT




    // LiveData for selected location
    private val _selectedLocation = MutableLiveData<String>("Tất Cả")
    val selectedLocation: LiveData<String> = _selectedLocation

    // Cache cho từng location
    private val cachedRoomsByLocation = mutableMapOf<String, Map<String, List<Pair<String, PhongTroModel>>>>()

    // Hàm để cập nhật location được chọn
    fun updateSelectedLocation(location: String) {
        _selectedLocation.value = location
        // Cập nhật lại danh sách phòng với location mới
        selectedLoaiPhongTro.value?.let { maloaiPhongTro ->
            updateRoomList(maloaiPhongTro)
        }
    }

    fun selectLoaiPhongTro(idLoaiPhong: String) {
        _selectedLoaiPhongTro.value = idLoaiPhong
    }

    //    Ham lay danh sach phong tro theo ma nguoi dung va trang thai(chuc nang quan ly phong tro)
    fun loadRoomByStatus(maNguoiDung: String) {
        // Lắng nghe thay đổi trong bảng PhongTro
        firestore.collection("PhongTro")
            .whereEqualTo("maNguoiDung", maNguoiDung) // Lọc theo mã người dùng
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("HomeViewModel", "Error listening to rooms: ", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val allRooms = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(PhongTroModel::class.java)?.let { room ->
                            Pair(doc.id, room)
                        }
                    }

                    val roomIds = allRooms.map { it.first } // Lấy danh sách id của phòng trọ

                    // Lắng nghe thay đổi trong bảng ChiTietThongTin
                    firestore.collection("ChiTietThongTin")
                        .whereIn("maPhongTro", roomIds)
                        .whereEqualTo("tenThongTin", "Diện Tích")
                        .addSnapshotListener { chiTietSnapshot, chiTietError ->
                            if (chiTietError != null) {
                                Log.e("HomeViewModel", "Error listening to room details: ", chiTietError)
                                return@addSnapshotListener
                            }

                            val chiTietMap = chiTietSnapshot?.documents?.associate { doc ->
                                doc.getString("maPhongTro") to doc.getDouble("soLuongDonVi")
                            } ?: emptyMap()

                            // Cập nhật thông tin diện tích cho từng phòng
                            val updatedRooms = allRooms.map { (id, room) ->
                                room.dienTich = chiTietMap[id]?.toLong()
                                Pair(id, room)
                            }

                            // Cập nhật LiveData
                            _roomListCT.value = updatedRooms

                            _phongDaDang.value =
                                updatedRooms.filter {
                                    it.second.trangThaiDuyet == "DaDuyet" && it.second.trangThaiPhong == false
                                }
                            _phongDangLuu.value =
                                updatedRooms.filter { it.second.trangThaiLuu }
                            _phongChoDuyet.value =
                                updatedRooms.filter { it.second.trangThaiDuyet == "ChoDuyet" }
                            _phongDaHuy.value =
                                updatedRooms.filter { it.second.trangThaiDuyet == "BiHuy" }
                            _phongDaChoThue.value =
                                updatedRooms.filter { it.second.trangThaiPhong }
                        }
                } else {
                    Log.d("HomeViewModel", "No rooms found for user: $maNguoiDung")
                }
            }
    }

    // Hàm tải danh sách phòng đơn lẻ (không thuộc tòa nhà nào)
    fun loadPhongDonLe(maNguoiDung: String) {
        firestore.collection("PhongTro")
            .whereEqualTo("maNguoiDung", maNguoiDung)
            .whereEqualTo("maNhaTro", "") // Phòng đơn lẻ có maNhaTro rỗng
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("HomeViewModel", "Lỗi khi lắng nghe danh sách phòng: ", error)
                    return@addSnapshotListener
                }
                xuLyDanhSachPhong(snapshot, maNguoiDung)
            }
    }


    // Hàm tải danh sách phòng của một tòa nhà cụ thể
    fun loadPhongTheoToaNha(maNguoiDung: String, maNhaTro: String) {
        firestore.collection("PhongTro")
            .whereEqualTo("maNguoiDung", maNguoiDung)
            .whereEqualTo("maNhaTro", maNhaTro)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("HomeViewModel", "Lỗi khi lắng nghe danh sách phòng: ", error)
                    return@addSnapshotListener
                }
                xuLyDanhSachPhong(snapshot, maNguoiDung)
            }
    }

    // Hàm xử lý dữ liệu phòng từ Firestore
    private fun xuLyDanhSachPhong(snapshot: QuerySnapshot?, maNguoiDung: String) {
        if (snapshot != null && !snapshot.isEmpty) {
            val danhSachPhong = snapshot.documents.mapNotNull { doc ->
                doc.toObject(PhongTroModel::class.java)?.let { phong ->
                    Pair(doc.id, phong)
                }
            }

            val danhSachMaPhong = danhSachPhong.map { it.first }

            // Lấy thông tin diện tích của phòng
            firestore.collection("ChiTietThongTin")
                .whereIn("maPhongTro", danhSachMaPhong)
                .whereEqualTo("tenThongTin", "Diện Tích")
                .addSnapshotListener { chiTietSnapshot, chiTietError ->
                    if (chiTietError != null) {
                        Log.e("HomeViewModel", "Lỗi khi lấy chi tiết phòng: ", chiTietError)
                        return@addSnapshotListener
                    }

                    val mapDienTich = chiTietSnapshot?.documents?.associate { doc ->
                        doc.getString("maPhongTro") to doc.getDouble("soLuongDonVi")
                    } ?: emptyMap()

                    // Cập nhật diện tích cho từng phòng
                    val danhSachPhongCapNhat = danhSachPhong.map { (id, phong) ->
                        phong.dienTich = mapDienTich[id]?.toLong()
                        Pair(id, phong)
                    }

                    // Phân loại phòng theo trạng thái
                    _phongDaDang.value = danhSachPhongCapNhat.filter {
                        it.second.trangThaiDuyet == "DaDuyet" && !it.second.trangThaiPhong
                    }
                    _phongDangLuu.value = danhSachPhongCapNhat.filter { it.second.trangThaiLuu }
                    _phongChoDuyet.value = danhSachPhongCapNhat.filter { it.second.trangThaiDuyet == "ChoDuyet" }
                    _phongDaHuy.value = danhSachPhongCapNhat.filter { it.second.trangThaiDuyet == "BiHuy" }
                    _phongDaChoThue.value = danhSachPhongCapNhat.filter { it.second.trangThaiPhong }
                }
        } else {
            Log.d("HomeViewModel", "Không tìm thấy phòng cho người dùng: $maNguoiDung")
            // Xóa tất cả danh sách khi không có phòng
            _phongDaDang.value = emptyList()
            _phongDangLuu.value = emptyList()
            _phongChoDuyet.value = emptyList()
            _phongDaHuy.value = emptyList()
            _phongDaChoThue.value = emptyList()
        }
    }

    //    Ham cap nhat trang thai phong chuyen phong tu da dang sang dang luu
    fun updateRoomStatus(roomId: String, trangThaiDuyet: String, trangThaiLuu: Boolean) {
        firestore.collection("PhongTro").document(roomId)
            .update(
                mapOf(
                    "trangThaiDuyet" to trangThaiDuyet,
                    "trangThaiLuu" to trangThaiLuu
                )
            )
            .addOnSuccessListener {
                Log.d("HomeViewModel", "Room status updated successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("HomeViewModel", "Error updating room status: ", exception)
            }
    }

    //    Doi trang thai de huy phong
    fun updateRoomStatusHuyPhong(roomId: String, trangThaiDuyet: String) {
        firestore.collection("PhongTro").document(roomId)
            .update(
                mapOf(
                    "trangThaiDuyet" to trangThaiDuyet
                )
            )
            .addOnSuccessListener {
                Log.d("HomeViewModel", "Room status updated successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("HomeViewModel", "Error updating room status: ", exception)
            }
    }


    // Hàm để cập nhật số lượt xem phòng trong Firestore
    fun incrementRoomViewCount(roomId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val roomRef = firestore.collection("PhongTro").document(roomId)
                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(roomRef)
                    val currentViewCount = snapshot.getLong("soLuotXemPhong") ?: 0
                    transaction.update(roomRef, "soLuotXemPhong", currentViewCount + 1)
                }.await()
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Lỗi tăng số lượt xem: ${e.message}")
            }
        }
    }


    fun loadLoaiPhongTro() {
        _isLoading.value = true  // Bắt đầu tải dữ liệu, set loading = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Lấy dữ liệu từ Firebase
                val snapshot = firestore.collection("LoaiPhong").get().await()
                val list = snapshot.documents.mapNotNull { it.toObject(LoaiPhongTro::class.java) }

                withContext(Dispatchers.Main) {
                    // Cập nhật danh sách Loại Phòng lên UI
                    _loaiPhongTroList.value = list
                    _isLoading.value = false  // Dữ liệu đã tải xong, set loading = false
                }
            } catch (e: Exception) {
                // Xử lý lỗi
                Log.e("HomeViewModel", "Error loading LoaiPhongTro: ${e.message}", e)
                _isLoading.value = false  // Nếu có lỗi thì set loading = false
            }
        }
    }


    //    lay anh tu Store de hien thi len banner
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

    // Sửa lại hàm updateRoomList để hỗ trợ lọc theo location
    fun updateRoomList(maloaiPhongTro: String) {
        val currentLocation = _selectedLocation.value ?: "Tất Cả"

        // Kiểm tra cache
        cachedRoomsByLocation[currentLocation]?.get(maloaiPhongTro)?.let {
            _roomList.postValue(it)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            firestore.collection("LoaiPhong")
                .whereEqualTo("maLoaiPhong", maloaiPhongTro)
                .addSnapshotListener { loaiPhongSnapshot, exception ->
                    if (exception != null) {
                        Log.e("HomeViewModel", "Error listening for changes: ", exception)
                        return@addSnapshotListener
                    }

                    val tenLoaiPhong = loaiPhongSnapshot?.documents?.firstOrNull()?.getString("tenLoaiPhong")
                    if (tenLoaiPhong != null) {
                        val roomsRef = firestore.collection("PhongTro")

                        // Tạo query cơ bản
                        var query = roomsRef.whereEqualTo("trangThaiDuyet", "DaDuyet")
                            .whereEqualTo("trangThaiPhong", false)

                        // Thêm điều kiện lọc theo location nếu không phải "Tất Cả"
                        if (currentLocation != "Tất Cả") {
                            query = query.whereEqualTo("dcTinhTP", currentLocation)
                        }

                        // Thêm điều kiện lọc theo loại phòng nếu không phải "Tất Cả"
                        if (tenLoaiPhong != "Tất Cả") {
                            query = query.whereEqualTo("maLoaiNhaTro", maloaiPhongTro)
                        }

                        query.addSnapshotListener { snapshot, e ->
                            if (e != null) {
                                Log.e("HomeViewModel", "Error fetching rooms: ", e)
                                return@addSnapshotListener
                            }

                            if (snapshot != null) {
                                val rooms = snapshot.documents.map { doc ->
                                    Pair(doc.id, doc.toObject(PhongTroModel::class.java)!!)
                                }

                                // Cập nhật cache cho location hiện tại
                                if (!cachedRoomsByLocation.containsKey(currentLocation)) {
                                    cachedRoomsByLocation[currentLocation] = mutableMapOf()
                                }
                                (cachedRoomsByLocation[currentLocation] as MutableMap)[maloaiPhongTro] = rooms

                                fetchChiTietThongTinForRoomList(rooms, maloaiPhongTro)
                                _roomList.postValue(rooms)
                            }
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

    //    lay du lieu danh sach hien thi o man chu tro phong tro su dung Kotlin Coroutines
    fun updateRoomListWithCoroutines() {
//        Lay id tai khoan dang nhap
        val idUser = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val roomRef = firestore.collection("PhongTro")
                val querySnapshot = roomRef.get().await() // Truy van  toan bo danh sach phong tro

//                Chuyen doi du lieu Firebase thanh danh sach Rm
                val rooms = querySnapshot.documents.mapNotNull { doc ->
                    val roomModel = doc.toObject(PhongTroModel::class.java)
                    roomModel?.let {
                        if (it.maNguoiDung != idUser && !it.trangThaiPhong && it.trangThaiDuyet == "DaDuyet") {
                            Pair(doc.id, it)
                        } else {
                            null
                        }

                    }
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
                    .whereEqualTo("maPhongTro", roomId)
                    .whereEqualTo("tenThongTin", "Diện Tích")
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
                        val chiTiet =
                            documents?.firstOrNull()?.toObject(ChiTietThongTin::class.java)
                        chiTiet?.let {
                            room.dienTich = it.soLuongDonVi
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
                    .whereEqualTo("maPhongTro", roomId)
                    .whereEqualTo("tenThongTin", "Diện Tích")
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
                        val chiTiet =
                            documents?.firstOrNull()?.toObject(ChiTietThongTin::class.java)
                        chiTiet?.let {
                            room.dienTich = it.soLuongDonVi
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
        cachedRoomsByLocation.clear()
        Log.d("HomeViewModel", "Cache cleared")  // Để debug log khi cache bị xóa
    }
}
