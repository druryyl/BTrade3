package com.elsasa.btrade3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsasa.btrade3.model.Order
import com.elsasa.btrade3.repository.OrderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OrderListViewModel(
    private val repository: OrderRepository
) : ViewModel() {
    val orders: StateFlow<List<Order>> = repository.getAllOrders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    suspend fun getItemCount(fakturId: String): Int {
        return repository.getOrderItemsByOrderId(fakturId).count()
    }
    private val itemCountCache = mutableMapOf<String, StateFlow<Int>>()

    fun getItemCountAsState(fakturId: String): StateFlow<Int> {
        return itemCountCache.getOrPut(fakturId) {
            repository.getOrderItemsByOrderId(fakturId)
                .map { items -> items.count() }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = 0
                )
        }
    }

    fun deleteOrder(order: Order) {
        viewModelScope.launch {
            repository.deleteOrder(order)
        }
    }
}