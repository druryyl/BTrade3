package com.elsasa.btrade3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elsasa.btrade3.repository.OrderRepository

class AddBarangViewModelFactory(
    private val orderRepository: OrderRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddBarangViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddBarangViewModel(orderRepository) as T //, barangRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}