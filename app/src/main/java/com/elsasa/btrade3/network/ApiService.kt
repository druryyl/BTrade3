package com.elsasa.btrade3.network

import com.elsasa.btrade3.model.CheckIn
import com.elsasa.btrade3.model.api.ApiResponse
import com.elsasa.btrade3.model.api.BarangListResponse
import com.elsasa.btrade3.model.api.CheckInSyncResponse
import com.elsasa.btrade3.model.api.CustomerListResponse
import com.elsasa.btrade3.model.api.CustomerSyncRequest
import com.elsasa.btrade3.model.api.CustomerSyncResponse
import com.elsasa.btrade3.model.api.OrderSyncRequest
import com.elsasa.btrade3.model.api.OrderSyncResponse
import com.elsasa.btrade3.model.api.SalesPersonListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("Brg/{serverId}")
    suspend fun getBarangs(@Path("serverId") serverId: String): Response<BarangListResponse>

    @GET("Customer/{serverId}") // Add this endpoint
    suspend fun getCustomers(@Path("serverId") serverId: String): Response<CustomerListResponse>

    @GET("SalesPerson/{serverId}") // Add this endpoint
    suspend fun getSalesPersons(@Path("serverId") serverId: String): Response<SalesPersonListResponse>

    @POST("Order") // Add this endpoint for order sync
    suspend fun syncOrder(@Body orderRequest: OrderSyncRequest): Response<OrderSyncResponse>

    @PATCH("Customer")
    suspend fun syncCustomerLocation(@Body customerRequest: CustomerSyncRequest): Response<CustomerSyncResponse>

    @POST("CheckIn")
    suspend fun syncCheckIn(@Body checkInRequest: CheckIn): Response<CheckInSyncResponse>
}