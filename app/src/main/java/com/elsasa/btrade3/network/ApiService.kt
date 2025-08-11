package com.elsasa.btrade3.network

import com.elsasa.btrade3.model.api.ApiResponse
import com.elsasa.btrade3.model.api.BarangListResponse
import com.elsasa.btrade3.model.api.CustomerListResponse
import com.elsasa.btrade3.model.api.OrderSyncRequest
import com.elsasa.btrade3.model.api.OrderSyncResponse
import com.elsasa.btrade3.model.api.SalesPersonListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("Brg") // Adjust endpoint as needed
    suspend fun getBarangs(): Response<BarangListResponse>

    @GET("Customer") // Add this endpoint
    suspend fun getCustomers(): Response<CustomerListResponse>

    @GET("SalesPerson") // Add this endpoint
    suspend fun getSalesPersons(): Response<SalesPersonListResponse>

    @POST("Order") // Add this endpoint for order sync
    suspend fun syncOrder(@Body orderRequest: OrderSyncRequest): Response<OrderSyncResponse>
}