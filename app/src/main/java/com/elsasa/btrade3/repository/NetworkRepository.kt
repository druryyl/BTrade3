package com.elsasa.btrade3.repository

import android.util.Log
import com.elsasa.btrade3.model.Barang
import com.elsasa.btrade3.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class NetworkRepository(
    private val apiService: ApiService
) {
    companion object {
        private const val TAG = "NetworkRepository"
    }

    suspend fun fetchBarangs(): Result<List<Barang>> = withContext(Dispatchers.IO) {
        try {
            //Log.d(TAG, "Fetching barangs from API...")
            val response = apiService.getBarangs()
            //Log.d(TAG, "API Response received. Successful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val apiResponse = response.body()
                //Log.d(TAG, "API Response body: $apiResponse")

                if (apiResponse?.status == "success") {
                    Log.d(TAG, "API returned success, data size: ${apiResponse.data?.size}")
                    Result.success(apiResponse.data?: emptyList())
                } else {
                    val errorMessage = apiResponse?.status ?: "Unknown error"
                    Log.e(TAG, "API returned error: $errorMessage")
                    Result.failure(Exception("API Error: $errorMessage"))
                }
            } else {
                val errorMessage = "HTTP ${response.code()}: ${response.message()}"
                Log.e(TAG, "HTTP Error: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error", e)
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error", e)
            Result.failure(e)
        }
    }
}