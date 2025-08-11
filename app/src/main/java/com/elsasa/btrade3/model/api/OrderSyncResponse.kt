package com.elsasa.btrade3.model.api


data class OrderSyncResponse(
    val status: String,
    val code: String,
    val data: String?, // Changed from OrderSyncData? to String?
    val message: String? = null // Added message field
)