package com.elsasa.btrade3.util

import android.location.Location
import android.util.Log

object LocationUtils {
    /**
     * Calculate distance between two coordinates in meters
     */
    fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        try {
            // Validate coordinates
            if (lat1.isNaN() || lng1.isNaN() || lat2.isNaN() || lng2.isNaN()) {
                return Float.MAX_VALUE // Return a large value to indicate invalid coordinates
            }

            // Validate coordinate ranges
            if (lat1 < -90 || lat1 > 90 || lat2 < -90 || lat2 > 90 ||
                lng1 < -180 || lng1 > 180 || lng2 < -180 || lng2 > 180) {
                return Float.MAX_VALUE
            }

            val location1 = Location("point1").apply {
                latitude = lat1
                longitude = lng1
            }

            val location2 = Location("point2").apply {
                latitude = lat2
                longitude = lng2
            }

            return location1.distanceTo(location2)
        } catch (e: Exception) {
            return Float.MAX_VALUE
        }
    }

    /**
     * Get customers within specified radius from a given coordinate
     */
    fun getCustomersWithinRadius(
        targetLat: Double,
        targetLng: Double,
        allCustomers: List<com.elsasa.btrade3.model.Customer>,
        radiusMeters: Float = 100f
    ): List<com.elsasa.btrade3.model.Customer> {
        return try {
            // Validate target coordinates
            if (targetLat.isNaN() || targetLng.isNaN()) {
                return emptyList()
            }

            // Filter out customers without valid location data first
            val customersWithValidLocation = allCustomers.filter { customer ->
                val hasValidLocation = customer.latitude != 0.0 &&
                        customer.longitude != 0.0 &&
                        !customer.latitude.isNaN() &&
                        !customer.longitude.isNaN()

                if (!hasValidLocation) {
                }
                hasValidLocation
            }

            // Filter customers within radius and handle calculation errors
            val customersWithinRadius = customersWithValidLocation.filter { customer ->
                try {
                    val distance = calculateDistance(
                        targetLat, targetLng,
                        customer.latitude, customer.longitude
                    )
                    distance <= radiusMeters && distance != Float.MAX_VALUE
                } catch (e: Exception) {
                    false
                }
            }

            // Sort by distance with error handling
            customersWithinRadius.sortedBy { customer ->
                try {
                    calculateDistance(
                        targetLat, targetLng,
                        customer.latitude, customer.longitude
                    )
                } catch (e: Exception) {
                    Float.MAX_VALUE
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}