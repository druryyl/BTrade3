package com.elsasa.btrade3.dao

import androidx.room.*
import com.elsasa.btrade3.model.Faktur
import kotlinx.coroutines.flow.Flow

@Dao
interface FakturDao {
    @Query("SELECT * FROM faktur_table ORDER BY fakturDate DESC")
    fun getAllFakturs(): Flow<List<Faktur>>

    @Query("SELECT * FROM faktur_table WHERE fakturId = :fakturId")
    suspend fun getFakturById(fakturId: String): Faktur?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaktur(faktur: Faktur)

    @Update
    suspend fun updateFaktur(faktur: Faktur)

    @Delete
    suspend fun deleteFaktur(faktur: Faktur)
}