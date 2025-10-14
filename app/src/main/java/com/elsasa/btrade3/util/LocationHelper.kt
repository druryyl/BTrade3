package com.elsasa.btrade3.util

import androidx.annotation.RequiresPermission
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun getCurrentLocation(
        onLocationResult: (LocationResult) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (!hasLocationPermission()) {
            onError(SecurityException("Location permission not granted"))
            return
        }

        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        // Create LocationResult with the location
                        val locationResult = LocationResult.create(listOf(location))
                        onLocationResult(locationResult)
                    } else {
                        // Location not available, request new location
                        requestNewLocation(onLocationResult, onError)
                    }
                }
                .addOnFailureListener { exception ->
                    onError(exception)
                }
        } catch (e: Exception) {
            onError(e)
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun requestNewLocation(
        onLocationResult: (LocationResult) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000 // 10 seconds
            fastestInterval = 5000 // 5 seconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                onLocationResult(locationResult)
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                if (!locationAvailability.isLocationAvailable) {
                    onError(Exception("Location not available"))
                }
            }
        }

        if (hasLocationPermission()) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun stopLocationUpdates() {
        // No need to stop since we're using single location requests
    }
}