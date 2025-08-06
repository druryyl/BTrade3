package com.elsasa.btrade3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elsasa.btrade3.repository.BarangRepository
import com.elsasa.btrade3.repository.FakturRepository
import com.elsasa.btrade3.repository.StaticDataRepository

class AddBarangViewModelFactory(
    private val fakturRepository: FakturRepository,
    private val barangRepository: BarangRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddBarangViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddBarangViewModel(fakturRepository, barangRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}