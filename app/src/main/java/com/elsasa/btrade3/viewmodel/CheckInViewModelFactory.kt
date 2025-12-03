package com.elsasa.btrade3.viewmodel


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elsasa.btrade3.repository.CheckInRepository
import com.elsasa.btrade3.repository.CustomerRepository

class CheckInViewModelFactory(
    private val context: Context,
    private val checkInRepository: CheckInRepository,
    private val customerRepository: CustomerRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CheckInViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CheckInViewModel(context, checkInRepository, customerRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}