package com.elsasa.btrade3.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.elsasa.btrade3.model.SalesPerson
import kotlinx.coroutines.flow.Flow

@Dao
interface SalesPersonDao {
    @Query("SELECT * FROM salesperson_table ORDER BY salesPersonName")
    fun getAllSalesPersons(): Flow<List<SalesPerson>>

    @Query("SELECT * FROM salesperson_table WHERE salesPersonId = :salesPersonId")
    suspend fun getSalesPersonById(salesPersonId: String): SalesPerson?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalesPerson(salesPerson: SalesPerson)

    @Update
    suspend fun updateSalesPerson(salesPerson: SalesPerson)

    @Delete
    suspend fun deleteSalesPerson(salesPerson: SalesPerson)

    @Query("DELETE FROM salesperson_table")
    suspend fun deleteAllSalesPersons()
}