package com.elsasa.btrade3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsasa.btrade3.model.Barang
import com.elsasa.btrade3.repository.BarangRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BarangSelectionViewModel(
    private val barangRepository: BarangRepository
) : ViewModel() {
    private val _barangs = MutableStateFlow<List<Barang>>(emptyList())
    val barangs: StateFlow<List<Barang>> = _barangs.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadBarangs()
    }

    private fun loadBarangs() {
        viewModelScope.launch {
            barangRepository.getAllBarangs().collect { barangList ->
                _barangs.value = barangList
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

}