package com.elsasa.btrade3.model.api


data class CustomerSyncRequest(
    val customerId: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val coordinateTimeStamp: Long,
    val coordinateUser: String
)
