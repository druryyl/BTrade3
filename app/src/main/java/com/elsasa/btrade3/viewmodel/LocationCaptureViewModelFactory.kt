package com.elsasa.btrade3.viewmodel


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elsasa.btrade3.repository.CustomerRepository
import com.elsasa.btrade3.repository.CustomerSyncRepository

class LocationCaptureViewModelFactory(
    private val context: Context,
    private val customerRepository: CustomerRepository,
    private val customerSyncRepository: CustomerSyncRepository // Add this parameter
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationCaptureViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocationCaptureViewModel(context, customerRepository, customerSyncRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}