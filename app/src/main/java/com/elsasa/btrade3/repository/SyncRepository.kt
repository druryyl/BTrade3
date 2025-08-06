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
        private const val BATCH_SIZE = 100 // Process in batches of 100
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

            return@withContext result.fold(
                onSuccess = { barangs ->
                    Log.d(TAG, "Successfully fetched ${barangs.size} barangs from API")

                    if (barangs.isEmpty()) {
                        return@fold SyncResult.Success("No data to sync", 0)
                    }

                    // Clear existing data
                    Log.d(TAG, "Clearing existing barang data...")
                    barangRepository.deleteAllBarangs()

                    // Insert new data
                    Log.d(TAG, "Inserting ${barangs.size} new barangs...")
                    var insertedCount = 0

                    try {
                        barangs.chunked(BATCH_SIZE).forEachIndexed { index, batch ->
                            Log.d(TAG, "Inserting batch ${index + 1}/${(barangs.size + BATCH_SIZE - 1) / BATCH_SIZE}")
                            batch.forEach { barang ->
                                barangRepository.insertBarang(barang)
                            }
                            insertedCount += batch.size
                            Log.d(TAG, "Inserted $insertedCount items so far...")
                        }

                        val successMessage = "Successfully synced $insertedCount items"
                        Log.d(TAG, successMessage)
                        SyncResult.Success(
                            message = successMessage,
                            count = insertedCount
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error during batch insertion", e)
                        SyncResult.Error("Insertion failed: ${e.message}")
                    }
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