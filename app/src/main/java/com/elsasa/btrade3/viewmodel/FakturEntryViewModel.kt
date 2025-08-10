package com.elsasa.btrade3.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsasa.btrade3.model.Faktur
import com.elsasa.btrade3.repository.FakturRepository
import com.elsasa.btrade3.ui.getUserEmail
import com.elsasa.btrade3.util.FriendlyIdGenerator
import com.elsasa.btrade3.util.LastSelectionManager
import com.elsasa.btrade3.util.UlidHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FakturEntryViewModel(
    private val repository: FakturRepository,
    private val context: Context

) : ViewModel() {
    private val _faktur = MutableStateFlow<Faktur?>(null)
    val faktur: StateFlow<Faktur?> = _faktur.asStateFlow()

    private val lastSelectionManager = LastSelectionManager(context)
    private var hasCreatedNewFaktur = false

    fun loadFaktur(fakturId: String) {
        viewModelScope.launch {
            _faktur.value = repository.getFakturById(fakturId)
            hasCreatedNewFaktur = false
        }
    }

    fun createNewFaktur(context: Context) {
        if (hasCreatedNewFaktur) return

        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val userEmail = getUserEmail(context) ?: ""
        val idGenerator = FriendlyIdGenerator()

        val lastSalesPersonId = lastSelectionManager.getLastSalesPersonId() ?: ""
        val lastSalesPersonName = lastSelectionManager.getLastSalesPersonName() ?: ""

        val fakturId = UlidHelper.generate()
        val fakturLocalCode = idGenerator.generateCompactDateSequenceId(context)

        val newFaktur =  Faktur(
            fakturId = fakturId,
            fakturLocalCode = fakturLocalCode,
            customerId = "",
            customerCode = "",
            customerName = "",
            customerAddress = "",
            fakturDate = currentDate,
            salesId = lastSalesPersonId,
            salesName = lastSalesPersonName,
            totalAmount = 0.0,
            userEmail = userEmail,
            statusSync = "OPEN"
        )
        _faktur.value = newFaktur
        hasCreatedNewFaktur = true

        // Save to database immediately
        saveFaktur(newFaktur)
    }

    fun updateCustomerInfo(customerId: String, customerCode: String, customerName: String, customerAddress: String) {
        val current = _faktur.value ?: return
        val updatedFaktur = current.copy(
            customerId = customerId,
            customerCode = customerCode,
            customerName = customerName,
            customerAddress = customerAddress)
        _faktur.value = updatedFaktur
        saveFakturAndReload(updatedFaktur)

        lastSelectionManager.saveLastCustomer(customerId,customerCode, customerName, customerAddress)
    }

    fun updateSalesInfo(salesId: String, salesName: String) {
        val current = _faktur.value ?: return
        val updatedFaktur = current.copy(
            salesId = salesId,
            salesName = salesName
        )
        _faktur.value = updatedFaktur
        saveFakturAndReload(updatedFaktur)

        lastSelectionManager.saveLastSalesPerson(salesId,salesName)
    }

    fun updateTotalAmount(totalAmount: Double) {
        val current = _faktur.value ?: return
        val updatedFaktur = current.copy(totalAmount = totalAmount)
        _faktur.value = updatedFaktur
        saveFakturAndReload(updatedFaktur)
    }

    fun updateUserEmail(email: String){
        Log.d("GoogleSignIn", "Updating user email to: $email")
        val current = _faktur.value?:return
        val updatedFaktur = current.copy(userEmail = email)
        _faktur.value = updatedFaktur
        Log.d("GoogleSignIn", "Updating user email to: $email")
        saveFakturAndReload(updatedFaktur)
    }

    private fun saveFaktur(fakturToSave: Faktur) {
        viewModelScope.launch {
            repository.insertFaktur(fakturToSave)
        }
    }
    private fun saveFakturAndReload(fakturToSave: Faktur) {
        viewModelScope.launch {
            // Save to database
            repository.insertFaktur(fakturToSave)
            // Reload from database to ensure consistency
            _faktur.value = repository.getFakturById(fakturToSave.fakturId)
        }
    }
}