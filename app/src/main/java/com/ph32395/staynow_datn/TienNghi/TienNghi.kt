package com.ph32395.staynow_datn.TienNghi

data class TienNghi(
    val Ma_tiennghi: String? = null,
    val Ten_tiennghi: String,
    val Icon_tiennghi: String,
    val Status: Boolean,
) {
    constructor() : this("", "", "", false)
}