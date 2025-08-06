package com.elsasa.btrade3.repository

import com.elsasa.btrade3.dao.BarangDao
import com.elsasa.btrade3.model.Barang
import kotlinx.coroutines.flow.Flow

class BarangRepository (
    private val barangDao: BarangDao
){
    fun getAllBarangs(): Flow<List<Barang>> = barangDao.getAllBarangs()
    suspend fun getBarangById(brgId: String): Barang? = barangDao.getBarangById(brgId)
    suspend fun insertBarang(barang: Barang) = barangDao.insertBarang(barang)
    suspend fun updateBarang(barang: Barang) = barangDao.updateBarang(barang)
    suspend fun deleteBarang(barang: Barang) = barangDao.deleteBarang(barang)
    suspend fun deleteAllBarangs() = barangDao.deleteAllBarangs()
}