package com.elsasa.btrade3.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elsasa.btrade3.repository.CustomerSyncRepository

class CustomerSyncViewModelFactory(
    private val customerSyncRepository: CustomerSyncRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustomerSyncViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CustomerSyncViewModel(customerSyncRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}