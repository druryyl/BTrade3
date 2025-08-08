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