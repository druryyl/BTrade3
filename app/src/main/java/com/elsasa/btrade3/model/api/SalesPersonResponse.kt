package com.elsasa.btrade3.model.api

import com.elsasa.btrade3.model.SalesPerson

data class SalesPersonListResponse(
    val status: String,
    val code: String,
    val data: List<SalesPerson>
)
