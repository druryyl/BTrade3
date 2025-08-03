package com.elsasa.btrade3.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elsasa.btrade3.model.Faktur
import com.elsasa.btrade3.repository.FakturRepository
import com.elsasa.btrade3.ui.getUserEmail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FakturEntryViewModel(
    private val repository: FakturRepository
) : ViewModel() {
    private val _faktur = MutableStateFlow<Faktur?>(null)
    val faktur: StateFlow<Faktur?> = _faktur.asStateFlow()

    fun loadFaktur(fakturId: String) {
        viewModelScope.launch {
            _faktur.value = repository.getFakturById(fakturId)
        }
    }

    fun createNewFaktur(context: android.content.Context) {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val userEmail = getUserEmail(context) ?: ""
        _faktur.value = Faktur(
            fakturId = UUID.randomUUID().toString(),
            customerCode = "",
            customerName = "",
            fakturDate = currentDate,
            salesName = "",
            totalAmount = 0.0,
            userEmail = userEmail
        )
    }

    fun updateCustomerInfo(customerCode: String, customerName: String) {
        val current = _faktur.value ?: return
        val updatedFaktur = current.copy(
            customerCode = customerCode,
            customerName = customerName
        )
        _faktur.value = updatedFaktur
        saveFakturAndReload(updatedFaktur)
    }

    fun updateSalesInfo(salesName: String) {
        val current = _faktur.value ?: return
        val updatedFaktur = current.copy(salesName = salesName)
        _faktur.value = updatedFaktur
        saveFakturAndReload(updatedFaktur)
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

    private fun saveFakturAndReload(fakturToSave: Faktur) {
        viewModelScope.launch {
            // Save to database
            repository.insertFaktur(fakturToSave)
            // Reload from database to ensure consistency
            _faktur.value = repository.getFakturById(fakturToSave.fakturId)
        }
    }
}