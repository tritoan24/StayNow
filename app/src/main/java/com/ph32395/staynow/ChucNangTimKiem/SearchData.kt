package com.ph32395.staynow.ChucNangTimKiem

class SearchData(
    val Ma_timkiem: String? = null,
    val Tu_khoa: String? = null,
    val Thoi_giantimkiem: String? = null
) {



    override fun toString(): String {
        return "Id: $Ma_timkiem\n" +
                "Key: $Tu_khoa\n" +
                "Time: $Thoi_giantimkiem"
    }

}