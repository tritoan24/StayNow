package com.ph32395.staynow.payment

data class OrderRequest(
    val amount: Double,
    val contractId: String,
    val billId: String,
    val items: String,
)

