package com.elsasa.btrade3.repository

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
            val response = apiService.getBarangs()

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
}