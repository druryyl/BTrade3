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

    // Updated query to include totalItems using subquery
    @Query("""
        SELECT 
            o.userEmail,
            o.orderDate,
            COUNT(o.orderId) as orderCount,
            SUM(o.totalAmount) as grossSales,
            COALESCE(SUM(item_counts.itemCount), 0) as totalItems
        FROM order_table o
        LEFT JOIN (
            SELECT 
                orderId,
                COUNT(*) as itemCount
            FROM order_item_table
            GROUP BY orderId
        ) item_counts ON o.orderId = item_counts.orderId
        GROUP BY o.userEmail, o.orderDate
        ORDER BY o.orderDate DESC, o.userEmail
    """)
    fun getOrderSummary(): Flow<List<OrderSummary>>

    // Updated query with date range filter
    @Query("""
        SELECT 
            o.userEmail,
            o.orderDate,
            COUNT(o.orderId) as orderCount,
            SUM(o.totalAmount) as grossSales,
            COALESCE(SUM(item_counts.itemCount), 0) as totalItems
        FROM order_table o
        LEFT JOIN (
            SELECT 
                orderId,
                COUNT(*) as itemCount
            FROM order_item_table
            GROUP BY orderId
        ) item_counts ON o.orderId = item_counts.orderId
        WHERE o.orderDate BETWEEN :startDate AND :endDate
        GROUP BY o.userEmail, o.orderDate
        ORDER BY o.orderDate DESC, o.userEmail
    """)
    fun getOrderSummaryByDateRange(startDate: String, endDate: String): Flow<List<OrderSummary>>
}