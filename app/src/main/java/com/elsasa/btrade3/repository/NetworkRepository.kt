package com.elsasa.btrade3.repository

import android.content.Context
import android.util.Log
import com.elsasa.btrade3.model.Barang
import com.elsasa.btrade3.model.Customer
import com.elsasa.btrade3.model.SalesPerson
import com.elsasa.btrade3.network.ApiService
import com.elsasa.btrade3.util.ServerHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class NetworkRepository(
    private val apiService: ApiService,
    private val serverHelper: ServerHelper
) {
    companion object {
        private const val TAG = "NetworkRepository"
    }

    suspend fun fetchBarangs(context: Context): Result<List<Barang>> = withContext(Dispatchers.IO) {
        try {
            val serverId = serverHelper.getSelectedServer(context)
            val response = apiService.getBarangs(serverId)

            if (response.isSuccessful) {
                val apiResponse = response.body()

                if (apiResponse?.status == "success") {
                    val data = apiResponse.data
                    Result.success(data)
                } else {
                    val errorMessage = apiResponse?.status ?: "Unknown error"
                    Result.failure(Exception("API Error: $errorMessage"))
                }
            } else {
                val errorMessage = "HTTP ${response.code()}: ${response.message()}"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: OutOfMemoryError) {
            Result.failure(Exception("Out of memory: Data too large to process"))
        } catch (e: IOException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Unexpected error: ${e.message}"))
        }
    }

    suspend fun fetchCustomers(context: Context): Result<List<Customer>> = withContext(Dispatchers.IO) {
        try {
            val serverId = serverHelper.getSelectedServer(context)
            val response = apiService.getCustomers(serverId)

            if (response.isSuccessful) {
                val apiResponse = response.body()

                if (apiResponse?.status == "success") {
                    val data = apiResponse.data ?: emptyList()
                    Result.success(data)
                } else {
                    val errorMessage = apiResponse?.status ?: "Unknown error"
                    Result.failure(Exception("API Error: $errorMessage"))
                }
            } else {
                val errorMessage = "HTTP ${response.code()}: ${response.message()}"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: OutOfMemoryError) {
            Result.failure(Exception("Out of memory: Data too large to process"))
        } catch (e: IOException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Unexpected error: ${e.message}"))
        }
    }
    // Add this method to NetworkRepository for SalesPerson data
    suspend fun fetchSalesPersons(context: Context): Result<List<SalesPerson>> = withContext(Dispatchers.IO) {
        try {
            val serverId = serverHelper.getSelectedServer(context)
            val response = apiService.getSalesPersons(serverId)

            if (response.isSuccessful) {
                val apiResponse = response.body()

                if (apiResponse?.status == "success") {
                    val data = apiResponse.data ?: emptyList()
                    Result.success(data)
                } else {
                    val errorMessage = apiResponse?.status ?: "Unknown error"
                    Result.failure(Exception("API Error: $errorMessage"))
                }
            } else {
                val errorMessage = "HTTP ${response.code()}: ${response.message()}"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: OutOfMemoryError) {
            Result.failure(Exception("Out of memory: Data too large to process"))
        } catch (e: IOException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Unexpected error: ${e.message}"))
        }
    }}