package com.ph32395.staynow_datn.Model


data class LoaiPhongTro(
    val Ma_loaiphong: String,
    val Status: Boolean,
    val Ten_loaiphong: String
) {
    constructor() : this("", false, "")

}

