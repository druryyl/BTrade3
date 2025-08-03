package com.elsasa.btrade3.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elsasa.btrade3.repository.FakturRepository

class FakturListViewModelFactory(
    private val repository: FakturRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FakturListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FakturListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}