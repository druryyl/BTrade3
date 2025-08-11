package com.elsasa.btrade3.dao

import androidx.room.*
import com.elsasa.btrade3.model.Order
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM order_table ORDER BY orderDate DESC")
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
}