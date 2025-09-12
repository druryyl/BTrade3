package com.elsasa.btrade3.model.api

data class OrderSyncRequest(
    val orderId: String,
    val orderLocalId: String,
    val customerId: String,
    val customerCode: String,
    val customerName: String,
    val address: String,
    val orderDate: String,
    val salesId: String,
    val salesName: String,
    val totalAmount: Double,
    val userEmail: String,
    val orderNote: String,
    val listItem: List<OrderItemSyncDto>
)

data class OrderItemSyncDto(
    val orderId: String,
    val noUrut: Int,
    val brgId: String,
    val brgCode: String,
    val brgName: String,
    val kategoriName: String,
    val qtyBesar: Int,
    val satBesar: String,
    val qtyKecil: Int,
    val satKecil: String,
    val qtyBonus: Int,
    val konversi: Int,
    val unitPrice: Double,
    val disc1: Double,
    val disc2: Double,
    val disc3: Double,
    val disc4: Double,
    val lineTotal: Double
)