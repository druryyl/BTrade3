package com.elsasa.btrade3.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.elsasa.btrade3.model.Barang
import kotlinx.coroutines.flow.Flow

@Dao
interface BarangDao {
    @Query("SELECT * FROM barang_table ORDER BY brgName")
    fun getAllBarangs(): Flow<List<Barang>>

    @Query("SELECT * FROM barang_table WHERE brgId = :brgId")
    suspend fun getBarangById(brgId: String): Barang?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBarang(barang: Barang)

    @Update
    suspend fun updateBarang(barang: Barang)

    @Delete
    suspend fun deleteBarang(barang: Barang)
    @Query("DELETE FROM barang_table")
    suspend fun deleteAllBarangs()
}