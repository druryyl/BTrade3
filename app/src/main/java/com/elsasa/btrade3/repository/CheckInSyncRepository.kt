package com.elsasa.btrade3.repository

import android.util.Log
import com.elsasa.btrade3.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

class CheckInSyncRepository(
    private val apiService: ApiService,
    private val checkInRepository: CheckInRepository
) {
    companion object {
        private const val TAG = "CheckInSyncRepository"
    }

    sealed class SyncResult {
        data class Success(val message: String, val count: Int) : SyncResult()
        data class Error(val message: String) : SyncResult()
        data class Progress(val current: Int, val total: Int, val customerName: String) : SyncResult()
        object Loading : SyncResult()
    }

    suspend fun syncDraftCheckIns(userEmail: String): SyncResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting draft check-ins sync...")

            // Get all draft check-ins
            val draftCheckIns = checkInRepository.getDraftCheckIns().firstOrNull() ?: emptyList()

            if (draftCheckIns.isEmpty()) {
                return@withContext SyncResult.Success("No draft check-ins to sync", 0)
            }

            val totalCheckIns = draftCheckIns.size
            var syncedCount = 0

            // Sync check-ins one by one
            draftCheckIns.forEachIndexed { index, checkIn ->
                Log.d(TAG, "Syncing check-in ${index + 1}/$totalCheckIns: ${checkIn.customerName}")

                try {
                    val response = apiService.syncCheckIn(checkIn)

                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse?.status == "success") {
                            // Update check-in status to "SENT"
                            val updatedCheckIn = checkIn.copy(statusSync = "SENT")
                            checkInRepository.updateCheckIn(updatedCheckIn)
                            syncedCount++
                            Log.d(TAG, "Successfully synced check-in: ${checkIn.customerName}")
                        } else {
                            val errorMessage = apiResponse?.data ?: "Unknown error"
                            Log.e(TAG, "API error for check-in ${checkIn.customerName}: $errorMessage")
                        }
                    } else {
                        Log.e(TAG, "HTTP error for check-in ${checkIn.customerName}: ${response.code()} ${response.message()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing check-in ${checkIn.customerName}", e)
                }
            }

            SyncResult.Success(
                message = "Successfully synced $syncedCount of $totalCheckIns check-ins",
                count = syncedCount
            )
        } catch (e: Exception) {
            Log.e(TAG, "Check-in sync error", e)
            SyncResult.Error("Check-in sync failed: ${e.message}")
        }
    }

    // Bulk sync with progress tracking
    suspend fun syncDraftCheckInsWithProgress(
        userEmail: String,
        onProgress: (SyncResult.Progress) -> Unit
    ): SyncResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting draft check-ins sync with progress...")

            // Get all draft check-ins
            val draftCheckIns = checkInRepository.getDraftCheckIns().firstOrNull() ?: emptyList()

            if (draftCheckIns.isEmpty()) {
                return@withContext SyncResult.Success("No draft check-ins to sync", 0)
            }

            val totalCheckIns = draftCheckIns.size
            var syncedCount = 0

            // Sync check-ins with progress updates
            draftCheckIns.forEachIndexed { index, checkIn ->
                onProgress(SyncResult.Progress(
                    current = index + 1,
                    total = totalCheckIns,
                    customerName = checkIn.customerName
                ))

                try {
                    val response = apiService.syncCheckIn(checkIn)

                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse?.status == "success") {
                            // Update check-in status to "SENT"
                            val updatedCheckIn = checkIn.copy(statusSync = "SENT")
                            checkInRepository.updateCheckIn(updatedCheckIn)
                            syncedCount++
                            Log.d(TAG, "Successfully synced check-in: ${checkIn.customerName}")
                        } else {
                            val errorMessage = apiResponse?.data ?: "Unknown error"
                            Log.e(TAG, "API error for check-in ${checkIn.customerName}: $errorMessage")
                        }
                    } else {
                        Log.e(TAG, "HTTP error for check-in ${checkIn.customerName}: ${response.code()} ${response.message()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing check-in ${checkIn.customerName}", e)
                }
            }

            SyncResult.Success(
                message = "Successfully synced $syncedCount of $totalCheckIns check-ins",
                count = syncedCount
            )
        } catch (e: Exception) {
            Log.e(TAG, "Check-in sync error", e)
            SyncResult.Error("Check-in sync failed: ${e.message}")
        }
    }
}