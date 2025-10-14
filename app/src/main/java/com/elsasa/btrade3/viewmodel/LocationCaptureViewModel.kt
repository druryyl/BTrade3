package com.elsasa.btrade3.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsasa.btrade3.model.Customer
import com.elsasa.btrade3.repository.CustomerRepository
import com.elsasa.btrade3.util.LocationHelper
import com.elsasa.btrade3.util.LocationStatus
import com.elsasa.btrade3.util.ReverseGeocodingHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocationCaptureViewModel(
    private val context: Context,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _locationStatus = MutableStateFlow(LocationStatus.NO_SIGNAL)
    val locationStatus: StateFlow<LocationStatus> = _locationStatus.asStateFlow()

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location.asStateFlow()

    private val _accuracy = MutableStateFlow(0f)
    val accuracy: StateFlow<Float> = _accuracy.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _address = MutableStateFlow<String?>(null)
    val address: StateFlow<String?> = _address.asStateFlow()


    private val _originalLocation = MutableStateFlow<Location?>(null) // Store original location
    val originalLocation: StateFlow<Location?> = _originalLocation.asStateFlow()

    private val locationHelper = LocationHelper(context)
    private val reverseGeocodingHelper = ReverseGeocodingHelper(context)

    fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    // Load existing customer location
    fun loadCustomerLocation(customerId: String) {
        viewModelScope.launch {
            customerRepository.getCustomerById(customerId)?.let { customer ->
                if (customer.latitude != 0.0 && customer.longitude != 0.0) {
                    val originalLocation = Location("stored").apply {
                        latitude = customer.latitude
                        longitude = customer.longitude
                        accuracy = customer.accuracy
                    }
                    _originalLocation.value = originalLocation
                    _location.value = originalLocation
                    _accuracy.value = customer.accuracy
                    _locationStatus.value = LocationStatus.LOCKED

                    // Get address for the stored location
                    val address = reverseGeocodingHelper.getAddressFromLocation(originalLocation)
                    _address.value = address
                } else {
                    _locationStatus.value = LocationStatus.NO_SIGNAL
                    _address.value = null
                }
            }
        }
    }

    // Start GPS location capture
    fun startLocationCapture() {
        if (!checkLocationPermission()) {
            _locationStatus.value = LocationStatus.NO_PERMISSION
            return
        }

        _isLoading.value = true
        _locationStatus.value = LocationStatus.ACQUIRING
        _address.value = null // Clear previous address

        locationHelper.getCurrentLocation(
            onLocationResult = { locationResult ->
                locationResult.lastLocation?.let { loc ->
                    _location.value = loc
                    _accuracy.value = loc.accuracy
                    _locationStatus.value = LocationStatus.LOCKED

                    // Get address for the new location
                    viewModelScope.launch {
                        val address = reverseGeocodingHelper.getAddressFromLocation(loc)
                        _address.value = address
                    }
                } ?: run {
                    _locationStatus.value = LocationStatus.NO_SIGNAL
                }
                _isLoading.value = false
            },
            onError = { exception ->
                _locationStatus.value = LocationStatus.NO_SIGNAL
                _isLoading.value = false
            }
        )
    }

    // Use the current displayed location to get address
    fun refreshAddress() {
        val currentLocation = _location.value
        if (currentLocation != null) {
            viewModelScope.launch {
                val address = reverseGeocodingHelper.getAddressFromLocation(currentLocation)
                _address.value = address
            }
        }
    }

    fun saveLocationForCustomer(customerId: String) {
        val currentLocation = _location.value
        if (currentLocation != null) {
            viewModelScope.launch {
                customerRepository.updateCustomerLocation(
                    customerId = customerId,
                    latitude = currentLocation.latitude,
                    longitude = currentLocation.longitude,
                    accuracy = currentLocation.accuracy,
                    timestamp = System.currentTimeMillis()
                )
            }
        }
    }


    fun resetToOriginalLocation() {
        _location.value = _originalLocation.value
        _accuracy.value = _originalLocation.value?.accuracy ?: 0f
        if (_originalLocation.value != null) {
            _locationStatus.value = LocationStatus.LOCKED
            // Refresh address for original location
            refreshAddress()
        } else {
            _locationStatus.value = LocationStatus.NO_SIGNAL
            _address.value = null
        }
    }

    fun clearLocation(customerId: String) {
        viewModelScope.launch {
            customerRepository.updateCustomerLocation(
                customerId = customerId,
                latitude = 0.0,
                longitude = 0.0,
                accuracy = 0f,
                timestamp = 0L
            )
            _location.value = null
            _originalLocation.value = null
            _accuracy.value = 0f
            _address.value = null
            _locationStatus.value = LocationStatus.NO_SIGNAL
        }
    }
}