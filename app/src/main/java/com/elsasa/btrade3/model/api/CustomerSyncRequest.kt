package com.elsasa.btrade3.model.api


data class CustomerSyncRequest(
    val customerId: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val coordinateTimeStamp: Long,
    val coordinateUser: String,
    val serverId: String
)

data class CheckInRequest(
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
    val statusSync: String,
    val serverId: String
)