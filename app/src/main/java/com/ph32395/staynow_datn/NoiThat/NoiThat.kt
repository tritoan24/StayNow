package com.ph32395.staynow_datn.NoiThat

data class NoiThat(
    val Ma_noithat: String? = null,
    val Ten_noithat: String,
    val Icon_noithat: String,
    val Status: Boolean,
) {
    constructor() : this("", "", "", false)
}
