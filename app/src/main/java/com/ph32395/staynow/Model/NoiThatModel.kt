package com.ph32395.staynow.Model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NoiThatModel(
    val Icon_noithat: String = "",
    val Ten_noithat: String = "",
    val Status: Boolean = true
) : Parcelable {
}