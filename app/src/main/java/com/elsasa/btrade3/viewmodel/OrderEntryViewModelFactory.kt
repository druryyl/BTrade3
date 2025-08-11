package com.elsasa.btrade3.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elsasa.btrade3.repository.OrderRepository

class OrderEntryViewModelFactory(
    private val repository: OrderRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderEntryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrderEntryViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}