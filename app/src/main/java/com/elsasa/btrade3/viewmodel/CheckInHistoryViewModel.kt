package com.elsasa.btrade3.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsasa.btrade3.model.CheckIn
import com.elsasa.btrade3.repository.CheckInRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CheckInHistoryViewModel(
    private val checkInRepository: CheckInRepository
) : ViewModel() {

    private val _checkIns = MutableStateFlow<List<CheckIn>>(emptyList())
    val checkIns: StateFlow<List<CheckIn>> = _checkIns.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadCheckIns() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                checkInRepository.getAllCheckIns().collect { checkIns ->
                    _checkIns.value = checkIns.sortedByDescending {
                        "${it.checkInDate}${it.checkInTime}" // Sort by date and time, descending
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _checkIns.value = emptyList()
                _isLoading.value = false
            }
        }
    }

    fun loadCheckInsSimple() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Get the first snapshot of data
                val initialData = checkInRepository.getAllCheckIns().first()
                _checkIns.value = initialData.sortedByDescending {
                    "${it.checkInDate}${it.checkInTime}"
                }
                _isLoading.value = false

                // Then listen for updates
                checkInRepository.getAllCheckIns().drop(1).collect { updatedData ->
                    _checkIns.value = updatedData.sortedByDescending {
                        "${it.checkInDate}${it.checkInTime}"
                    }
                }
            } catch (e: Exception) {
                _checkIns.value = emptyList()
                _isLoading.value = false
            }
        }
    }

    fun loadDraftCheckIns() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val initialData = checkInRepository.getDraftCheckIns().first()
                _checkIns.value = initialData.sortedByDescending {
                    "${it.checkInDate}${it.checkInTime}"
                }
                _isLoading.value = false

                // Listen for updates
                checkInRepository.getDraftCheckIns().drop(1).collect { updatedData ->
                    _checkIns.value = updatedData.sortedByDescending {
                        "${it.checkInDate}${it.checkInTime}"
                    }
                }
            } catch (e: Exception) {
                _checkIns.value = emptyList()
                _isLoading.value = false
            }
        }
    }

    // Add delete functionality
    fun deleteCheckIn(checkInId: String) {
        viewModelScope.launch {
            try {
                checkInRepository.deleteCheckInById(checkInId)
                // The repository flow will automatically update the list
            } catch (e: Exception) {
                // Handle deletion error if needed
                e.printStackTrace()
            }
        }
    }

    // Alternative method if you want to pass the whole CheckIn object
    fun deleteCheckIn(checkIn: CheckIn) {
        viewModelScope.launch {
            try {
                checkInRepository.deleteCheckIn(checkIn)
                // The repository flow will automatically update the list
            } catch (e: Exception) {
                // Handle deletion error if needed
                e.printStackTrace()
            }
        }
    }
}