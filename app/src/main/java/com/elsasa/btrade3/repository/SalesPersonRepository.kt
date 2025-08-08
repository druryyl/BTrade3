package com.elsasa.btrade3.repository


import com.elsasa.btrade3.dao.SalesPersonDao
import com.elsasa.btrade3.model.SalesPerson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class SalesPersonRepository(
    private val salesPersonDao: SalesPersonDao
) {
    fun getAllSalesPersons(): Flow<List<SalesPerson>> = salesPersonDao.getAllSalesPersons()

    suspend fun getSalesPersonById(salesPersonId: String): SalesPerson? = salesPersonDao.getSalesPersonById(salesPersonId)

    suspend fun insertSalesPerson(salesPerson: SalesPerson) = salesPersonDao.insertSalesPerson(salesPerson)

    suspend fun updateSalesPerson(salesPerson: SalesPerson) = salesPersonDao.updateSalesPerson(salesPerson)

    suspend fun deleteSalesPerson(salesPerson: SalesPerson) = salesPersonDao.deleteSalesPerson(salesPerson)

    suspend fun deleteAllSalesPersons() = salesPersonDao.deleteAllSalesPersons()

}