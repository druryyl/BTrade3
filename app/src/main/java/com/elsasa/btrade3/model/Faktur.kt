package com.elsasa.btrade3.model


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "faktur_table")
data class Faktur(
    @PrimaryKey
    val fakturId: String,
    val customerCode: String,
    val customerName: String,
    val fakturDate: String, // yyyy-MM-dd
    val salesName: String,
    val totalAmount: Double,
    val userEmail: String = ""
)
