package com.ph32395.staynow.Model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChiTietThongTinModel(
    val don_vi: String = "",
    val icon_thongtin: String = "",
    val ma_phongtro: String = "",
    val so_luong_donvi: Long = 0L,
    val ten_thongtin: String = ""
) : Parcelable
