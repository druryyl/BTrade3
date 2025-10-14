package com.elsasa.btrade3.util

import android.content.Context
import android.location.Geocoder
import android.location.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class ReverseGeocodingHelper(private val context: Context) {

    suspend fun getAddressFromLocation(location: Location): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                1 // Get only the first address
            )

            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                buildString {
                    // Try to get the most specific address parts
                    if (address.thoroughfare != null) {
                        append(address.thoroughfare)
                        if (address.featureName != null && address.thoroughfare != address.featureName) {
                            append(", ${address.featureName}")
                        }
                    } else if (address.featureName != null) {
                        append(address.featureName)
                    } else if (address.subThoroughfare != null) {
                        append(address.subThoroughfare)
                        if (address.thoroughfare != null) {
                            append(" ${address.thoroughfare}")
                        }
                    } else {
                        // If no street info, use other available info
                        append(address.getAddressLine(0))
                    }

                    // Add city if available
                    if (address.locality != null) {
                        if (isNotEmpty()) append(", ")
                        append(address.locality)
                    }

                    // Add administrative area if available
                    if (address.adminArea != null) {
                        if (isNotEmpty()) append(", ")
                        append(address.adminArea)
                    }
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getFullAddressFromLocation(location: Location): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                1
            )

            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                address.getAddressLine(0) // Returns the full address line
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}