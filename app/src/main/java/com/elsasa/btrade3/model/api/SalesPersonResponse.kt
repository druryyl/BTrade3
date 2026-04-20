package com.elsasa.btrade3.model.api

import com.elsasa.btrade3.model.SalesPerson

data class SalesPersonListResponse(
    val status: String,
    val code: String,
    val data: List<SalesPerson>
)

data class SalesPersonExistsData(
    val email: String,
    val isValid: Int
)

data class SalesPersonExistsResponse(
    val status: String,
    val code: String,
    val data: SalesPersonExistsData
)

