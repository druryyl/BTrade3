package com.elsasa.btrade3.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.elsasa.btrade3.model.CustomerLocation

@Dao
interface CustomerLocationDao {
    @Query("SELECT * FROM customer_location_table WHERE customerId = :customerId")
    suspend fun getLocationByCustomerId(customerId: String): CustomerLocation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: CustomerLocation)

    @Update
    suspend fun updateLocation(location: CustomerLocation)

    @Delete
    suspend fun deleteLocation(location: CustomerLocation)

    @Query("DELETE FROM customer_location_table WHERE customerId = :customerId")
    suspend fun deleteLocationByCustomerId(customerId: String)
}