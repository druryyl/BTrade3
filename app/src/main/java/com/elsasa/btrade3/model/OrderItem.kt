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
        onDelete = ForeignKey.NO_ACTION
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
    val qtyBonus: Int,
    val konversi: Int,
    val unitPrice: Double,
    val disc1: Double,
    val disc2: Double,
    val disc3: Double,
    val disc4: Double,
    val lineTotal: Double
)