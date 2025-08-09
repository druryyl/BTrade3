package com.elsasa.btrade3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsasa.btrade3.model.Barang
import com.elsasa.btrade3.model.FakturItem
import com.elsasa.btrade3.repository.FakturRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddBarangViewModel(
    private val fakturRepository: FakturRepository,
    //private val barangRepository: BarangRepository
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
    private val _originalFakturItem: MutableStateFlow<FakturItem?> = MutableStateFlow(null)

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
                val item = fakturRepository.getFakturItemsByFakturId(fakturId).first()
                val itemToEdit = item.find{
                    "${it.fakturId}-${it.noUrut}" == itemId ||
                    it.noUrut.toString() == itemId
                }

                itemToEdit?.let { item ->
                    _editingItemId.value = itemId
                    _originalFakturItem.value = item

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
            val currentItems = fakturRepository.getFakturItemsByFakturId(fakturId).first()
            val nextNoUrut = currentItems.size + 1

            val lineTotal1 = (qtyBesar * barang.konversi * barang.hrgSat)
            val lineTotal2 = (qtyKecil * barang.hrgSat)
            val lineTotal = lineTotal1 + lineTotal2
            val fakturItem = FakturItem(
                fakturId = fakturId,
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

            fakturRepository.insertFakturItem(fakturItem)
            updateTotalAmount()
        }
    }

    fun updateItem(){
        val fakturId = _fakturId.value ?: return
        val barang = _selectedBarang.value ?: return
        val qtyBesar = _qtyBesar.value
        val qtyKecil = _qtyKecil.value
        val originalItem = _originalFakturItem.value ?: return

        viewModelScope.launch {
            val lineTotal1 = (qtyBesar * barang.konversi * barang.hrgSat)
            val lineTotal2 = (qtyKecil * barang.hrgSat)
            val lineTotal = lineTotal1 + lineTotal2
            val fakturItem = FakturItem(
                fakturId = fakturId,
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

            fakturRepository.updateFakturItem(fakturItem)

            // Update total amount in faktur
            updateTotalAmount()
        }
    }

    private fun updateTotalAmount(){
        val fakturId = _fakturId.value ?: return
        viewModelScope.launch {
            val total = fakturRepository.calculateTotalAmount(fakturId)
            fakturRepository.getFakturById(fakturId)?.let { faktur ->
                fakturRepository.updateFaktur(faktur.copy(
                    totalAmount = total
                ))
            }
        }
    }
}