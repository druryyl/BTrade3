package com.elsasa.btrade3.repository

import com.elsasa.btrade3.dao.CheckInDao
import com.elsasa.btrade3.model.CheckIn
import kotlinx.coroutines.flow.Flow

class CheckInRepository(
    private val checkInDao: CheckInDao
) {
    fun getAllCheckIns(): Flow<List<CheckIn>> = checkInDao.getAllCheckIns()

    fun getDraftCheckIns(): Flow<List<CheckIn>> = checkInDao.getDraftCheckIns()

    suspend fun getCheckInById(checkInId: String): CheckIn? = checkInDao.getCheckInById(checkInId)

    suspend fun insertCheckIn(checkIn: CheckIn) = checkInDao.insertCheckIn(checkIn)

    suspend fun updateCheckIn(checkIn: CheckIn) = checkInDao.updateCheckIn(checkIn)

    suspend fun deleteCheckIn(checkIn: CheckIn) = checkInDao.deleteCheckIn(checkIn)

    suspend fun deleteCheckInById(checkInId: String) = checkInDao.deleteCheckInById(checkInId)
}