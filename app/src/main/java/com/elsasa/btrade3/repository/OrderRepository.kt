package com.elsasa.btrade3.repository

import com.elsasa.btrade3.dao.OrderDao
import com.elsasa.btrade3.dao.OrderItemDao
import com.elsasa.btrade3.model.Order
import com.elsasa.btrade3.model.OrderItem
import kotlinx.coroutines.flow.Flow

class OrderRepository(
    private val orderDao: OrderDao,
    private val orderItemDao: OrderItemDao,
) {
    fun getAllOrders(): Flow<List<Order>> = orderDao.getAllOrders()
    fun getDraftOrders(): Flow<List<Order>> = orderDao.getDraftOrders()
    suspend fun getOrderById(fakturId: String): Order? = orderDao.getOrderById(fakturId)


    suspend fun insertOrder(order: Order) = orderDao.insertOrder(order)

    suspend fun updateOrder(order: Order) = orderDao.updateOrder(order)

    suspend fun deleteOrder(order: Order) = orderDao.deleteOrder(order)

    fun getOrderItemsByOrderId(orderId: String): Flow<List<OrderItem>> =
        orderItemDao.getOrderItemsByOrderId(orderId)

    suspend fun insertOrderItem(orderItem: OrderItem) = orderItemDao.insertOrderItem(orderItem)

    suspend fun updateOrderItem(orderItem: OrderItem) = orderItemDao.updateOrderItem(orderItem)

    suspend fun deleteOrderItem(orderItem: OrderItem) = orderItemDao.deleteOrderItem(orderItem)

    suspend fun calculateTotalAmount(fakturId: String): Double {
        return orderItemDao.getTotalAmountForOrder(fakturId) ?: 0.0
    }

    suspend fun deleteAllItemsForOrder(fakturId: String) =
        orderItemDao.deleteAllItemsForOrder(fakturId)

    // New method to update order sync status
    suspend fun updateOrderSyncStatus(orderId: String, status: String, fakturCode: String = "") {
        getOrderById(orderId)?.let { order ->
            val updatedOrder = if (fakturCode.isNotEmpty()) {
                order.copy(statusSync = status, fakturCode = fakturCode)
            } else {
                order.copy(statusSync = status)
            }
            updateOrder(updatedOrder)
        }
    }
}