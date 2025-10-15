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
                Log.e("LocationUtils", "Invalid coordinates for distance calculation: $lat1, $lng1, $lat2, $lng2")
                return Float.MAX_VALUE // Return a large value to indicate invalid coordinates
            }

            // Validate coordinate ranges
            if (lat1 < -90 || lat1 > 90 || lat2 < -90 || lat2 > 90 ||
                lng1 < -180 || lng1 > 180 || lng2 < -180 || lng2 > 180) {
                Log.e("LocationUtils", "Coordinates out of range: $lat1, $lng1, $lat2, $lng2")
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
            Log.e("LocationUtils", "Error calculating distance", e)
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
                Log.e("LocationUtils", "Invalid target coordinates: $targetLat, $targetLng")
                return emptyList()
            }

            // Filter out customers without valid location data first
            val customersWithValidLocation = allCustomers.filter { customer ->
                val hasValidLocation = customer.latitude != 0.0 &&
                        customer.longitude != 0.0 &&
                        !customer.latitude.isNaN() &&
                        !customer.longitude.isNaN()

                if (!hasValidLocation) {
                    Log.w("LocationUtils", "Skipping customer ${customer.customerName} - invalid location data: ${customer.latitude}, ${customer.longitude}")
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
                    Log.e("LocationUtils", "Error calculating distance for customer ${customer.customerName}", e)
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
                    Log.e("LocationUtils", "Error sorting customer ${customer.customerName}", e)
                    Float.MAX_VALUE
                }
            }
        } catch (e: Exception) {
            Log.e("LocationUtils", "Error getting customers within radius", e)
            emptyList()
        }
    }
}