package com.ph32395.staynow_datn.NoiThat

data class NoiThat(
    val maNoiThat: String? = null,
    val tenNoiThat: String,
    val iconNoiThat: String,
    val trangThai: Boolean,
) {
    constructor() : this("", "", "", false)
}
