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

    private val _qtyBonus = MutableStateFlow(0)
    val qtyBonus: StateFlow<Int> = _qtyBonus.asStateFlow()

    private val _disc1 = MutableStateFlow(0.0)
    val disc1: StateFlow<Double> = _disc1.asStateFlow()

    private val _disc2 = MutableStateFlow(0.0)
    val disc2: StateFlow<Double> = _disc2.asStateFlow()

    private val _disc3 = MutableStateFlow(0.0)
    val disc3: StateFlow<Double> = _disc3.asStateFlow()

    private val _disc4 = MutableStateFlow(0.0)
    val disc4: StateFlow<Double> = _disc4.asStateFlow()

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

    fun setQtyBonus(newQty: Int) {
        if (newQty >= 0) {
            _qtyBonus.value = newQty
        }
    }

    fun setDisc1(newDisc: Double) {
        _disc1.value = newDisc
    }

    fun setDisc2(newDisc: Double) {
        _disc2.value = newDisc
    }

    fun setDisc3(newDisc: Double) {
        _disc3.value = newDisc
    }

    fun setDisc4(newDisc: Double) {
        _disc4.value = newDisc
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
                    _qtyBonus.value = item.qtyBonus
                    _disc1.value = item.disc1
                    _disc2.value = item.disc2
                    _disc3.value = item.disc3
                    _disc4.value = item.disc4
                }
            }
        }
    }

    fun saveItem() {
        val fakturId = _fakturId.value ?: return
        val barang = _selectedBarang.value ?: return
        val qtyBesar = _qtyBesar.value
        val qtyKecil = _qtyKecil.value
        val qtyBonus = _qtyBonus.value
        val disc1 = _disc1.value
        val disc2 = _disc2.value
        val disc3 = _disc3.value
        val disc4 = _disc4.value


        viewModelScope.launch {
            // Get the current items to determine the next sequence number
            val currentItems = orderRepository.getOrderItemsByOrderId(fakturId).first()
            val faktur = orderRepository.getOrderById(fakturId)
            val nextNoUrut = currentItems.size + 1

            val lineTotal1 = (qtyBesar * barang.konversi * barang.hrgSat)
            val lineTotal2 = (qtyKecil * barang.hrgSat)
            var lineTotal = lineTotal1 + lineTotal2
            val disc1Rp = disc1 * lineTotal / 100
            lineTotal = lineTotal - disc1Rp
            val disc2Rp = disc2 * lineTotal / 100
            lineTotal = lineTotal - disc2Rp
            val disc3Rp = disc3 * lineTotal / 100
            lineTotal = lineTotal - disc3Rp
            val disc4Rp = disc4 * lineTotal / 100
            lineTotal = lineTotal - disc4Rp

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
                qtyBonus = qtyBonus,
                konversi = barang.konversi,
                unitPrice = barang.hrgSat,
                disc1 = disc1,
                disc2 = disc2,
                disc3 = disc3,
                disc4 = disc4,
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
        val qtyBonus = _qtyBonus.value
        val disc1 = _disc1.value
        val disc2 = _disc2.value
        val disc3 = _disc3.value
        val disc4 = _disc4.value
        val originalItem = _originalOrderItem.value ?: return

        viewModelScope.launch {
            val lineTotal1 = (qtyBesar * barang.konversi * barang.hrgSat)
            val lineTotal2 = (qtyKecil * barang.hrgSat)
            var lineTotal = lineTotal1 + lineTotal2
            val disc1Rp = disc1 * lineTotal / 100
            lineTotal = lineTotal - disc1Rp
            val disc2Rp = disc2 * lineTotal / 100
            lineTotal = lineTotal - disc2Rp
            val disc3Rp = disc3 * lineTotal / 100
            lineTotal = lineTotal - disc3Rp
            val disc4Rp = disc4 * lineTotal / 100
            lineTotal = lineTotal - disc4Rp

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
                qtyBonus = qtyBonus,
                konversi = barang.konversi,
                unitPrice = barang.hrgSat,
                disc1 = disc1,
                disc2 = disc2,
                disc3 = disc3,
                disc4 = disc4,
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