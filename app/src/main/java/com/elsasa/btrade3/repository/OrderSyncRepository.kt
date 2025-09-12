package com.elsasa.btrade3.repository

import android.util.Log
import com.elsasa.btrade3.model.Order
import com.elsasa.btrade3.model.api.OrderItemSyncDto
import com.elsasa.btrade3.model.api.OrderSyncRequest
import com.elsasa.btrade3.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class OrderSyncRepository(
    private val apiService: ApiService,
    private val orderRepository: OrderRepository
) {
    companion object {
        private const val TAG = "OrderSyncRepository"
    }

    sealed class SyncResult {
        data class Success(val message: String, val count: Int) : SyncResult()
        data class Error(val message: String) : SyncResult()
        data class Progress(val current: Int, val total: Int, val orderCode: String) : SyncResult()
        object Loading : SyncResult()
    }

    suspend fun syncDraftOrders(): SyncResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting draft orders sync...")

            // Get all draft orders
            val draftOrders = orderRepository.getDraftOrders().firstOrNull() ?: emptyList()

            if (draftOrders.isEmpty()) {
                return@withContext SyncResult.Success("No draft orders to sync", 0)
            }

            val totalOrders = draftOrders.size
            var syncedCount = 0

            // Sync orders one by one
            draftOrders.forEachIndexed { index, order ->
                Log.d(TAG, "Syncing order ${index + 1}/$totalOrders: ${order.orderLocalId}")

                try {
                    // Get order items
                    val orderItems = orderRepository.getOrderItemsByOrderId(order.orderId).firstOrNull() ?: emptyList()

                    // Convert to sync DTO
                    val orderItemDtos = orderItems.map { item ->
                        OrderItemSyncDto(
                            orderId = item.orderId,
                            noUrut = item.noUrut,
                            brgId = item.brgId,
                            brgCode = item.brgCode,
                            brgName = item.brgName,
                            kategoriName = item.kategoriName,
                            qtyBesar = item.qtyBesar,
                            satBesar = item.satBesar,
                            qtyKecil = item.qtyKecil,
                            satKecil = item.satKecil,
                            qtyBonus = item.qtyBonus,
                            konversi = item.konversi,
                            unitPrice = item.unitPrice,
                            disc1 = item.disc1,
                            disc2 = item.disc2,
                            disc3 = item.disc3,
                            disc4 = item.disc4,
                            lineTotal = item.lineTotal
                        )
                    }

                    // Create sync request
                    val syncRequest = OrderSyncRequest(
                        orderId = order.orderId,
                        orderLocalId = order.orderId,
                        customerId = order.customerId,
                        customerCode = order.customerCode,
                        customerName = order.customerName,
                        address = order.customerAddress,
                        orderDate = order.orderDate,
                        salesId = order.salesId,
                        salesName = order.salesName,
                        totalAmount = order.totalAmount,
                        userEmail = order.userEmail,
                        listItem = orderItemDtos,
                        orderNote = order.orderNote
                    )

                    // Send to API
                    val response = apiService.syncOrder(syncRequest)

                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse?.status == "success") {
                            // Update order status to SENT
                            // Since there's no fakturCode in response, we'll use a default or leave it as is
                            orderRepository.updateOrderSyncStatus(
                                orderId = order.orderId,
                                status = "SENT"
                                // No fakturCode since it's not in the response
                            )
                            syncedCount++
                            Log.d(TAG, "Successfully synced order: ${order.orderLocalId}")
                        } else {
                            val errorMessage = apiResponse?.message ?: "API returned error status"
                            Log.e(TAG, "API error for order ${order.orderLocalId}: $errorMessage")
                        }
                    } else {
                        Log.e(TAG, "HTTP error for order ${order.orderLocalId}: ${response.code()} ${response.message()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing order ${order.orderLocalId}", e)
                }
            }

            SyncResult.Success(
                message = "Successfully synced $syncedCount of $totalOrders orders",
                count = syncedCount
            )
        } catch (e: Exception) {
            Log.e(TAG, "Sync error", e)
            SyncResult.Error("Sync failed: ${e.message}")
        }
    }

    // Bulk sync with progress tracking
    suspend fun syncDraftOrdersWithProgress(
        onProgress: (SyncResult.Progress) -> Unit
    ): SyncResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting draft orders sync with progress...")

            // Get all draft orders
            val draftOrders = orderRepository.getDraftOrders().firstOrNull() ?: emptyList()

            if (draftOrders.isEmpty()) {
                return@withContext SyncResult.Success("No draft orders to sync", 0)
            }

            val totalOrders = draftOrders.size
            var syncedCount = 0

            // Sync orders concurrently
            val syncResults = draftOrders.mapIndexed { index, order ->
                async {
                    onProgress(SyncResult.Progress(
                        current = index + 1,
                        total = totalOrders,
                        orderCode = order.orderLocalId
                    ))

                    syncSingleOrder(order)
                }
            }.awaitAll()

            syncedCount = syncResults.count { it }

            SyncResult.Success(
                message = "Successfully synced $syncedCount of $totalOrders orders",
                count = syncedCount
            )
        } catch (e: Exception) {
            Log.e(TAG, "Sync error", e)
            SyncResult.Error("Sync failed: ${e.message}")
        }
    }
    private suspend fun syncSingleOrder(order: Order): Boolean {
        return try {
            // Get order items
            val orderItems = orderRepository.getOrderItemsByOrderId(order.orderId).firstOrNull() ?: emptyList()

            // Convert to sync DTO
            val orderItemDtos = orderItems.map { item ->
                OrderItemSyncDto(
                    orderId = item.orderId,
                    noUrut = item.noUrut,
                    brgId = item.brgId,
                    brgCode = item.brgCode,
                    brgName = item.brgName,
                    kategoriName = item.kategoriName,
                    qtyBesar = item.qtyBesar,
                    satBesar = item.satBesar,
                    qtyKecil = item.qtyKecil,
                    satKecil = item.satKecil,
                    qtyBonus = item.qtyBonus,
                    konversi = item.konversi,
                    unitPrice = item.unitPrice,
                    disc1 = item.disc1,
                    disc2 = item.disc2,
                    disc3 = item.disc3,
                    disc4 = item.disc4,
                    lineTotal = item.lineTotal
                )
            }

            // Create sync request
            val syncRequest = OrderSyncRequest(
                orderId = order.orderId,
                orderLocalId = order.orderLocalId,
                customerId = order.customerId,
                customerCode = order.customerCode,
                customerName = order.customerName,
                address = order.customerAddress,
                orderDate = order.orderDate,
                salesId = order.salesId,
                salesName = order.salesName,
                totalAmount = order.totalAmount,
                userEmail = order.userEmail,
                orderNote = order.orderNote,
                listItem = orderItemDtos
            )

            // Send to API
            val response = apiService.syncOrder(syncRequest)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.status == "success") {
                    // Update order status to SENT
                    orderRepository.updateOrderSyncStatus(
                        orderId = order.orderId,
                        status = "SENT"
                    )
                    Log.d(TAG, "Successfully synced order: ${order.orderLocalId}")
                    true
                } else {
                    val errorMessage = apiResponse?.message ?: "API returned error status"
                    Log.e(TAG, "API error for order ${order.orderLocalId}: $errorMessage")
                    false
                }
            } else {
                Log.e(TAG, "HTTP error for order ${order.orderLocalId}: ${response.code()} ${response.message()}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing order ${order.orderLocalId}", e)
            false
        }
    }
}