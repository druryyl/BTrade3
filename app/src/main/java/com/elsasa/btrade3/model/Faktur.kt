package com.elsasa.btrade3.model


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "faktur_table")
data class Faktur(
    @PrimaryKey
    val fakturId: String,
    val globalId: String,
    val customerId: String,
    val customerCode: String,
    val customerName: String,
    val customerAddress: String,
    val fakturDate: String, // yyyy-MM-dd
    val salesId: String,
    val salesName: String,
    val totalAmount: Double,
    val userEmail: String,
    val statusSync: String
)
