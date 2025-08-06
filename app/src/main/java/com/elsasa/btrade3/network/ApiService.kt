package com.elsasa.btrade3.network

import com.elsasa.btrade3.model.api.ApiResponse
import com.elsasa.btrade3.model.api.BarangListResponse
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("Brg") // Adjust endpoint as needed
    suspend fun getBarangs(): Response<BarangListResponse>

    // Future endpoints for Customer and Sales
    // @GET("customers")
    // suspend fun getCustomers(): Response<ApiResponse<CustomerListResponse>>

    // @GET("sales")
    // suspend fun getSales(): Response<ApiResponse<SalesListResponse>>
}