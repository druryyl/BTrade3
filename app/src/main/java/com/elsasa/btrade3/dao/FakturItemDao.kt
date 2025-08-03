package com.elsasa.btrade3.dao


import androidx.room.*
import com.elsasa.btrade3.model.FakturItem
import kotlinx.coroutines.flow.Flow

@Dao
interface FakturItemDao {
    @Query("SELECT * FROM faktur_item_table WHERE fakturId = :fakturId ORDER BY noUrut")
    fun getFakturItemsByFakturId(fakturId: String): Flow<List<FakturItem>>

    @Query("SELECT SUM(lineTotal) FROM faktur_item_table WHERE fakturId = :fakturId")
    suspend fun getTotalAmountForFaktur(fakturId: String): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFakturItem(fakturItem: FakturItem)

    @Update
    suspend fun updateFakturItem(fakturItem: FakturItem)

    @Delete
    suspend fun deleteFakturItem(fakturItem: FakturItem)

    @Query("DELETE FROM faktur_item_table WHERE fakturId = :fakturId")
    suspend fun deleteAllItemsForFaktur(fakturId: String)
}