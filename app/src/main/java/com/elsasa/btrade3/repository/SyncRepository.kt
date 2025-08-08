package com.elsasa.btrade3.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncRepository(
    private val networkRepository: NetworkRepository,
    private val barangRepository: BarangRepository,
    private val customerRepository: CustomerRepository,
    private val salesPersonRepository: SalesPersonRepository

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
            val result = networkRepository.fetchBarangs()

            return@withContext result.fold(
                onSuccess = { barangs ->
                    if (barangs.isEmpty()) {
                        return@fold SyncResult.Success("No data to sync", 0)
                    }
                    barangRepository.deleteAllBarangs()
                    var insertedCount = 0

                    try {
                        barangs.chunked(BATCH_SIZE).forEachIndexed { index, batch ->
                            batch.forEach { barang ->
                                barangRepository.insertBarang(barang)
                            }
                            insertedCount += batch.size
                        }

                        val successMessage = "Successfully synced $insertedCount items"
                        SyncResult.Success(
                            message = successMessage,
                            count = insertedCount
                        )
                    } catch (e: Exception) {
                        SyncResult.Error("Insertion failed: ${e.message}")
                    }
                },
                onFailure = { exception ->
                    val errorMessage = "Sync failed: ${exception.message}"
                    SyncResult.Error(errorMessage)                }
            )
        } catch (e: Exception) {
            val errorMessage = "Sync error: ${e.message}"
            SyncResult.Error(errorMessage)
        }
    }
    suspend fun syncCustomers(): SyncResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting customer sync...")

            // Fetch data from API
            val result = networkRepository.fetchCustomers()

            return@withContext result.fold(
                onSuccess = { customers ->
                    Log.d(TAG, "Successfully fetched ${customers.size} customers from API")

                    if (customers.isEmpty()) {
                        return@fold SyncResult.Success("No customer data to sync", 0)
                    }

                    // Clear existing data
                    Log.d(TAG, "Clearing existing customer data...")
                    customerRepository.deleteAllCustomer()

                    // Insert new data in batches
                    Log.d(TAG, "Inserting ${customers.size} new customers in batches...")
                    var insertedCount = 0

                    try {
                        customers.chunked(BATCH_SIZE).forEachIndexed { index, batch ->
                            Log.d(TAG, "Inserting customer batch ${index + 1}/${(customers.size + BATCH_SIZE - 1) / BATCH_SIZE}")
                            batch.forEach { customer ->
                                customerRepository.insertCustomer(customer)
                            }
                            insertedCount += batch.size
                            Log.d(TAG, "Inserted $insertedCount customers so far...")
                        }

                        val successMessage = "Successfully synced $insertedCount customers"
                        Log.d(TAG, successMessage)
                        SyncResult.Success(
                            message = successMessage,
                            count = insertedCount
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error during customer batch insertion", e)
                        SyncResult.Error("Customer insertion failed: ${e.message}")
                    }
                },
                onFailure = { exception ->
                    val errorMessage = "Customer sync failed: ${exception.message}"
                    Log.e(TAG, errorMessage, exception)
                    SyncResult.Error(errorMessage)
                }
            )
        } catch (e: Exception) {
            val errorMessage = "Customer sync error: ${e.message}"
            Log.e(TAG, errorMessage, e)
            SyncResult.Error(errorMessage)
        }
    }

    suspend fun syncSalesPersons(): SyncResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting sales person sync...")

            // Fetch data from API
            val result = networkRepository.fetchSalesPersons()

            return@withContext result.fold(
                onSuccess = { salesPersons ->
                    Log.d(TAG, "Successfully fetched ${salesPersons.size} sales persons from API")

                    if (salesPersons.isEmpty()) {
                        return@fold SyncResult.Success("No sales person data to sync", 0)
                    }

                    // Clear existing data
                    Log.d(TAG, "Clearing existing sales person data...")
                    salesPersonRepository.deleteAllSalesPersons()

                    // Insert new data in batches
                    Log.d(TAG, "Inserting ${salesPersons.size} new sales persons in batches...")
                    var insertedCount = 0

                    try {
                        salesPersons.chunked(BATCH_SIZE).forEachIndexed { index, batch ->
                            Log.d(TAG, "Inserting sales person batch ${index + 1}/${(salesPersons.size + BATCH_SIZE - 1) / BATCH_SIZE}")
                            batch.forEach { salesPerson ->
                                salesPersonRepository.insertSalesPerson(salesPerson)
                            }
                            insertedCount += batch.size
                            Log.d(TAG, "Inserted $insertedCount sales persons so far...")
                        }

                        val successMessage = "Successfully synced $insertedCount sales persons"
                        Log.d(TAG, successMessage)
                        SyncResult.Success(
                            message = successMessage,
                            count = insertedCount
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error during sales person batch insertion", e)
                        SyncResult.Error("Sales person insertion failed: ${e.message}")
                    }
                },
                onFailure = { exception ->
                    val errorMessage = "Sales person sync failed: ${exception.message}"
                    Log.e(TAG, errorMessage, exception)
                    SyncResult.Error(errorMessage)
                }
            )
        } catch (e: Exception) {
            val errorMessage = "Sales person sync error: ${e.message}"
            Log.e(TAG, errorMessage, e)
            SyncResult.Error(errorMessage)
        }
    }
}