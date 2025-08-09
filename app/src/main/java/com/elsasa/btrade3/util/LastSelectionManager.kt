package com.elsasa.btrade3.util


import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.elsasa.btrade3.model.Customer

class LastSelectionManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("last_selections", Context.MODE_PRIVATE)

    companion object {
        private const val LAST_SALES_PERSON_ID_KEY = "last_sales_person_id"
        private const val LAST_SALES_PERSON_NAME_KEY = "last_sales_person_name"
        private const val LAST_CUSTOMER_ID_KEY = "last_customer_id"
        private const val LAST_CUSTOMER_CODE_KEY = "last_customer_code"
        private const val LAST_CUSTOMER_NAME_KEY = "last_customer_name"
        private const val LAST_CUSTOMER_ADDRESS_KEY = "last_customer_address"

    }

    fun saveLastSalesPerson(salesPersonId: String, salesPersonName: String) {
        prefs.edit { putString(LAST_SALES_PERSON_ID_KEY, salesPersonId) }
        prefs.edit { putString(LAST_SALES_PERSON_NAME_KEY, salesPersonName) }
    }

    // Get last selected sales person
    fun getLastSalesPersonName(): String? {
        return prefs.getString(LAST_SALES_PERSON_NAME_KEY, null)
    }
    fun getLastSalesPersonId(): String? {
        return prefs.getString(LAST_SALES_PERSON_ID_KEY, null)
    }

    // Save last selected customer
    fun saveLastCustomer(customerId: String, customerCode: String, customerName: String, address: String) {
        prefs.edit {
            putString(LAST_CUSTOMER_CODE_KEY, customerCode)
            .putString(LAST_CUSTOMER_NAME_KEY, customerName)
                .putString(LAST_CUSTOMER_ID_KEY, customerId)
                .putString(LAST_CUSTOMER_ADDRESS_KEY, address)
        }
    }

    fun getLastCustomer(): Customer {
        val id = prefs.getString(LAST_CUSTOMER_ID_KEY, null)
        val code = prefs.getString(LAST_CUSTOMER_CODE_KEY, null)
        val name = prefs.getString(LAST_CUSTOMER_NAME_KEY, null)
        val address = prefs.getString(LAST_CUSTOMER_ADDRESS_KEY, null)
        val customer = Customer(
            customerId = id?:"",
            customerCode = code?:"",
            customerName = name?:"",
            alamat = address?:"",
            wilayah = "")
        return customer
    }

    // Clear all last selections
    fun clearLastSelections() {
        prefs.edit {
            remove(LAST_SALES_PERSON_NAME_KEY)
                .remove(LAST_CUSTOMER_CODE_KEY)
                .remove(LAST_CUSTOMER_NAME_KEY)
                .remove(LAST_CUSTOMER_ID_KEY)
                .remove(LAST_CUSTOMER_ADDRESS_KEY)
        }
    }
}