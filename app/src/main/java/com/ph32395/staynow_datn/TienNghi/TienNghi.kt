package com.ph32395.staynow_datn.TienNghi

data class TienNghi(
    val maTienNghi: String? = null,
    val tenTienNghi: String,
    val iconTienNghi: String,
    val trangThai: Boolean,
) {
    constructor() : this("", "", "", false)
}