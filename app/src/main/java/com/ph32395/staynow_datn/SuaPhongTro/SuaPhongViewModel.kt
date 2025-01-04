package com.ph32395.staynow_datn.SuaPhongTro

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.ph32395.staynow_datn.DichVu.DichVu
import com.ph32395.staynow_datn.Model.PhongTroModel
import com.ph32395.staynow_datn.TaoPhongTro.ChiTietThongTin
import com.ph32395.staynow_datn.TaoPhongTro.PhiDichVu

class RoomDetailsViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _roomDetails = MutableLiveData<PhongTroModel?>()
    val roomDetails: LiveData<PhongTroModel> get() = _roomDetails as LiveData<PhongTroModel>

    private val _services = MutableLiveData<List<PhiDichVu>>()
    val services: LiveData<List<PhiDichVu>> get() = _services

    private val _furniture = MutableLiveData<List<NoiThat>>()
    val furniture: LiveData<List<NoiThat>> get() = _furniture

    private val _amenities = MutableLiveData<List<TienNghi>>()
    val amenities: LiveData<List<TienNghi>> get() = _amenities

    private val _images = MutableLiveData<List<String>>()
    val images: LiveData<List<String>> get() = _images

    private val _listDichVu = MutableLiveData<List<DichVu>>()
    val listDichVu: LiveData<List<DichVu>> get() = _listDichVu


    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _details = MutableLiveData<List<ChiTietThongTin>>()
    val imfor: LiveData<List<ChiTietThongTin>> get() = _details

    // Fetch all room details
    fun fetchRoomDetails(roomId: String) {
        // Fetch room basic details
        firestore.collection("PhongTro").document(roomId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val room = document.toObject<PhongTroModel>()
                    _roomDetails.postValue(room)

                    // Fetch related collections
                    fetchServices(roomId)
                    fetchFurniture(roomId)
                    fetchAmenities(roomId)
                    fetchImfor(roomId)
                } else {
                    _error.postValue("Phòng trọ không tồn tại.")
                }
            }
            .addOnFailureListener { exception ->
                _error.postValue("Lỗi khi tải dữ liệu phòng trọ: ${exception.message}")
            }
    }

    fun fetchServices(roomId: String) {
        // Lấy dữ liệu từ bảng PhiDichVu
        firestore.collection("PhiDichVu")
            .whereEqualTo("maPhongTro", roomId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val servicesList = documents.map { it.toObject(PhiDichVu::class.java) }
                    _services.postValue(servicesList) // Cập nhật LiveData
                } else {
                    _services.postValue(emptyList()) // Không có dịch vụ nào
                }
            }
            .addOnFailureListener { exception ->
                _error.postValue("Lỗi khi tải dịch vụ: ${exception.message}")
            }
    }

    fun fetchImfor(roomId: String) {
        // Lấy dữ liệu từ bảng ChiTietThongTin
        firestore.collection("ChiTietThongTin")
            .whereEqualTo("maPhongTro", roomId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val detailsList = documents.map { it.toObject(ChiTietThongTin::class.java) }
                    _details.postValue(detailsList) // Cập nhật LiveData
                } else {
                    _details.postValue(emptyList()) // Không có chi tiết thông tin nào
                }
            }
            .addOnFailureListener { exception ->
                _error.postValue("Lỗi khi tải chi tiết thông tin: ${exception.message}")
            }
}
    fun getListDichVu() {
        firestore.collection("DichVu")
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val dichVuList = documents.map { doc ->
                        val dichVu = doc.toObject(DichVu::class.java)
                        Log.d("DichVu_Debug", "Tên dịch vụ: ${dichVu.tenDichVu}")
                        Log.d("DichVu_Debug", "Đơn vị: ${dichVu.donVi}")
                        dichVu
                    }
                    _listDichVu.postValue(dichVuList)
                } else {
                    _listDichVu.postValue(emptyList())
                }
            }
            .addOnFailureListener { exception ->
                Log.e("DichVu_Error", "Lỗi: ${exception.message}")
                _error.postValue("Lỗi khi tải danh sách dịch vụ: ${exception.message}")
            }
    }


    // Fetch furniture related to the room
    private fun fetchFurniture(roomId: String) {
        firestore.collection("PhongTroNoiThat")
            .whereEqualTo("maPhongTro", roomId)
            .get()
            .addOnSuccessListener { documents ->
                val furnitureIds = documents.mapNotNull { it.getString("maNoiThat") }
                if (furnitureIds.isNotEmpty()) {
                    firestore.collection("NoiThat")
                        .whereIn("maNoiThat", furnitureIds)
                        .get()
                        .addOnSuccessListener { noiThatDocs ->
                            val furniture = noiThatDocs.map { it.toObject<NoiThat>() }
                            _furniture.postValue(furniture)
                        }
                        .addOnFailureListener { exception ->
                            _error.postValue("Lỗi khi tải nội thất: ${exception.message}")
                        }
                } else {
                    _furniture.postValue(emptyList()) // Không có nội thất
                }
            }
            .addOnFailureListener { exception ->
                _error.postValue("Lỗi khi tải danh sách nội thất: ${exception.message}")
            }

    }


    // Fetch amenities related to the room
    private fun fetchAmenities(roomId: String) {
        firestore.collection("PhongTroTienNghi")
            .whereEqualTo("maPhongTro", roomId)
            .get()
            .addOnSuccessListener { documents ->
                val amenityIds = documents.mapNotNull { it.getString("maTienNghi") }
                if (amenityIds.isNotEmpty()) {
                    firestore.collection("TienNghi")
                        .whereIn("maTienNghi", amenityIds)
                        .get()
                        .addOnSuccessListener { tienNghiDocs ->
                            val amenities = tienNghiDocs.map { it.toObject<TienNghi>() }
                            _amenities.postValue(amenities)
                        }
                        .addOnFailureListener { exception ->
                            _error.postValue("Lỗi khi tải tiện nghi: ${exception.message}")
                        }
                } else {
                    _amenities.postValue(emptyList()) // Không có tiện nghi
                }
            }
            .addOnFailureListener { exception ->
                _error.postValue("Lỗi khi tải danh sách tiện nghi: ${exception.message}")
            }

    }

    // Fetch images related to the room
    fun fetchImages(imageUrls: List<String>) {
        _images.postValue(imageUrls)
    }



    fun initializeSelectedLists(roomId: String) {
        firestore.collection("PhongTroNoiThat")
            .whereEqualTo("maPhongTro", roomId)
            .get()
            .addOnSuccessListener { documents ->
                val furnitureIds = documents.mapNotNull { it.getString("maNoiThat") }
                if (furnitureIds.isNotEmpty()) {
                    firestore.collection("NoiThat")
                        .whereIn("maNoiThat", furnitureIds)
                        .get()
                        .addOnSuccessListener { noiThatDocs ->
                            val furniture = noiThatDocs.map { it.toObject<NoiThat>() }
                            _furniture.postValue(furniture)
                        }
                        .addOnFailureListener { exception ->
                            _error.postValue("Lỗi khi tải nội thất: ${exception.message}")
                        }
                } else {
                    _furniture.postValue(emptyList()) // Không có nội thất
                }
            }
            .addOnFailureListener { exception ->
                _error.postValue("Lỗi khi tải danh sách nội thất: ${exception.message}")
            }


        firestore.collection("PhongTroTienNghi")
            .whereEqualTo("maPhongTro", roomId)
            .get()
            .addOnSuccessListener { documents ->
                val amenityIds = documents.mapNotNull { it.getString("maTienNghi") }
                if (amenityIds.isNotEmpty()) {
                    firestore.collection("TienNghi")
                        .whereIn("maTienNghi", amenityIds)
                        .get()
                        .addOnSuccessListener { tienNghiDocs ->
                            val amenities = tienNghiDocs.map { it.toObject<TienNghi>() }
                            _amenities.postValue(amenities)
                        }
                        .addOnFailureListener { exception ->
                            _error.postValue("Lỗi khi tải tiện nghi: ${exception.message}")
                        }
                } else {
                    _amenities.postValue(emptyList()) // Không có tiện nghi
                }
            }
            .addOnFailureListener { exception ->
                _error.postValue("Lỗi khi tải danh sách tiện nghi: ${exception.message}")
            }

    }


}


data class NoiThat(
    val maNoiThat: String = "",
    val tenNoiThat: String = ""
)

data class TienNghi(
    val maTienNghi: String = "",
    val tenTienNghi: String = ""
)
