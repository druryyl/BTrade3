package com.elsasa.btrade3.model.api


data class CheckInSyncResponse(
    val status: String,
    val code: String,
    val data: String?
)