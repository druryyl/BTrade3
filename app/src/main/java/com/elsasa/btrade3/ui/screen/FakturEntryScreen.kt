package com.elsasa.btrade3.ui.screen

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.elsasa.btrade3.model.Faktur
import com.elsasa.btrade3.viewmodel.FakturEntryViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FakturEntryScreen(
    navController: NavController,
    viewModel: FakturEntryViewModel,
    fakturId: String?,
    context: Context = LocalContext.current
) {
    val faktur by viewModel.faktur.collectAsStateWithLifecycle()

    val selectedCustomerCode = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("selected_customer_code")

    val selectedCustomerName = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("selected_customer_name")

    val selectedSalesName = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("selected_sales_name")

    LaunchedEffect(Unit) {
        if (fakturId == "new") {
            viewModel.createNewFaktur(context)
        } else {
            fakturId?.let { viewModel.loadFaktur(it) }
        }
    }

    // Handle customer selection result
    LaunchedEffect(selectedCustomerCode, selectedCustomerName) {
        selectedCustomerCode?.let { code ->
            selectedCustomerName?.let { name ->
                viewModel.updateCustomerInfo(code, name)
                // Clear the saved state to avoid reprocessing
                navController.currentBackStackEntry?.savedStateHandle?.remove<String>("selected_customer_code")
                navController.currentBackStackEntry?.savedStateHandle?.remove<String>("selected_customer_name")
            }
        }
    }

    // Handle sales selection result
    LaunchedEffect(selectedSalesName) {
        selectedSalesName?.let { name ->
            viewModel.updateSalesInfo(name)
            // Clear the saved state to avoid reprocessing
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("selected_sales_name")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (fakturId == "new") "New Sales Order" else "Edit Sales Order") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        faktur?.let { fakturData ->
            FakturEntryContent(
                faktur = fakturData,
                onCustomerSelect = {
                    navController.navigate("customer_selection") {
                        popUpTo("faktur_entry") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onSalesSelect = {
                    navController.navigate("sales_selection") {
                        popUpTo("faktur_entry") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onViewItems = {
                    navController.navigate("item_list/${fakturData.fakturId}")
                },
                onUpdateCustomer = { code, name ->
                    viewModel.updateCustomerInfo(code, name)
                },
                onUpdateSales = { name ->
                    viewModel.updateSalesInfo(name)
                },
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
fun FakturEntryContent(
    faktur: Faktur,
    onCustomerSelect: () -> Unit,
    onSalesSelect: () -> Unit,
    onViewItems: () -> Unit,
    onUpdateCustomer: (String, String) -> Unit,
    onUpdateSales: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val customerCode = faktur.customerCode
    val customerName = faktur.customerName
    val salesName = faktur.salesName
    val userEmail = faktur.userEmail
    // Log.d("FakturEntryContent", "customerCode=$customerCode, customerName=$customerName, salesName=$salesName")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (userEmail.isNotEmpty()) {
            // User Email Section
            Card(elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "User Information",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Signed in as: $userEmail",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        // Customer Info Section
        Card(elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Customer Information",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onCustomerSelect,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (customerName.isNotEmpty()) {
                            "$customerCode - $customerName"
                        } else {
                            "Select Customer"
                        }
                    )
                }
            }
        }

        // Sales Info Section
        Card(elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Sales Information",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onSalesSelect,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = salesName.ifEmpty {
                            "Select Sales Person"
                        }
                    )
                }
            }
        }

        // Total Amount Section
        Card(elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Order Summary",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Amount:")
                    Text(
                        text = formatCurrency(faktur.totalAmount),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onViewItems,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View/Edit Items")
                }
            }
        }

        // Save Button
//        Button(
//            onClick = {
//                onUpdateCustomer(customerCode, customerName)
//                onUpdateSales(salesName)
//            },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Save")
//        }
    }
}

private fun formatCurrency(amount: Double): String {
    val locale = Locale.Builder().setLanguage("id").setRegion("ID").build()
    val format = NumberFormat.getCurrencyInstance(locale)
    return format.format(amount)
}