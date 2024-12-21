package com.ph32395.staynow_datn.Model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NoiThatModel(
    val iconNoiThat: String = "",
    val tenNoiThat: String = "",
    val status: Boolean = true
) : Parcelable {
}