package com.elsasa.btrade3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsasa.btrade3.model.Barang
import com.elsasa.btrade3.model.OrderItem
import com.elsasa.btrade3.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddBarangViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {
    private val _fakturId = MutableStateFlow<String?>(null)
    val fakturId: StateFlow<String?> = _fakturId.asStateFlow()

    private val _selectedBarang = MutableStateFlow<Barang?>(null)
    val selectedBarang: StateFlow<Barang?> = _selectedBarang.asStateFlow()

    private val _qtyBesar = MutableStateFlow(0)
    val qtyBesar: StateFlow<Int> = _qtyBesar.asStateFlow()

    private val _qtyKecil = MutableStateFlow(0)
    val qtyKecil: StateFlow<Int> = _qtyKecil.asStateFlow()

    private val _editingItemId: MutableStateFlow<String?> = MutableStateFlow(null)
    private val _originalOrderItem: MutableStateFlow<OrderItem?> = MutableStateFlow(null)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()


    fun setFakturId(fakturId: String) {
        _fakturId.value = fakturId
    }

    fun selectBarang(barang: Barang) {
        _selectedBarang.value = barang
    }

    fun setQtyBesar(newQty: Int) {
        if (newQty >= 0) {
            _qtyBesar.value = newQty
        }
    }

    fun setQtyKecil(newQty: Int) {
        if (newQty >= 0) {
            _qtyKecil.value = newQty
        }
    }


    fun loadItemForEditing(itemId: String){
        viewModelScope.launch {
            _fakturId.value?.let { fakturId ->
                val item = orderRepository.getOrderItemsByOrderId(fakturId).first()
                val itemToEdit = item.find{
                    "${it.orderId}-${it.noUrut}" == itemId ||
                    it.noUrut.toString() == itemId
                }

                itemToEdit?.let { item ->
                    _editingItemId.value = itemId
                    _originalOrderItem.value = item

                    val barang = Barang(
                            brgId = item.brgId,
                            brgCode = item.brgCode,
                            brgName = item.brgName,
                            kategoriName = item.kategoriName,
                            satBesar = item.satBesar,
                            satKecil = item.satKecil,
                            konversi = item.konversi,
                            hrgSat = item.unitPrice,
                            stok = 0
                        )

                    _selectedBarang.value = barang
                    _qtyBesar.value = item.qtyBesar
                    _qtyKecil.value = item.qtyKecil
                }
            }
        }
    }

    fun saveItem() {
        val fakturId = _fakturId.value ?: return
        val barang = _selectedBarang.value ?: return
        val qtyBesar = _qtyBesar.value
        val qtyKecil = _qtyKecil.value


        viewModelScope.launch {
            // Get the current items to determine the next sequence number
            val currentItems = orderRepository.getOrderItemsByOrderId(fakturId).first()
            val faktur = orderRepository.getOrderById(fakturId)
            val nextNoUrut = currentItems.size + 1

            val lineTotal1 = (qtyBesar * barang.konversi * barang.hrgSat)
            val lineTotal2 = (qtyKecil * barang.hrgSat)
            val lineTotal = lineTotal1 + lineTotal2
            val orderItem = OrderItem(
                orderId = fakturId,
                noUrut = nextNoUrut,
                brgId = barang.brgId,
                brgCode = barang.brgCode,
                brgName = barang.brgName,
                kategoriName = barang.kategoriName,
                qtyBesar = qtyBesar,
                satBesar = barang.satBesar,
                qtyKecil = qtyKecil,
                satKecil = barang.satKecil,
                konversi = barang.konversi,
                unitPrice = barang.hrgSat,
                lineTotal = lineTotal
            )

            orderRepository.insertOrderItem(orderItem)
            updateTotalAmount()
        }
    }

    fun updateItem(){
        val fakturId = _fakturId.value ?: return
        val barang = _selectedBarang.value ?: return
        val qtyBesar = _qtyBesar.value
        val qtyKecil = _qtyKecil.value
        val originalItem = _originalOrderItem.value ?: return

        viewModelScope.launch {
            val lineTotal1 = (qtyBesar * barang.konversi * barang.hrgSat)
            val lineTotal2 = (qtyKecil * barang.hrgSat)
            val lineTotal = lineTotal1 + lineTotal2
            val orderItem = OrderItem(
                orderId = fakturId,
                noUrut = originalItem.noUrut,
                brgId = barang.brgId,
                brgCode = barang.brgCode,
                brgName = barang.brgName,
                kategoriName = barang.kategoriName,
                qtyBesar = qtyBesar,
                satBesar = barang.satBesar,
                qtyKecil = qtyKecil,
                satKecil = barang.satKecil,
                konversi = barang.konversi,
                unitPrice = barang.hrgSat,
                lineTotal = lineTotal
            )

            orderRepository.updateOrderItem(orderItem)

            // Update total amount in faktur
            updateTotalAmount()
        }
    }

    private fun updateTotalAmount(){
        val fakturId = _fakturId.value ?: return
        viewModelScope.launch {
            val total = orderRepository.calculateTotalAmount(fakturId)
            orderRepository.getOrderById(fakturId)?.let { faktur ->
                orderRepository.updateOrder(faktur.copy(
                    totalAmount = total
                ))
            }
        }
    }
}