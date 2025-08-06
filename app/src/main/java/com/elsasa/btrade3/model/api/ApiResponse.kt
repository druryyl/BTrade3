package com.elsasa.btrade3.model.api

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)