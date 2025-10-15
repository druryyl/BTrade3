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
            // Fetch data from API
            val result = networkRepository.fetchCustomers()
            return@withContext result.fold(
                onSuccess = { customers ->
                    if (customers.isEmpty()) {
                        return@fold SyncResult.Success("No customer data to sync", 0)
                    }

                    // Clear existing data
                    customerRepository.deleteAllCustomer()

                    // Insert new data in batches
                    var insertedCount = 0

                    try {
                        customers.chunked(BATCH_SIZE).forEachIndexed { index, batch ->
                            batch.forEach { customer ->
                                customerRepository.insertCustomer(customer)
                            }
                            insertedCount += batch.size
                        }

                        val successMessage = "Successfully synced $insertedCount customers"
                        SyncResult.Success(
                            message = successMessage,
                            count = insertedCount
                        )
                    } catch (e: Exception) {
                        SyncResult.Error("Customer insertion failed: ${e.message}")
                    }
                },
                onFailure = { exception ->
                    val errorMessage = "Customer sync failed: ${exception.message}"
                    SyncResult.Error(errorMessage)
                }
            )
        } catch (e: Exception) {
            val errorMessage = "Customer sync error: ${e.message}"
            SyncResult.Error(errorMessage)
        }
    }

    suspend fun syncSalesPersons(): SyncResult = withContext(Dispatchers.IO) {
        try {
            // Fetch data from API
            val result = networkRepository.fetchSalesPersons()

            return@withContext result.fold(
                onSuccess = { salesPersons ->
                    if (salesPersons.isEmpty()) {
                        return@fold SyncResult.Success("No sales person data to sync", 0)
                    }

                    // Clear existing data
                    salesPersonRepository.deleteAllSalesPersons()

                    // Insert new data in batches
                    var insertedCount = 0

                    try {
                        salesPersons.chunked(BATCH_SIZE).forEachIndexed { index, batch ->
                            batch.forEach { salesPerson ->
                                salesPersonRepository.insertSalesPerson(salesPerson)
                            }
                            insertedCount += batch.size
                        }

                        val successMessage = "Successfully synced $insertedCount sales persons"
                        SyncResult.Success(
                            message = successMessage,
                            count = insertedCount
                        )
                    } catch (e: Exception) {
                        SyncResult.Error("Sales person insertion failed: ${e.message}")
                    }
                },
                onFailure = { exception ->
                    val errorMessage = "Sales person sync failed: ${exception.message}"
                    SyncResult.Error(errorMessage)
                }
            )
        } catch (e: Exception) {
            val errorMessage = "Sales person sync error: ${e.message}"
            SyncResult.Error(errorMessage)
        }
    }
}