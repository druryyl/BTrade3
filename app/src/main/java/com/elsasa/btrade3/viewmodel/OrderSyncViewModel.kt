package com.elsasa.btrade3.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsasa.btrade3.repository.CheckInSyncRepository
import com.elsasa.btrade3.repository.OrderSyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrderSyncViewModel(
    private val orderSyncRepository: OrderSyncRepository,
    private val checkInSyncRepository: CheckInSyncRepository
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

    // Add this new method for Check-In sync
    fun syncDraftCheckIns(userEmail: String, context: Context) {
        viewModelScope.launch {
            _syncState.value = OrderSyncRepository.SyncResult.Loading
            try {
                val result = checkInSyncRepository.syncDraftCheckIns(userEmail, context)
                _syncState.value = convertCheckInSyncResultToOrderSyncResult(result)
            } catch (e: Exception) {
                _syncState.value = OrderSyncRepository.SyncResult.Error("Check-in sync failed: ${e.message}")
            }
        }
    }

    // Add this new method for Check-In sync with progress
    fun syncDraftCheckInsWithProgress(userEmail: String, context: Context) {
        viewModelScope.launch {
            _syncState.value = OrderSyncRepository.SyncResult.Loading
            try {
                val result = checkInSyncRepository.syncDraftCheckInsWithProgress(
                    userEmail = userEmail,
                    onProgress = { progress ->
                    _syncState.value = OrderSyncRepository.SyncResult.Progress(
                        current = progress.current,
                        total = progress.total,
                        orderCode = progress.customerName)},
                    context = context);
                _syncState.value = convertCheckInSyncResultToOrderSyncResult(result)
            } catch (e: Exception) {
                _syncState.value = OrderSyncRepository.SyncResult.Error("Check-in sync failed: ${e.message}")
            }
        }
    }
    private fun convertCheckInSyncResultToOrderSyncResult(
        checkInResult: CheckInSyncRepository.SyncResult
    ): OrderSyncRepository.SyncResult {
        return when (checkInResult) {
            is CheckInSyncRepository.SyncResult.Success ->
                OrderSyncRepository.SyncResult.Success(checkInResult.message, checkInResult.count)
            is CheckInSyncRepository.SyncResult.Error ->
                OrderSyncRepository.SyncResult.Error(checkInResult.message)
            is CheckInSyncRepository.SyncResult.Progress ->
                OrderSyncRepository.SyncResult.Progress(checkInResult.current, checkInResult.total, checkInResult.customerName)
            is CheckInSyncRepository.SyncResult.Loading ->
                OrderSyncRepository.SyncResult.Loading
        }
    }
}