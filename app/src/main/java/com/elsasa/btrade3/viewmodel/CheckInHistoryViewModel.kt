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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
class CheckInHistoryViewModel(
    private val checkInRepository: CheckInRepository
) : ViewModel() {

    private val _checkIns = MutableStateFlow<List<CheckIn>>(emptyList())
    val checkIns: StateFlow<List<CheckIn>> = _checkIns.asStateFlow()

    private val _filteredCheckIns = MutableStateFlow<List<CheckIn>>(emptyList())
    val filteredCheckIns: StateFlow<List<CheckIn>> = _filteredCheckIns.asStateFlow()

    private val _selectedDate = MutableStateFlow<String>("")
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _checkInCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val checkInCounts: StateFlow<Map<String, Int>> = _checkInCounts.asStateFlow()

    fun loadCheckIns() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                checkInRepository.getAllCheckIns().collect { checkIns ->
                    _checkIns.value = checkIns.sortedByDescending {
                        "${it.checkInDate}${it.checkInTime}" // Sort by date and time, descending
                    }
                    updateCheckInCounts(_checkIns.value)
                    updateFilteredCheckIns()
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _checkIns.value = emptyList()
                _filteredCheckIns.value = emptyList()
                _isLoading.value = false
            }
        }
    }

    private fun updateCheckInCounts(allCheckIns: List<CheckIn>) {
        val counts = allCheckIns.groupingBy { it.checkInDate }.eachCount()
        _checkInCounts.value = counts
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
        updateFilteredCheckIns()
    }

    private fun updateFilteredCheckIns() {
        val allCheckIns = _checkIns.value
        val selectedDate = _selectedDate.value

        val filtered = if (selectedDate.isNotEmpty()) {
            allCheckIns.filter { it.checkInDate == selectedDate }
        } else {
            allCheckIns
        }

        _filteredCheckIns.value = filtered.sortedByDescending { it.checkInTime }
    }

    fun navigateToDate(direction: Int) { // 1 for next day, -1 for previous day
        val currentDateString = _selectedDate.value
        if (currentDateString.isEmpty()) return

        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currentDate = sdf.parse(currentDateString)
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            calendar.add(Calendar.DAY_OF_MONTH, direction)
            val newDate = sdf.format(calendar.time)
            _selectedDate.value = newDate
            updateFilteredCheckIns()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadDraftCheckIns() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                checkInRepository.getDraftCheckIns().collect { checkIns ->
                    _checkIns.value = checkIns.sortedByDescending {
                        "${it.checkInDate}${it.checkInTime}"
                    }
                    updateCheckInCounts(_checkIns.value)
                    updateFilteredCheckIns()
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _checkIns.value = emptyList()
                _filteredCheckIns.value = emptyList()
                _isLoading.value = false
            }
        }
    }

    fun deleteCheckIn(checkInId: String) {
        viewModelScope.launch {
            try {
                checkInRepository.deleteCheckInById(checkInId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteCheckIn(checkIn: CheckIn) {
        viewModelScope.launch {
            try {
                checkInRepository.deleteCheckIn(checkIn)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}