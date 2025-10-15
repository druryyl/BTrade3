package com.elsasa.btrade3.repository


import android.util.Log
import com.elsasa.btrade3.model.api.CustomerSyncRequest
import com.elsasa.btrade3.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class CustomerSyncRepository(
    private val apiService: ApiService,
    private val customerRepository: CustomerRepository
) {
    companion object {
        private const val TAG = "CustomerSyncRepository"
    }

    sealed class SyncResult {
        data class Success(val message: String, val count: Int) : SyncResult()
        data class Error(val message: String) : SyncResult()
        data class Progress(val current: Int, val total: Int, val customerName: String) : SyncResult()
        object Loading : SyncResult()
    }

    suspend fun syncUpdatedCustomers(userEmail: String): SyncResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting customer location sync...")

            // Get all customers with isUpdated = true
            val updatedCustomers = customerRepository.getAllCustomer().first()
                .filter { it.isUpdated }

            if (updatedCustomers.isEmpty()) {
                return@withContext SyncResult.Success("No updated customers to sync", 0)
            }

            val totalCustomers = updatedCustomers.size
            var syncedCount = 0

            // Sync each customer location
            updatedCustomers.forEachIndexed { index, customer ->
                Log.d(TAG, "Syncing customer ${index + 1}/$totalCustomers: ${customer.customerName}")

                // Update progress (if you want to show progress)
                // This would need to be handled differently in practice

                try {
                    val syncRequest = CustomerSyncRequest(
                        customerId = customer.customerId,
                        latitude = customer.latitude,
                        longitude = customer.longitude,
                        accuracy = customer.accuracy,
                        coordinateTimeStamp = customer.locationTimestamp,
                        coordinateUser = userEmail
                    )

                    val response = apiService.syncCustomerLocation(syncRequest)

                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse?.status == "success") {
                            // Update customer's isUpdated flag to false
                            val updatedCustomer = customer.copy(isUpdated = false)
                            customerRepository.updateCustomer(updatedCustomer)
                            syncedCount++
                            Log.d(TAG, "Successfully synced customer: ${customer.customerName}")
                        } else {
                            val errorMessage = apiResponse?.message ?: "Unknown error"
                            Log.e(TAG, "API error for customer ${customer.customerName}: $errorMessage")
                        }
                    } else {
                        Log.e(TAG, "HTTP error for customer ${customer.customerName}: ${response.code()} ${response.message()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing customer ${customer.customerName}", e)
                }
            }

            SyncResult.Success(
                message = "Successfully synced $syncedCount of $totalCustomers customer locations",
                count = syncedCount
            )
        } catch (e: Exception) {
            Log.e(TAG, "Customer sync error", e)
            SyncResult.Error("Customer sync failed: ${e.message}")
        }
    }

    // Bulk sync with progress tracking
    suspend fun syncUpdatedCustomersWithProgress(
        userEmail: String,
        onProgress: (SyncResult.Progress) -> Unit
    ): SyncResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting customer location sync with progress...")

            // Get all customers with isUpdated = true
            val updatedCustomers = customerRepository.getAllCustomer().first()
                .filter { it.isUpdated }

            if (updatedCustomers.isEmpty()) {
                return@withContext SyncResult.Success("No updated customers to sync", 0)
            }

            val totalCustomers = updatedCustomers.size
            var syncedCount = 0

            // Sync customers with progress updates
            updatedCustomers.forEachIndexed { index, customer ->
                onProgress(SyncResult.Progress(
                    current = index + 1,
                    total = totalCustomers,
                    customerName = customer.customerName
                ))

                try {
                    val syncRequest = CustomerSyncRequest(
                        customerId = customer.customerId,
                        latitude = customer.latitude,
                        longitude = customer.longitude,
                        accuracy = customer.accuracy,
                        coordinateTimeStamp = customer.locationTimestamp,
                        coordinateUser = userEmail
                    )

                    val response = apiService.syncCustomerLocation(syncRequest)

                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse?.status == "success") {
                            // Update customer's isUpdated flag to false
                            val updatedCustomer = customer.copy(isUpdated = false)
                            customerRepository.updateCustomer(updatedCustomer)
                            syncedCount++
                            Log.d(TAG, "Successfully synced customer: ${customer.customerName}")
                        } else {
                            val errorMessage = apiResponse?.message ?: "Unknown error"
                            Log.e(TAG, "API error for customer ${customer.customerName}: $errorMessage")
                        }
                    } else {
                        Log.e(TAG, "HTTP error for customer ${customer.customerName}: ${response.code()} ${response.message()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing customer ${customer.customerName}", e)
                }
            }

            SyncResult.Success(
                message = "Successfully synced $syncedCount of $totalCustomers customer locations",
                count = syncedCount
            )
        } catch (e: Exception) {
            Log.e(TAG, "Customer sync error", e)
            SyncResult.Error("Customer sync failed: ${e.message}")
        }
    }
}