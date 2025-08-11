package com.elsasa.btrade3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsasa.btrade3.model.OrderItem
import com.elsasa.btrade3.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ItemListViewModel(
    private val repository: OrderRepository
) : ViewModel() {
    private val _fakturId = MutableStateFlow<String?>(null)
    val fakturId: StateFlow<String?> = _fakturId.asStateFlow()

    private val _items = MutableStateFlow<List<OrderItem>>(emptyList())
    val items: StateFlow<List<OrderItem>> = _items.asStateFlow()

    fun setFakturId(fakturId: String) {
        _fakturId.value = fakturId
        loadItems(fakturId)
    }

    private fun loadItems(fakturId: String) {
        viewModelScope.launch {
            repository.getOrderItemsByOrderId(fakturId).collectLatest { itemList ->
                _items.value = itemList
            }
        }
    }

    fun deleteItem(item: OrderItem) {
        viewModelScope.launch {
            repository.deleteOrderItem(item)
            updateTotalAmount()
        }
    }

    fun updateTotalAmount() {
        val id = _fakturId.value ?: return
        viewModelScope.launch {
            val total = repository.calculateTotalAmount(id)
            repository.getOrderById(id)?.let { faktur ->
                repository.updateOrder(faktur.copy(totalAmount = total))
            }
        }
    }
}