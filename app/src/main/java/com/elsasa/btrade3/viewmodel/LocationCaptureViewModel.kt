package com.elsasa.btrade3.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsasa.btrade3.model.Customer
import com.elsasa.btrade3.repository.CustomerRepository
import com.elsasa.btrade3.repository.CustomerSyncRepository
import com.elsasa.btrade3.util.LocationHelper
import com.elsasa.btrade3.util.LocationStatus
import com.elsasa.btrade3.util.LocationUtils
import com.elsasa.btrade3.util.ReverseGeocodingHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LocationCaptureViewModel(
    private val context: Context,
    private val customerRepository: CustomerRepository,
    private val customerSyncRepository: CustomerSyncRepository // Add this dependency

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

    private val _nearbyCustomers = MutableStateFlow<List<Customer>>(emptyList())
    val nearbyCustomers: StateFlow<List<Customer>> = _nearbyCustomers.asStateFlow()

    private val _allCustomers = MutableStateFlow<List<Customer>>(emptyList())
    private var _selectedCustomerWilayah: String = "" // Store the selected customer's wilayah
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
            try {
                customerRepository.getCustomerById(customerId)?.let { customer ->
                    _selectedCustomerWilayah = customer.wilayah // Store the wilayah

                    if (customer.latitude != 0.0 && customer.longitude != 0.0 &&
                        !customer.latitude.isNaN() && !customer.longitude.isNaN()) {
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
                        try {
                            val address = reverseGeocodingHelper.getAddressFromLocation(originalLocation)
                            _address.value = address
                        } catch (e: Exception) {
                            _address.value = null
                        }
                    } else {
                        _locationStatus.value = LocationStatus.NO_SIGNAL
                        _address.value = null
                    }
                }

                // Load customers filtered by wilayah for nearby search (more efficient)
                val customersForWilayah = if (_selectedCustomerWilayah.isNotEmpty()) {
                    customerRepository.getAllCustomer().first().filter { customer ->
                        customer.wilayah == _selectedCustomerWilayah
                    }
                } else {
                    customerRepository.getAllCustomer().first()
                }

                _allCustomers.value = customersForWilayah
                updateNearbyCustomers()
            } catch (e: Exception) {
                _locationStatus.value = LocationStatus.NO_SIGNAL
                _address.value = null
                _nearbyCustomers.value = emptyList()
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

                    // Validate the new location before using it
                    if (loc.latitude.isNaN() || loc.longitude.isNaN()) {
                        _locationStatus.value = LocationStatus.NO_SIGNAL
                        _isLoading.value = false
                        return@let
                    }

                    _location.value = loc
                    _accuracy.value = loc.accuracy
                    _locationStatus.value = LocationStatus.LOCKED

                    // Get address for the new location - wrap in try-catch
                    viewModelScope.launch {
                        try {
                            val address = reverseGeocodingHelper.getAddressFromLocation(loc)
                            _address.value = address
                        } catch (e: Exception) {
                            _address.value = null
                        }
                    }

                    // Update nearby customers based on new location
                    updateNearbyCustomers()
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

    // Update nearby customers based on current location
    private fun updateNearbyCustomers() {
        try {
            val currentLocation = _location.value

            if (currentLocation != null) {
                // Validate coordinates before calculating distances
                if (currentLocation.latitude.isNaN() || currentLocation.longitude.isNaN()) {
                    _nearbyCustomers.value = emptyList()
                    return
                }

                // Only process customers from the same wilayah (much more efficient)
                val customersToProcess = if (_selectedCustomerWilayah.isNotEmpty()) {
                    _allCustomers.value.filter { customer ->
                        customer.wilayah == _selectedCustomerWilayah
                    }
                } else {
                    _allCustomers.value
                }

                val nearby = LocationUtils.getCustomersWithinRadius(
                    currentLocation.latitude,
                    currentLocation.longitude,
                    _allCustomers.value,
                    100f // 100 meters radius
                )

                _nearbyCustomers.value = nearby
            } else {
                _nearbyCustomers.value = emptyList()
            }
        } catch (e: Exception) {
            _nearbyCustomers.value = emptyList()
        }
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

    // Save location and sync to cloud
    fun saveLocationForCustomer(customerId: String, userEmail: String) {
        val currentLocation = _location.value
        if (currentLocation != null) {
            viewModelScope.launch {
                // Update customer location in local database with isUpdated = true
                customerRepository.updateCustomerLocation(
                    customerId = customerId,
                    latitude = currentLocation.latitude,
                    longitude = currentLocation.longitude,
                    accuracy = currentLocation.accuracy,
                    timestamp = System.currentTimeMillis(),
                    isUpdated = true  // Set to true when location is updated
                )

                // Sync the updated location to cloud
                try {
                    val syncResult = customerSyncRepository.syncUpdatedCustomers(userEmail)
                    // Handle sync result if needed (show toast, log, etc.)
                } catch (e: Exception) {
                    // Handle sync error - location is still saved locally
                    // You might want to show an error message to the user
                }
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
        updateNearbyCustomers()
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
            _nearbyCustomers.value = emptyList()
        }
    }
}