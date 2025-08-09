package com.elsasa.btrade3.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elsasa.btrade3.repository.FakturRepository

class FakturEntryViewModelFactory(
    private val repository: FakturRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FakturEntryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FakturEntryViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}