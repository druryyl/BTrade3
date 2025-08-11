package com.elsasa.btrade3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elsasa.btrade3.repository.OrderSyncRepository

class OrderSyncViewModelFactory(
    private val orderSyncRepository: OrderSyncRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderSyncViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrderSyncViewModel(orderSyncRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}