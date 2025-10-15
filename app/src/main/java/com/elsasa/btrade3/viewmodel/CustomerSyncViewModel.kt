package com.elsasa.btrade3.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsasa.btrade3.repository.CustomerSyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CustomerSyncViewModel(
    private val customerSyncRepository: CustomerSyncRepository
) : ViewModel() {

    private val _syncState = MutableStateFlow<CustomerSyncRepository.SyncResult>(
        CustomerSyncRepository.SyncResult.Success("Ready to sync", 0)
    )
    val syncState: StateFlow<CustomerSyncRepository.SyncResult> = _syncState.asStateFlow()

    fun syncCustomerLocations(userEmail: String) {
        viewModelScope.launch {
            _syncState.value = CustomerSyncRepository.SyncResult.Loading
            try {
                _syncState.value = customerSyncRepository.syncUpdatedCustomersWithProgress(userEmail) { progress ->
                    _syncState.value = progress
                }
            } catch (e: Exception) {
                _syncState.value = CustomerSyncRepository.SyncResult.Error("Sync failed: ${e.message}")
            }
        }
    }
}