package com.elsasa.btrade3.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncRepository(
    private val networkRepository: NetworkRepository,
    private val barangRepository: BarangRepository
) {
    companion object {
        private const val TAG = "SyncRepository"
    }
    sealed class SyncResult {
        data class Success(val message: String, val count: Int) : SyncResult()
        data class Error(val message: String) : SyncResult()
        object Loading : SyncResult()
    }

    suspend fun syncBarangs(): SyncResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting barang sync...")
            val result = networkRepository.fetchBarangs()

            result.fold(
                onSuccess = { barangs ->
                    Log.d(TAG, "Successfully fetched ${barangs.size} barangs from API")
                    Log.d(TAG, "Clearing existing barang data...")
                    barangRepository.deleteAllBarangs()

                    // Insert new data
                    Log.d(TAG, "Inserting ${barangs.size} new barangs...")
                    barangs.forEach { barang ->
                        barangRepository.insertBarang(barang)
                    }

                    val successMessage = "Successfully synced ${barangs.size} items"
                    Log.d(TAG, successMessage)
                    SyncResult.Success(
                        message = successMessage,
                        count = barangs.size
                    )
                },
                onFailure = { exception ->
                    val errorMessage = "Sync failed: ${exception.message}"
                    Log.e(TAG, errorMessage, exception)
                    SyncResult.Error(errorMessage)                }
            )
        } catch (e: Exception) {
            val errorMessage = "Sync error: ${e.message}"
            Log.e(TAG, errorMessage, e)
            SyncResult.Error(errorMessage)
        }
    }
}