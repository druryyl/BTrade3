package com.elsasa.btrade3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsasa.btrade3.model.Barang
import com.elsasa.btrade3.model.FakturItem
import com.elsasa.btrade3.repository.BarangRepository
import com.elsasa.btrade3.repository.FakturRepository
import com.elsasa.btrade3.repository.StaticDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddBarangViewModel(
    private val fakturRepository: FakturRepository,
    private val barangRepository: BarangRepository
    //private val staticDataRepository: StaticDataRepository
) : ViewModel() {
    private val _fakturId = MutableStateFlow<String?>(null)
    val fakturId: StateFlow<String?> = _fakturId.asStateFlow()

    private val _selectedBarang = MutableStateFlow<Barang?>(null)
    val selectedBarang: StateFlow<Barang?> = _selectedBarang.asStateFlow()

    private val _qty = MutableStateFlow(1)
    val qty: StateFlow<Int> = _qty.asStateFlow()

    private val _editingItemId: MutableStateFlow<String?> = MutableStateFlow(null)
    private val _originalFakturItem: MutableStateFlow<FakturItem?> = MutableStateFlow(null)

    private val _barangs = MutableStateFlow<List<Barang>>(emptyList())
    val barangs: StateFlow<List<Barang>> = _barangs.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadBarangs()
    }

    fun setFakturId(fakturId: String) {
        _fakturId.value = fakturId
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

    fun getFilteredBarangs(): List<Barang> {
        val query = _searchQuery.value.lowercase()
        return if (query.isEmpty()) {
            _barangs.value
        } else {
            _barangs.value.filter { barang ->
                barang.brgCode.lowercase().contains(query) ||
                barang.brgName.lowercase().contains(query) ||
                barang.kategoriName.lowercase().contains(query)
            }
        }
    }

    fun selectBarang(barang: Barang) {
        _selectedBarang.value = barang
    }

    fun setQty(newQty: Int) {
        if (newQty > 0) {
            _qty.value = newQty
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

                    val barang = _barangs.value.find{ it.brgCode == item.brgCode }
                        ?: Barang(
                            brgId = "",
                            brgCode = item.brgCode,
                            brgName = item.brgName,
                            kategoriName = "",
                            satBesar = "",
                            satKecil = item.unitName,
                            konversi = 1,
                            hrgSat = item.unitPrice,
                            stok = 0
                        )

                    _selectedBarang.value = barang
                    _qty.value = item.qty
                }
            }
        }
    }

    fun saveItem() {
        val fakturId = _fakturId.value ?: return
        val barang = _selectedBarang.value ?: return
        val qty = _qty.value

        viewModelScope.launch {
            // Get the current items to determine the next sequence number
            val currentItems = fakturRepository.getFakturItemsByFakturId(fakturId).first()
            val nextNoUrut = currentItems.size + 1

            val lineTotal = qty * barang.hrgSat
            val fakturItem = FakturItem(
                fakturId = fakturId,
                noUrut = nextNoUrut,
                brgCode = barang.brgCode,
                brgName = barang.brgName,
                qty = qty,
                unitName = barang.satKecil,
                unitPrice = barang.hrgSat,
                lineTotal = lineTotal
            )

            fakturRepository.insertFakturItem(fakturItem)
            updateTotalAmount()
        }
    }

    fun updateItem(itemId: String){
        val fakturId = _fakturId.value ?: return
        val barang = _selectedBarang.value ?: return
        val qty = _qty.value
        val originalItem = _originalFakturItem.value ?: return

        viewModelScope.launch {
            val lineTotal = qty * barang.hrgSat
            val fakturItem = FakturItem(
                fakturId = fakturId,
                noUrut = originalItem.noUrut,
                brgCode = barang.brgCode,
                brgName = barang.brgName,
                qty = qty,
                unitName = barang.satKecil,
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
                fakturRepository.updateFaktur(faktur.copy(totalAmount = total))
            }
        }
    }
}