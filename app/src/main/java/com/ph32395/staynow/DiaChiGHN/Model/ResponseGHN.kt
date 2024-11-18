package com.ph32395.staynow.DiaChiGHN.Model

class ResponseGHN<T>(
    val data: T,
    val message: String,
    val code: Int,
)
