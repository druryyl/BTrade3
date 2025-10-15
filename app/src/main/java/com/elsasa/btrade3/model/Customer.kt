package com.elsasa.btrade3.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize


@Suppress("DEPRECATED_ANNOTATION")
@Entity(tableName = "customer_table")
@Parcelize
data class Customer(
    @PrimaryKey
    val customerId: String,
    val customerCode: String,
    val customerName: String,
    val alamat: String,
    val wilayah: String,
    val latitude: Double = 0.0,      // New field
    val longitude: Double = 0.0,     // New field
    val accuracy: Float = 0.0f,      // GPS accuracy in meters
    val locationTimestamp: Long = 0L, // When location was set
    val isUpdated: Boolean = false
): Parcelable