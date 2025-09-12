package com.elsasa.btrade3.model


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_table")
data class Order(
    @PrimaryKey
    val orderId: String,
    val orderLocalId: String,
    val customerId: String,
    val customerCode: String,
    val customerName: String,
    val customerAddress: String,

    val orderDate: String,
    val salesId: String,
    val salesName: String,
    val totalAmount: Double,

    val userEmail: String,
    val statusSync: String,

    val fakturCode: String,
    val orderNote: String
)
