package com.ph32395.staynow.payment

data class OrderRequest(
    val amount: Double,
    val contractId: String,
    val billId: String,
    val items: String,
    val typeBill: TypeBill
)
data class OrderRequestService(
    val amount: Double,
    val billId: String,
    val items: String,
    val typeBill: TypeBill
)

