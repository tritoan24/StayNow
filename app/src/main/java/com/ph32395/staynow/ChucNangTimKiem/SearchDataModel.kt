package com.ph32395.staynow.ChucNangTimKiem

class SearchDataModel(
    val ma_timkiem: String? = null,
    val tu_khoa: String? = null,
    val thoi_giantimkiem: String? = null,
    var timestamps: String? = null
) {


    override fun toString(): String {
        return "Id: $ma_timkiem\n" +
                "Key: $tu_khoa\n" +
                "Time: $thoi_giantimkiem\n" +
                "Timestamps: $timestamps"
    }

}