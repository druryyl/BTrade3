package com.elsasa.btrade3.repository

import com.elsasa.btrade3.dao.CustomerLocationDao
import com.elsasa.btrade3.model.CustomerLocation

class CustomerLocationRepository(
    private val customerLocationDao: CustomerLocationDao
) {
    suspend fun getLocationByCustomerId(customerId: String): CustomerLocation? {
        return customerLocationDao.getLocationByCustomerId(customerId)
    }

    suspend fun saveLocation(location: CustomerLocation) {
        customerLocationDao.insertLocation(location)
    }

    suspend fun updateLocation(location: CustomerLocation) {
        customerLocationDao.updateLocation(location)
    }

    suspend fun deleteLocation(customerId: String) {
        customerLocationDao.deleteLocationByCustomerId(customerId)
    }
}