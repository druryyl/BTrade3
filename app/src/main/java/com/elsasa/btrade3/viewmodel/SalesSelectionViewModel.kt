package com.elsasa.btrade3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsasa.btrade3.model.SalesPerson
import com.elsasa.btrade3.repository.SalesPersonRepository
import com.elsasa.btrade3.repository.StaticDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SalesSelectionViewModel(
    private val salesPersonRepository: SalesPersonRepository
) : ViewModel() {
    private val _salesPersons = MutableStateFlow<List<SalesPerson>>(emptyList())
    val salesPersons: StateFlow<List<SalesPerson>> = _salesPersons.asStateFlow()

    init {
        loadSalesPersons()
    }

    private fun loadSalesPersons() {
        viewModelScope.launch {
            salesPersonRepository.getAllSalesPersons().collect { salesList ->
                _salesPersons.value = salesList.sortedBy { it.salesPersonName }
            }
        }
    }
}