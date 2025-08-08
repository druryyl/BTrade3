package com.elsasa.btrade3.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.elsasa.btrade3.model.Customer
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customer_table ORDER BY customerName")
    fun getAllCustomer(): Flow<List<Customer>>

    @Query("SELECT * FROM customer_table WHERE customerId = :customerId")
    suspend fun getCustomerById(customerId: String): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)
    @Query("DELETE FROM customer_table")
    suspend fun deleteAllCustomer()
}