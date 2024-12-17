package com.ph32395.staynow_datn.Model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TienNghiModel(
    val Icon_tiennghi: String = "",
    val Status: Boolean = true,
    val Ten_tiennghi: String = ""
) : Parcelable {
}