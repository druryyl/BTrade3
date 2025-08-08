package com.elsasa.btrade3.repository

import com.elsasa.btrade3.dao.CustomerDao
import com.elsasa.btrade3.model.Customer
import kotlinx.coroutines.flow.Flow

class CustomerRepository (
    private val customerDao: CustomerDao
){
    fun getAllCustomer(): Flow<List<Customer>> = customerDao.getAllCustomer()
    suspend fun getCustomerById(brgId: String): Customer? = customerDao.getCustomerById(brgId)
    suspend fun insertCustomer(customer: Customer) = customerDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: Customer) = customerDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) = customerDao.deleteCustomer(customer)
    suspend fun deleteAllCustomer() = customerDao.deleteAllCustomer()
}