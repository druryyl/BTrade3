package com.elsasa.btrade3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsasa.btrade3.model.OrderSummary
import com.elsasa.btrade3.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

class OrderSummaryViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _orderSummaries = MutableStateFlow<List<OrderSummary>>(emptyList())
    val orderSummaries: StateFlow<List<OrderSummary>> = _orderSummaries.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _dateRange = MutableStateFlow<Pair<String, String>?>(null)
    val dateRange: StateFlow<Pair<String, String>?> = _dateRange.asStateFlow()

    init {
        loadOrderSummaries()
    }

    private fun loadOrderSummaries() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                orderRepository.getOrderSummary().collectLatest { summaries ->
                    _orderSummaries.value = summaries
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
                // Handle error if needed
            }
        }
    }

    fun loadOrderSummariesByDateRange(startDate: String, endDate: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _dateRange.value = Pair(startDate, endDate)
            try {
                orderRepository.getOrderSummaryByDateRange(startDate, endDate).collectLatest { summaries ->
                    _orderSummaries.value = summaries
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
                // Handle error if needed
            }
        }
    }

    fun resetToAllData() {
        _dateRange.value = null
        loadOrderSummaries()
    }

    fun getTotalSales(): Double {
        return _orderSummaries.value.sumOf { it.grossSales }
    }

    fun getTotalOrders(): Int {
        return _orderSummaries.value.sumOf { it.orderCount }
    }
}