package com.elsasa.btrade3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsasa.btrade3.model.FakturItem
import com.elsasa.btrade3.repository.FakturRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ItemListViewModel(
    private val repository: FakturRepository
) : ViewModel() {
    private val _fakturId = MutableStateFlow<String?>(null)
    val fakturId: StateFlow<String?> = _fakturId.asStateFlow()

    private val _items = MutableStateFlow<List<FakturItem>>(emptyList())
    val items: StateFlow<List<FakturItem>> = _items.asStateFlow()

    fun setFakturId(fakturId: String) {
        _fakturId.value = fakturId
        loadItems(fakturId)
    }

    private fun loadItems(fakturId: String) {
        viewModelScope.launch {
            repository.getFakturItemsByFakturId(fakturId).collectLatest { itemList ->
                _items.value = itemList
            }
        }
    }

    fun deleteItem(item: FakturItem) {
        viewModelScope.launch {
            repository.deleteFakturItem(item)
            updateTotalAmount()
        }
    }

    fun updateTotalAmount() {
        val id = _fakturId.value ?: return
        viewModelScope.launch {
            val total = repository.calculateTotalAmount(id)
            repository.getFakturById(id)?.let { faktur ->
                repository.updateFaktur(faktur.copy(totalAmount = total))
            }
        }
    }
}