package com.elsasa.btrade3.dao


import androidx.room.*
import com.elsasa.btrade3.model.OrderItem
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderItemDao {
    @Query("SELECT * FROM order_item_table WHERE orderId = :orderId ORDER BY noUrut")
    fun getOrderItemsByOrderId(orderId: String): Flow<List<OrderItem>>

    @Query("SELECT SUM(lineTotal) FROM order_item_table WHERE orderId = :orderId")
    suspend fun getTotalAmountForOrder(orderId: String): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItem(orderItem: OrderItem)

    @Update
    suspend fun updateOrderItem(orderItem: OrderItem)

    @Delete
    suspend fun deleteOrderItem(orderItem: OrderItem)

    @Query("DELETE FROM order_item_table WHERE orderId = :orderId")
    suspend fun deleteAllItemsForOrder(orderId: String)
}