package com.elsasa.btrade3.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsasa.btrade3.repository.SyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SyncViewModel(
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _syncState = MutableStateFlow<SyncRepository.SyncResult>(SyncRepository.SyncResult.Loading)
    val syncState: StateFlow<SyncRepository.SyncResult> = _syncState.asStateFlow()

    fun syncBarangs(context: Context) {
        viewModelScope.launch {
            _syncState.value = SyncRepository.SyncResult.Loading
            try {
                _syncState.value = syncRepository.syncBarangs(context)
            } catch (e: Exception) {
                _syncState.value = SyncRepository.SyncResult.Error("Sync failed: ${e.message}")
            }
        }
    }

    fun syncCustomers() {
        viewModelScope.launch {
            _syncState.value = SyncRepository.SyncResult.Loading
            try {
                _syncState.value = syncRepository.syncCustomers()
            } catch (e: Exception) {
                _syncState.value = SyncRepository.SyncResult.Error("Sync failed: ${e.message}")
            }
        }
    }

    fun syncSalesPersons() {
        viewModelScope.launch {
            _syncState.value = SyncRepository.SyncResult.Loading
            try {
                _syncState.value = syncRepository.syncSalesPersons()
            } catch (e: Exception) {
                _syncState.value = SyncRepository.SyncResult.Error("Sync failed: ${e.message}")
            }
        }
    }


    // Initialize with a ready state so it's not stuck in loading
    init {
        _syncState.value = SyncRepository.SyncResult.Success("Ready to sync", 0)
    }
}