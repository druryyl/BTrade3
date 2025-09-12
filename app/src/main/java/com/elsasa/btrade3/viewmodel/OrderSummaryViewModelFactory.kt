package com.elsasa.btrade3.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elsasa.btrade3.repository.OrderRepository

class OrderSummaryViewModelFactory(
    private val orderRepository: OrderRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderSummaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrderSummaryViewModel(orderRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}