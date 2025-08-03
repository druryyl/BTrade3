package com.elsasa.btrade3.model


import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "faktur_item_table",
    primaryKeys = ["fakturId", "noUrut"],
    foreignKeys = [ForeignKey(
        entity = Faktur::class,
        parentColumns = ["fakturId"],
        childColumns = ["fakturId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class FakturItem(
    val fakturId: String,
    val noUrut: Int,
    val brgCode: String,
    val brgName: String,
    val qty: Int,
    val unitName: String,
    val unitPrice: Double,
    val lineTotal: Double
)