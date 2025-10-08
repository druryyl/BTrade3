package com.elsasa.btrade3.viewmodel

import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elsasa.btrade3.model.CustomerLocation
import com.elsasa.btrade3.repository.CustomerLocationRepository
import com.elsasa.btrade3.service.LocationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class CustomerLocationViewModel(
    application: Application,
    private val customerLocationRepository: CustomerLocationRepository
) : AndroidViewModel(application) {

    private val locationService = LocationService(application)

    private val _uiState = MutableStateFlow<LocationUiState>(LocationUiState.Idle)
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    private val _customerLocation = MutableStateFlow<CustomerLocation?>(null)
    val customerLocation: StateFlow<CustomerLocation?> = _customerLocation.asStateFlow()

    sealed class LocationUiState {
        object Idle : LocationUiState()
        object Loading : LocationUiState()
        data class Success(val message: String) : LocationUiState()
        data class Error(val message: String) : LocationUiState()
        data class PermissionRequired(val message: String) : LocationUiState()
    }

    fun loadCustomerLocation(customerId: String) {
        viewModelScope.launch {
            try {
                val location = customerLocationRepository.getLocationByCustomerId(customerId)
                _customerLocation.value = location
            } catch (e: Exception) {
                _uiState.value = LocationUiState.Error("Failed to load location: ${e.message}")
            }
        }
    }

    fun getCurrentLocationAndSave(customerId: String, customerName: String) {
        viewModelScope.launch {
            _uiState.value = LocationUiState.Loading

            locationService.getCurrentLocation()
                .onSuccess { locationData ->
                    try {
                        // Reverse geocode to get address
                        val address = reverseGeocode(locationData.latitude, locationData.longitude)

                        // Create customer location
                        val customerLocation = CustomerLocation(
                            customerId = customerId,
                            latitude = locationData.latitude,
                            longitude = locationData.longitude,
                            accuracy = locationData.accuracy,
                            address = address,
                            timestamp = System.currentTimeMillis()
                        )

                        // Save to database
                        customerLocationRepository.saveLocation(customerLocation)
                        _customerLocation.value = customerLocation

                        _uiState.value = LocationUiState.Success(
                            "Location saved successfully for $customerName"
                        )
                    } catch (e: Exception) {
                        _uiState.value = LocationUiState.Error("Failed to save location: ${e.message}")
                    }
                }
                .onFailure { exception ->
                    when (exception.message) {
                        "Location services are disabled" -> {
                            _uiState.value = LocationUiState.Error(
                                "Please enable location services in your device settings"
                            )
                        }
                        else -> {
                            _uiState.value = LocationUiState.Error(
                                "Failed to get location: ${exception.message}"
                            )
                        }
                    }
                }
        }
    }

    fun deleteCustomerLocation(customerId: String) {
        viewModelScope.launch {
            try {
                customerLocationRepository.deleteLocation(customerId)
                _customerLocation.value = null
                _uiState.value = LocationUiState.Success("Location removed successfully")
            } catch (e: Exception) {
                _uiState.value = LocationUiState.Error("Failed to remove location: ${e.message}")
            }
        }
    }

    private fun reverseGeocode(latitude: Double, longitude: Double): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // For API 33+, use the callback-based approach
                // For simplicity, we'll return coordinates
                "Lat: $latitude, Lng: $longitude"
            } else {
                val geocoder = Geocoder(getApplication())
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    buildString {
                        address.thoroughfare?.let { append("$it ") }
                        address.subThoroughfare?.let { append("$it, ") }
                        address.locality?.let { append("$it, ") }
                        address.adminArea?.let { append("$it ") }
                        address.postalCode?.let { append("$it") }
                    }.trim().ifEmpty { "Lat: $latitude, Lng: $longitude" }
                } else {
                    "Lat: $latitude, Lng: $longitude"
                }
            }
        } catch (e: Exception) {
            "Lat: $latitude, Lng: $longitude"
        }
    }
}