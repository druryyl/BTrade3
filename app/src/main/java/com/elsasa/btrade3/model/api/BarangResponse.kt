package com.elsasa.btrade3.model.api

import com.elsasa.btrade3.model.Barang

data class BarangListResponse(
    val status: String,
    val code: String,
    val data: List<Barang>
)