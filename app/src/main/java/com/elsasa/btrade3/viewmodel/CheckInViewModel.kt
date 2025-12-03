package com.elsasa.btrade3.viewmodel

import com.elsasa.btrade3.model.Customer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsasa.btrade3.model.CheckIn
import com.elsasa.btrade3.repository.CheckInRepository
import com.elsasa.btrade3.repository.CustomerRepository
import com.elsasa.btrade3.util.LocationHelper
import com.elsasa.btrade3.util.LocationStatus
import com.elsasa.btrade3.util.LocationUtils
import com.elsasa.btrade3.util.ReverseGeocodingHelper
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

    private val _currentAddress = MutableStateFlow<String?>(null)
    val currentAddress: StateFlow<String?> = _currentAddress.asStateFlow()

    private val _nearbyCustomers = MutableStateFlow<List<Customer>>(emptyList())
    val nearbyCustomers: StateFlow<List<Customer>> = _nearbyCustomers.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    private val _allCustomers = MutableStateFlow<List<Customer>>(emptyList())

    private val _selectedCustomerWilayah: MutableStateFlow<String> = MutableStateFlow("")
    private val _selectedCustomerId: MutableStateFlow<String> = MutableStateFlow("")

    private val locationHelper = LocationHelper(context)
    private val reverseGeocodingHelper = ReverseGeocodingHelper(context)

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
        _currentAddress.value = null // Clear previous address

        locationHelper.getCurrentLocation(
            onLocationResult = { locationResult ->
                locationResult.lastLocation?.let { loc ->
                    _currentLocation.value = loc
                    _accuracy.value = loc.accuracy
                    _locationStatus.value = LocationStatus.LOCKED

                    // Get address for the new location
                    viewModelScope.launch {
                        val address = reverseGeocodingHelper.getAddressFromLocation(loc)
                        _currentAddress.value = address
                    }

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
            val currentLocation = _currentLocation.value

            if (currentLocation != null) {
                if (currentLocation.latitude.isNaN() || currentLocation.longitude.isNaN()) {
                    _nearbyCustomers.value = emptyList()
                    return
                }

                // Process in background to avoid blocking UI
                viewModelScope.launch {
                    // Filter customers by wilayah and exclude the selected customer
                    val customersToProcess = if (_selectedCustomerWilayah.value.isNotEmpty()) {
                        _allCustomers.value.filter { customer ->
                            customer.wilayah == _selectedCustomerWilayah.value &&
                                    customer.customerId != _selectedCustomerId.value
                        }
                    } else {
                        _allCustomers.value.filter { customer ->
                            customer.customerId != _selectedCustomerId.value
                        }
                    }

                    val nearby = LocationUtils.getCustomersWithinRadius(
                        currentLocation.latitude,
                        currentLocation.longitude,
                        customersToProcess,
                        100f // 100 meters radius
                    )

                    // Update UI on main thread
                    _nearbyCustomers.value = nearby
                }
            } else {
                _nearbyCustomers.value = emptyList()
            }
        } catch (e: Exception) {
            _nearbyCustomers.value = emptyList()
        }
    }

    fun selectCustomer(customer: Customer) {
        _selectedCustomerId.value = customer.customerId
        _selectedCustomerWilayah.value = customer.wilayah
        _selectedCustomer.value = customer
        // Update nearby customers to exclude the selected one
        updateNearbyCustomers()
    }

    fun unselectCustomer() {
        _selectedCustomer.value = null
        _selectedCustomerId.value = ""
        _selectedCustomerWilayah.value = ""
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

    fun refreshCurrentAddress() {
        val currentLoc = _currentLocation.value
        if (currentLoc != null) {
            viewModelScope.launch {
                val address = reverseGeocodingHelper.getAddressFromLocation(currentLoc)
                _currentAddress.value = address
            }
        }
    }

    fun resetLocation() {
        _currentLocation.value = null
        _accuracy.value = 0f
        _currentAddress.value = null
        _locationStatus.value = LocationStatus.NO_SIGNAL
        _selectedCustomer.value = null
        _nearbyCustomers.value = emptyList()
    }
}