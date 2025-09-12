package com.elsasa.btrade3.ui.screen

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
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
                onOrderNoteChange = { note ->
                    viewModel.updateOrderNote(note)
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
    onOrderNoteChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val customerCode = order.customerCode
    val customerName = order.customerName
    val customerAddress = order.customerAddress
    val salesName = order.salesName
    val userEmail = order.userEmail
    val orderNote = order.orderNote


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (userEmail.isNotEmpty()) {
            InfoCard(
                title = userEmail,
                icon = Icons.Default.AccountCircle
            ) {
            }
        }

        // Customer Info
        InfoCard(
            title = "Customer",
            icon = Icons.Default.Person
        ) {
            OutlinedButton(
                onClick = onCustomerSelect,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
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

            if (customerAddress.isNotEmpty()) {
                //Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = customerAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Sales Info
        InfoCard(
            title = "Sales",
            icon = Icons.Default.Face
        ) {
            OutlinedButton(
                onClick = onSalesSelect,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = salesName.ifEmpty { "Select Sales Person" },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }


        // Order Summary
        InfoCard(
            title = "Order Summary",
            icon = Icons.Default.Receipt
        ) {
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
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Button(
                onClick = onViewItems,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("View / Edit Items")
            }
        }
        // Order Note
        InfoCard(
            title = "Order Note",
            icon = Icons.Default.RequestQuote
        ) {
            OutlinedTextField(
                value = orderNote,
                onValueChange = onOrderNoteChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp),
                label = { Text("Note For Admin") },
                textStyle = MaterialTheme.typography.bodyMedium,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Default
                ),
                maxLines = 4
            )
        }

    }
}

@Composable
fun InfoCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(7.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(7.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
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