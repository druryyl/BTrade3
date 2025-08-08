package com.elsasa.btrade3.model.api

import com.elsasa.btrade3.model.Customer

data class CustomerListResponse(
    val status: String,
    val code: String,
    val data: List<Customer>
)
