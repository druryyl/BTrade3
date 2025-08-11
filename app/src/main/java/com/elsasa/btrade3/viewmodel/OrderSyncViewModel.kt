package com.elsasa.btrade3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsasa.btrade3.repository.OrderSyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrderSyncViewModel(
    private val orderSyncRepository: OrderSyncRepository
) : ViewModel() {

    private val _syncState = MutableStateFlow<OrderSyncRepository.SyncResult>(OrderSyncRepository.SyncResult.Success("Ready to sync", 0))
    val syncState: StateFlow<OrderSyncRepository.SyncResult> = _syncState.asStateFlow()

    fun syncDraftOrders() {
        viewModelScope.launch {
            _syncState.value = OrderSyncRepository.SyncResult.Loading
            try {
                _syncState.value = orderSyncRepository.syncDraftOrders()
            } catch (e: Exception) {
                _syncState.value = OrderSyncRepository.SyncResult.Error("Sync failed: ${e.message}")
            }
        }
    }

    fun syncDraftOrdersWithProgress() {
        viewModelScope.launch {
            _syncState.value = OrderSyncRepository.SyncResult.Loading
            try {
                _syncState.value = orderSyncRepository.syncDraftOrdersWithProgress { progress ->
                    _syncState.value = progress
                }
            } catch (e: Exception) {
                _syncState.value = OrderSyncRepository.SyncResult.Error("Sync failed: ${e.message}")
            }
        }
    }
}