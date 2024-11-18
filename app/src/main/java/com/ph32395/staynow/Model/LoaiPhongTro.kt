package com.ph32395.staynow.Model


data class LoaiPhongTro(
    val Ma_loaiphong: String,
    val Status: Boolean,
    val Ten_loaiphong: String
) {
    constructor() : this("", false, "")

}

