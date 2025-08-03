package com.elsasa.btrade3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsasa.btrade3.model.Customer
import com.elsasa.btrade3.repository.StaticDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CustomerSelectionViewModel(
    private val repository: StaticDataRepository
) : ViewModel() {
    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadCustomers()
    }

    private fun loadCustomers() {
        viewModelScope.launch {
            repository.getCustomers().collectLatest { customerList ->
                _customers.value = customerList
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getFilteredCustomers(): List<Customer> {
        val query = _searchQuery.value.lowercase()
        return if (query.isEmpty()) {
            _customers.value
        } else {
            _customers.value.filter { customer ->
                customer.customerCode.lowercase().contains(query) ||
                        customer.customerName.lowercase().contains(query) ||
                        customer.address.lowercase().contains(query)
            }
        }
    }
}