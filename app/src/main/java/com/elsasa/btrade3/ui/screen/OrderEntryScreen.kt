package com.elsasa.btrade3.ui.screen

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.elsasa.btrade3.model.Order
import com.elsasa.btrade3.viewmodel.OrderEntryViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderEntryScreen(
    navController: NavController,
    viewModel: OrderEntryViewModel,
    orderId: String?,
    statusSync: String?,
    context: Context = LocalContext.current
) {

    val order by viewModel.order.collectAsStateWithLifecycle()

    val selectedCustomerId = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("selected_customer_id")

    val selectedCustomerCode = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("selected_customer_code")

    val selectedCustomerName = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("selected_customer_name")

    val selectedCustomerAddress = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("selected_customer_address")

    val selectedSalesId = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("selected_sales_id")

    val selectedSalesName = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("selected_sales_name")

    val initializationKey = if (orderId == "new") {
        val orderId2 = order?.orderId
        orderId2 ?: "new"
    } else {
        orderId
    }
    LaunchedEffect(initializationKey) {
        if (initializationKey == "new") {
            viewModel.createNewOrder(context)
        } else {
            initializationKey?.let { viewModel.loadOrder(it) }
        }
    }

    // Handle customer selection result
    LaunchedEffect(selectedCustomerId, selectedCustomerCode, selectedCustomerName, selectedCustomerAddress) {
        selectedCustomerId?.let { id ->
            selectedCustomerCode?.let { code ->
                selectedCustomerName?.let { name ->
                    selectedCustomerAddress?.let { address ->
                        viewModel.updateCustomerInfo(id, code, name, address)
                        // Clear the saved state to avoid reprocessing
                        navController.currentBackStackEntry?.savedStateHandle?.remove<String>("selected_customer_id")
                        navController.currentBackStackEntry?.savedStateHandle?.remove<String>("selected_customer_code")
                        navController.currentBackStackEntry?.savedStateHandle?.remove<String>("selected_customer_name")
                        navController.currentBackStackEntry?.savedStateHandle?.remove<String>("selected_customer_address")
                    }
                }
            }
        }
    }

    // Handle sales selection result
    LaunchedEffect(selectedSalesId,selectedSalesName) {
        selectedSalesId?.let { id ->
            selectedSalesName?.let { name ->
                viewModel.updateSalesInfo(id, name)
                // Clear the saved state to avoid reprocessing
                navController.currentBackStackEntry?.savedStateHandle?.remove<String>("selected_sales_id")
                navController.currentBackStackEntry?.savedStateHandle?.remove<String>("selected_sales_name")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (orderId == "new") "New Sales Order" else "Edit Sales Order") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        order?.let { orderData ->
            FakturEntryContent(
                order = orderData,
                onCustomerSelect = {
                    if (statusSync != "DRAFT") {
                        // Show toast message
                        Toast.makeText(
                            context,
                            "Order cannot be edited",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@FakturEntryContent
                    }

                    navController.navigate("customer_selection") {
                        popUpTo("order_entry") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onSalesSelect = {
                    if (statusSync != "DRAFT") {
                        // Show toast message
                        Toast.makeText(
                            context,
                            "Order cannot be edited",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@FakturEntryContent
                    }

                    navController.navigate("sales_selection") {
                        popUpTo("order_entry") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onViewItems = {
                    navController.navigate("item_list/${orderData.orderId}/${orderData.statusSync}")
                },
                modifier = Modifier.padding(padding)
            )
        }
    }
}


@Composable
fun FakturEntryContent(
    order: Order,
    onCustomerSelect: () -> Unit,
    onSalesSelect: () -> Unit,
    onViewItems: () -> Unit,
    modifier: Modifier = Modifier
) {
    val customerCode = order.customerCode
    val customerName = order.customerName
    val salesName = order.salesName
    val userEmail = order.userEmail

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp) // Smaller outer padding for compact look
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (userEmail.isNotEmpty()) {
            // User Email Section
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = "User Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Signed in as:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Customer Info Section
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = "Customer Information",
                    style = MaterialTheme.typography.titleMedium,
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
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Sales Info Section
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = "Sales Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onSalesSelect,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = salesName.ifEmpty { "Select Sales Person" },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Total Amount Section
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = "Order Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Amount:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = formatCurrency(order.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onViewItems,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("View / Edit Items")
                }
            }
        }
    }
}


private fun formatCurrency(amount: Double): String {
    val locale = Locale.Builder().setLanguage("id").setRegion("ID").build()
    val format = NumberFormat.getCurrencyInstance(locale)
    format.maximumFractionDigits = 0  // This sets the maximum decimal places to 0
    format.minimumFractionDigits = 0
    return format.format(amount)
}