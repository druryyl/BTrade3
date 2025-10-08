package com.elsasa.btrade3.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customer_location_table")
data class CustomerLocation(
    @PrimaryKey
    val customerId: String,
    val latitude: Double,
    val longitude: Double,
    val address: String = "",
    val accuracy: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)