package com.elsasa.btrade3.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "customer_table")
data class Customer(
    @PrimaryKey
    val customerId: String,
    val customerCode: String,
    val customerName: String,
    val alamat: String,
    val wilayah: String
)