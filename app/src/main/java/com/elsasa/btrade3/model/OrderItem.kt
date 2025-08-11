package com.elsasa.btrade3.model


import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "order_item_table",
    primaryKeys = ["orderId", "noUrut"],
    foreignKeys = [ForeignKey(
        entity = Order::class,
        parentColumns = ["orderId"],
        childColumns = ["orderId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class OrderItem(
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
    val konversi: Int,
    val unitPrice: Double,
    val lineTotal: Double
)