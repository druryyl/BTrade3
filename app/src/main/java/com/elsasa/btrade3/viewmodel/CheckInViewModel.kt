package com.elsasa.btrade3.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsasa.btrade3.model.CheckIn
import com.elsasa.btrade3.model.Customer
import com.elsasa.btrade3.repository.CheckInRepository
import com.elsasa.btrade3.repository.CustomerRepository
import com.elsasa.btrade3.util.LocationHelper
import com.elsasa.btrade3.util.LocationStatus
import com.elsasa.btrade3.util.LocationUtils
import com.elsasa.btrade3.util.UlidHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.flow.first

class CheckInViewModel(
    private val context: Context,
    private val checkInRepository: CheckInRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _locationStatus = MutableStateFlow(LocationStatus.NO_SIGNAL)
    val locationStatus: StateFlow<LocationStatus> = _locationStatus.asStateFlow()

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _accuracy = MutableStateFlow(0f)
    val accuracy: StateFlow<Float> = _accuracy.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _nearbyCustomers = MutableStateFlow<List<Customer>>(emptyList())
    val nearbyCustomers: StateFlow<List<Customer>> = _nearbyCustomers.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    private val _allCustomers = MutableStateFlow<List<Customer>>(emptyList())

    private val locationHelper = LocationHelper(context)

    fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun loadNearbyCustomers() {
        viewModelScope.launch {
            _allCustomers.value = customerRepository.getAllCustomer().first()
            updateNearbyCustomers()
        }
    }

    fun startLocationCapture() {
        if (!checkLocationPermission()) {
            _locationStatus.value = LocationStatus.NO_PERMISSION
            return
        }

        _isLoading.value = true
        _locationStatus.value = LocationStatus.ACQUIRING

        locationHelper.getCurrentLocation(
            onLocationResult = { locationResult ->
                locationResult.lastLocation?.let { loc ->
                    _currentLocation.value = loc
                    _accuracy.value = loc.accuracy
                    _locationStatus.value = LocationStatus.LOCKED
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

    private fun updateNearbyCustomers() {
        try {
            val currentLocation = _currentLocation.value

            if (currentLocation != null) {
                if (currentLocation.latitude.isNaN() || currentLocation.longitude.isNaN()) {
                    _nearbyCustomers.value = emptyList()
                    return
                }

                // Filter customers within 100 meters and exclude the selected customer
                val nearby = LocationUtils.getCustomersWithinRadius(
                    currentLocation.latitude,
                    currentLocation.longitude,
                    _allCustomers.value.filter { customer ->
                        customer.customerId != _selectedCustomer.value?.customerId // Exclude selected customer
                    },
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

    fun selectCustomer(customer: Customer) {
        _selectedCustomer.value = customer
        // Update nearby customers to exclude the selected one
        updateNearbyCustomers()
    }

    fun unselectCustomer() {
        _selectedCustomer.value = null
        updateNearbyCustomers()
    }

    fun checkIn(userEmail: String) {
        val currentLoc = _currentLocation.value
        val selectedCust = _selectedCustomer.value

        if (currentLoc != null && selectedCust != null) {
            viewModelScope.launch {
                val checkIn = CheckIn(
                    checkInId = UlidHelper.generate(),
                    checkInDate = SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                    ).format(Date()),
                    checkInTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()),
                    userEmail = userEmail,
                    checkInLatitude = currentLoc.latitude,
                    checkInLongitude = currentLoc.longitude,
                    accuracy = currentLoc.accuracy,
                    customerId = selectedCust.customerId,
                    customerCode = selectedCust.customerCode,
                    customerName = selectedCust.customerName,
                    customerAddress = selectedCust.alamat,
                    customerLatitude = selectedCust.latitude,
                    customerLongitude = selectedCust.longitude,
                    statusSync = "DRAFT"
                )

                checkInRepository.insertCheckIn(checkIn)
                // Reset selection after successful check-in
                _selectedCustomer.value = null
            }
        }
    }

    fun resetLocation() {
        _currentLocation.value = null
        _accuracy.value = 0f
        _locationStatus.value = LocationStatus.NO_SIGNAL
        _selectedCustomer.value = null
        _nearbyCustomers.value = emptyList()
    }
}