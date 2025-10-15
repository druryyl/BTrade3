package com.elsasa.btrade3.model.api


data class CustomerSyncResponse(
    val status: String,
    val code: String,
    val message: String?,
    val data: String?
)
