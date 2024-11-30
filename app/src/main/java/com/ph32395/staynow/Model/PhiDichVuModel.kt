package com.ph32395.staynow.Model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PhiDichVuModel(
    val don_vi: String = "",
    val icon_dichvu: String = "",
    val ma_phidichvu: String? = "",
    val ma_phongtro: String = "",
    val so_tien: Double = 0.0,
    val ten_dichvu: String = ""
) : Parcelable {
}