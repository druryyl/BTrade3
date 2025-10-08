package com.elsasa.btrade3.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elsasa.btrade3.repository.CustomerLocationRepository

class CustomerLocationViewModelFactory(
    private val customerLocationRepository: CustomerLocationRepository,
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustomerLocationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CustomerLocationViewModel(
                application,
                customerLocationRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}