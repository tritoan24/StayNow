package com.ph32395.staynow.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ph32395.staynow.Model.RoomDetailModel

class RoomDetailViewModel : ViewModel() {

    private val _room = MutableLiveData<RoomDetailModel>()
    val room: LiveData<RoomDetailModel> get() = _room

    // Sử dụng MutableLiveData để lưu danh sách ảnh
    private val _images = MutableLiveData<List<String>>()
    val images: LiveData<List<String>> get() = _images  // Định nghĩa LiveData cho hình ảnh

    // Hàm này dùng để cập nhật danh sách ảnh
    fun setImages(newImages: List<String>) {
        _images.value = newImages
    }

    init {
//        Them du lieu cung cho phong tro
        loadMockData()
    }

    private fun loadMockData() {
        val Danh_sachanh = listOf(
            "https://firebasestorage.googleapis.com/v0/b/staynowapp1.appspot.com/o/avatars%2Fanh_tro_1.jpg?alt=media&token=f7b1c2b5-b718-4811-8090-fb7b586a8ed8",
            "https://firebasestorage.googleapis.com/v0/b/staynowapp1.appspot.com/o/avatars%2Fanh_tro_2.jpg?alt=media&token=e358c1c8-9159-4d67-b33e-1406e560ea1b",
            "https://firebasestorage.googleapis.com/v0/b/staynowapp1.appspot.com/o/avatars%2Fanh_tro_3.jpg?alt=media&token=70b837e5-9519-447a-8bf0-15872e4a743e",
            "https://firebasestorage.googleapis.com/v0/b/staynowapp1.appspot.com/o/avatars%2Fanh_tro_4.jpg?alt=media&token=d8ef4124-12cf-4f11-a493-c529d4921346"
        )

        val mockRoom = RoomDetailModel(
            Ma_phongtro = "1",
            Ma_nguoidung = "user123",
            Ma_gioitinh = "Nam",
            Ten_phongtro = "Phòng khép kín, có gác trên ",
            Dia_chi = "49 Lê Đức Thọ, Mỹ Đình 2, Nam Từ Liêm, Hà Nội",
            Chi_tietthem = "Thời gian không giới hạn\n Để được 3 xe\n Không chung chủ",
            Loai_phongtro = "Chung cư mini",
            Trang_thai = "Online 20 phút trước",
            Dien_tich = "25m²",
            Gia_thue = "4,500,000",
            Danh_sachanh = Danh_sachanh,
            So_luotxem = 130,
            Ngay_tao = "06/11/2024",
            Ngay_capnhat = "06/11/2024",
            So_nguoi = 3,
            Tang = 4,
            Tien_coc = "4,000,000",
            Danh_gia = "4,5"
        )
        _room.value = mockRoom
    }
}