package com.elsasa.btrade3.model


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checkin_table")
data class CheckIn(
    @PrimaryKey
    val checkInId: String,
    val checkInDate: String,        // yyyy-MM-dd
    val checkInTime: String,        // HH:mm:ss
    val userEmail: String,
    val checkInLatitude: Double,
    val checkInLongitude: Double,
    val accuracy: Float,
    val customerId: String,
    val customerCode: String,
    val customerName: String,
    val customerAddress: String,
    val customerLatitude: Double,
    val customerLongitude: Double,
    val statusSync: String          // "DRAFT" initially
)