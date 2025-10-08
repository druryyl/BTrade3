package com.elsasa.btrade3.service


import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.tasks.await

class LocationService(private val context: Context) {
    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private val locationRequest by lazy {
        LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000 // 10 seconds
        ).apply {
            setMinUpdateDistanceMeters(10f)
            setMaxUpdateDelayMillis(30000) // 30 seconds
        }.build()
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Result<LocationData> {
        return try {
            // Check if location services are enabled
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                return Result.failure(Exception("Location services are disabled"))
            }

            // Get last known location first
            val lastLocation = fusedLocationClient.lastLocation.await()
            if (lastLocation != null) {
                return Result.success(
                    LocationData(
                        latitude = lastLocation.latitude,
                        longitude = lastLocation.longitude,
                        accuracy = lastLocation.accuracy
                    )
                )
            }

            // If no last location, request current location
            val locationResult = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).await()

            Result.success(
                LocationData(
                    latitude = locationResult.latitude,
                    longitude = locationResult.longitude,
                    accuracy = locationResult.accuracy
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    data class LocationData(
        val latitude: Double,
        val longitude: Double,
        val accuracy: Float
    )
}