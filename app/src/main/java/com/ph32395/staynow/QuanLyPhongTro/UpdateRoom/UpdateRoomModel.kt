package com.ph32395.staynow.QuanLyPhongTro.UpdateRoom

import android.os.Parcelable
import com.ph32395.staynow.Model.ChiTietThongTinModel
import com.ph32395.staynow.Model.NoiThatModel
import com.ph32395.staynow.Model.PhiDichVuModel
import com.ph32395.staynow.Model.TienNghiModel
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class UpdateRoomModel(
    val Ten_phongtro: String = "",
    val Dia_chi: String = "",
    val Loai_phong: String = "",
    val Gioi_tinh: String = "",
    val Url_image: ArrayList<String> = ArrayList(),
    val Gia_phong: Double = 0.0,
    val Chi_tietthongtin: ArrayList<ChiTietThongTinModel> = ArrayList(),
    val Dich_vu: ArrayList<PhiDichVuModel> = ArrayList(),
    val Noi_that: ArrayList<NoiThatModel> = ArrayList(),
    val Tien_nghi: ArrayList<TienNghiModel> = ArrayList(),
    val Chi_tietthem: String = ""
) : Parcelable {

}