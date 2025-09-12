package com.elsasa.btrade3.model


data class OrderSummary(
    val userEmail: String,
    val orderDate: String,
    val orderCount: Int,
    val grossSales: Double,
    val totalItems: Int
)
