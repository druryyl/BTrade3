package com.elsasa.btrade3.repository


import android.content.Context
import android.util.Log
import com.elsasa.btrade3.model.api.CustomerSyncRequest
import com.elsasa.btrade3.network.ApiService
import com.elsasa.btrade3.util.ServerHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class CustomerSyncRepository(
    private val apiService: ApiService,
    private val customerRepository: CustomerRepository,
    private val serverHelper: ServerHelper // Add ServerHelper dependency

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

    suspend fun syncUpdatedCustomers(userEmail: String, context: Context): SyncResult = withContext(Dispatchers.IO) {
        try {
            val serverId = serverHelper.getSelectedServer(context)
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
                // Update progress (if you want to show progress)
                // This would need to be handled differently in practice
                try {
                    val syncRequest = CustomerSyncRequest(
                        customerId = customer.customerId,
                        latitude = customer.latitude,
                        longitude = customer.longitude,
                        accuracy = customer.accuracy,
                        coordinateTimeStamp = customer.locationTimestamp,
                        coordinateUser = userEmail,
                        serverId = serverId
                    )

                    val response = apiService.syncCustomerLocation(syncRequest)

                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse?.status == "success") {
                            // Update customer's isUpdated flag to false
                            val updatedCustomer = customer.copy(isUpdated = false)
                            customerRepository.updateCustomer(updatedCustomer)
                            syncedCount++
                        } else {
                            val errorMessage = apiResponse?.message ?: "Unknown error"
                        }
                    } else {
                    }
                } catch (e: Exception) {
                }
            }

            SyncResult.Success(
                message = "Successfully synced $syncedCount of $totalCustomers customer locations",
                count = syncedCount
            )
        } catch (e: Exception) {
            SyncResult.Error("Customer sync failed: ${e.message}")
        }
    }

    // Bulk sync with progress tracking
    suspend fun syncUpdatedCustomersWithProgress(
        userEmail: String,
        onProgress: (SyncResult.Progress) -> Unit,
        context: Context
    ): SyncResult = withContext(Dispatchers.IO) {
        try {
            val serverId = serverHelper.getSelectedServer(context)
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
                        coordinateUser = userEmail,
                        serverId = serverId
                    )

                    val response = apiService.syncCustomerLocation(syncRequest)

                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse?.status == "success") {
                            // Update customer's isUpdated flag to false
                            val updatedCustomer = customer.copy(isUpdated = false)
                            customerRepository.updateCustomer(updatedCustomer)
                            syncedCount++
                        } else {
                            val errorMessage = apiResponse?.message ?: "Unknown error"
                        }
                    } else {
                    }
                } catch (e: Exception) {
                }
            }

            SyncResult.Success(
                message = "Successfully synced $syncedCount of $totalCustomers customer locations",
                count = syncedCount
            )
        } catch (e: Exception) {
            SyncResult.Error("Customer sync failed: ${e.message}")
        }
    }
}