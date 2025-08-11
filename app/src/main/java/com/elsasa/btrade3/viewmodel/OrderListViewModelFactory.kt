package com.elsasa.btrade3.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elsasa.btrade3.repository.OrderRepository

class OrderListViewModelFactory(
    private val repository: OrderRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrderListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}