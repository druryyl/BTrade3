package com.elsasa.btrade3.dao

import androidx.room.*
import com.elsasa.btrade3.model.Order
import com.elsasa.btrade3.model.OrderSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM order_table ORDER BY orderLocalId DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM order_table WHERE orderId = :orderId")
    suspend fun getOrderById(orderId: String): Order?

    @Query("SELECT * FROM order_table WHERE statusSync = 'DRAFT'")
    fun getDraftOrders(): Flow<List<Order>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)

    @Update
    suspend fun updateOrder(order: Order)

    @Delete
    suspend fun deleteOrder(order: Order)

    // Add this new query for order summary
    @Query("""
        SELECT userEmail, orderDate, COUNT(orderId) as orderCount, SUM(totalAmount) as grossSales
        FROM order_table
        GROUP BY userEmail, orderDate
        ORDER BY orderDate DESC, userEmail
    """)
    fun getOrderSummary(): Flow<List<OrderSummary>>

    // Optional: Filter by date range
    @Query("""
        SELECT userEmail, orderDate, COUNT(orderId) as orderCount, SUM(totalAmount) as grossSales
        FROM order_table
        WHERE orderDate BETWEEN :startDate AND :endDate
        GROUP BY userEmail, orderDate
        ORDER BY orderDate DESC, userEmail
    """)
    fun getOrderSummaryByDateRange(startDate: String, endDate: String): Flow<List<OrderSummary>>
}