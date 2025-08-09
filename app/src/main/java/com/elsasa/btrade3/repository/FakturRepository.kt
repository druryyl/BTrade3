package com.elsasa.btrade3.repository

import com.elsasa.btrade3.dao.FakturDao
import com.elsasa.btrade3.dao.FakturItemDao
import com.elsasa.btrade3.model.Faktur
import com.elsasa.btrade3.model.FakturItem
import kotlinx.coroutines.flow.Flow

class FakturRepository(
    private val fakturDao: FakturDao,
    private val fakturItemDao: FakturItemDao,
) {
    fun getAllFakturs(): Flow<List<Faktur>> = fakturDao.getAllFakturs()
    suspend fun getFakturById(fakturId: String): Faktur? = fakturDao.getFakturById(fakturId)

    suspend fun insertFaktur(faktur: Faktur) = fakturDao.insertFaktur(faktur)

    suspend fun updateFaktur(faktur: Faktur) = fakturDao.updateFaktur(faktur)

    suspend fun deleteFaktur(faktur: Faktur) = fakturDao.deleteFaktur(faktur)

    fun getFakturItemsByFakturId(fakturId: String): Flow<List<FakturItem>> =
        fakturItemDao.getFakturItemsByFakturId(fakturId)

    suspend fun insertFakturItem(fakturItem: FakturItem) = fakturItemDao.insertFakturItem(fakturItem)

    suspend fun updateFakturItem(fakturItem: FakturItem) = fakturItemDao.updateFakturItem(fakturItem)

    suspend fun deleteFakturItem(fakturItem: FakturItem) = fakturItemDao.deleteFakturItem(fakturItem)

    suspend fun calculateTotalAmount(fakturId: String): Double {
        return fakturItemDao.getTotalAmountForFaktur(fakturId) ?: 0.0
    }

    suspend fun deleteAllItemsForFaktur(fakturId: String) =
        fakturItemDao.deleteAllItemsForFaktur(fakturId)
}