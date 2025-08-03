package com.elsasa.btrade3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsasa.btrade3.model.Faktur
import com.elsasa.btrade3.repository.FakturRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FakturListViewModel(
    private val repository: FakturRepository
) : ViewModel() {
    val fakturs: StateFlow<List<Faktur>> = repository.getAllFakturs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteFaktur(faktur: Faktur) {
        viewModelScope.launch {
            repository.deleteFaktur(faktur)
        }
    }
}