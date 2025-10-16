package com.elsasa.btrade3.ui.screen

import android.content.Context
import android.util.Log
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.elsasa.btrade3.model.Order
import com.elsasa.btrade3.ui.component.SelectableModernOrderCard
import com.elsasa.btrade3.ui.logoutUser
import com.elsasa.btrade3.util.MapUtils
import com.elsasa.btrade3.util.MovableFloatingActionButton
import com.elsasa.btrade3.viewmodel.OrderListViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(
    navController: NavController,
    viewModel: OrderListViewModel,
    context: Context = LocalContext.current
) {
    val orders by viewModel.orders.collectAsState()
    var orderToDelete by remember { mutableStateOf<Order?>(null) }

    // Selection mode state
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedOrders = remember { mutableStateMapOf<String, Order>() }

    // Bulk delete confirmation dialog
    var showBulkDeleteDialog by remember { mutableStateOf(false) }

    // Search state
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Filtered orders based on search
    val filteredOrders = remember(orders, searchQuery) {
        if (searchQuery.isBlank()) {
            orders
        } else {
            orders.filter { order ->
                order.customerName.contains(searchQuery, ignoreCase = true) ||
                        order.customerCode.contains(searchQuery, ignoreCase = true) ||
                        order.orderLocalId.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Delete confirmation dialog
    if (orderToDelete != null) {
        AlertDialog(
            onDismissRequest = { orderToDelete = null },
            title = { Text("Delete Sales Order") },
            text = {
                Text(
                    "Are you sure you want to delete this sales order?\n\n" +
                            (orderToDelete?.customerName ?: "")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        orderToDelete?.let { viewModel.deleteOrder(it) }
                        orderToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { orderToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Bulk delete confirmation dialog
    if (showBulkDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showBulkDeleteDialog = false },
            title = { Text("Delete Selected Orders") },
            text = {
                Text(
                    "Are you sure you want to delete ${selectedOrders.size} selected orders?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedOrders.values.forEach { order ->
                            viewModel.deleteOrder(order)
                        }
                        selectedOrders.clear()
                        isSelectionMode = false
                        showBulkDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            if (isSearchActive) {
                // Search mode top bar
                SearchTopAppBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { /* Search is live, so no action needed */ },
                    onClear = {
                        searchQuery = ""
                    },
                    onBack = {
                        isSearchActive = false
                        searchQuery = ""
                    }
                )
            } else if (isSelectionMode) {
                // Selection mode top bar
                TopAppBar(
                    title = {
                        Text(
                            "${selectedOrders.size} selected",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSelectionMode = false
                            selectedOrders.clear()
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        if (selectedOrders.isNotEmpty()) {
                            IconButton(onClick = {
                                showBulkDeleteDialog = true
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Selected"
                                )
                            }
                        }
                    }
                )
            } else {
                // Normal mode top bar
                TopAppBar(
                    title = {
                        Text(
                            "Sales Orders",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        }
                        OverflowMenu(navController, context) // Single menu button instead of multiple icons
                    },
                )
            }
        },
        floatingActionButton = {
            if (!isSelectionMode && !isSearchActive) {
                MovableFloatingActionButton(
                    onClick = { navController.navigate("faktur_entry/new/DRAFT") },
                    modifier = Modifier
                        .padding(end = 16.dp, bottom = 16.dp)
                )
            }
        }
    ) { padding ->
        if (filteredOrders.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (searchQuery.isBlank()) Icons.Default.ShoppingCart else Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (searchQuery.isBlank()) "No sales orders found" else "No matching orders found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (searchQuery.isBlank()) "Create a new one to get started" else "Try a different search term",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(filteredOrders) { order ->
                    val itemCount by viewModel.getItemCountAsState(order.orderId).collectAsState()

                    // Handle long press to enter selection mode
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()

                    LaunchedEffect(isPressed) {
                        if (isPressed && !isSelectionMode) {
                            isSelectionMode = true
                            selectedOrders[order.orderId] = order
                        }
                    }

                    SelectableModernOrderCard(
                        order = order,
                        itemCount = itemCount,
                        isSelected = selectedOrders.containsKey(order.orderId),
                        isSelectionMode = isSelectionMode,
                        onEditClick = {
                            if (isSelectionMode) {
                                // Toggle selection in selection mode
                                if (selectedOrders.containsKey(order.orderId)) {
                                    selectedOrders.remove(order.orderId)
                                } else {
                                    selectedOrders[order.orderId] = order
                                }
                            } else {
                                // Normal edit click
                                navController.navigate("faktur_entry/${order.orderId}/${order.statusSync}")
                            }
                        },
                        onDeleteClick = {
                            if (isSelectionMode) {
                                // Toggle selection in selection mode
                                if (selectedOrders.containsKey(order.orderId)) {
                                    selectedOrders.remove(order.orderId)
                                } else {
                                    selectedOrders[order.orderId] = order
                                }
                            } else {
                                // Normal delete click
                                orderToDelete = order
                            }
                        },
                        onSyncClick = {},
                        onOpenCustomerInMaps = { order ->
                            // Open customer location in Google Maps
                            if (order.customerLatitude != 0.0 && order.customerLongitude != 0.0) {
                                MapUtils.openInGoogleMaps(
                                    context = context,
                                    latitude = order.customerLatitude,
                                    longitude = order.customerLongitude,
                                    label = order.customerName
                                )
                            }
                        },
                        interactionSource = interactionSource
                    )
                }
            }
        }
    }
}

@Composable
fun OverflowMenu(
    navController: NavController,
    context: Context
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "More options"
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Manage Customers") },
                onClick = {
                    navController.navigate("customer_selection?fromMain=true")
                    expanded = false
                },
                leadingIcon = { Icon(Icons.Default.People, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("Sync Data") },
                onClick = {
                    navController.navigate("sync")
                    expanded = false
                },
                leadingIcon = { Icon(Icons.Default.Sync, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("Sync Orders") },
                onClick = {
                    navController.navigate("order_sync")
                    expanded = false
                },
                leadingIcon = { Icon(Icons.Default.CloudUpload, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("Order Summary") },
                onClick = {
                    navController.navigate("order_summary")
                    expanded = false
                },
                leadingIcon = { Icon(Icons.Default.Analytics, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("Logout") },
                onClick = {
                    logoutUser(context)
                    navController.navigate("login") {
                        popUpTo("faktur_list") { inclusive = true }
                    }
                    // Handle logout
                    expanded = false
                },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopAppBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit
) {
    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by customer, code, or order...") },
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { onSearch(query) }
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear"
                    )
                }
            }
        }
    )
}

private fun formatCurrency(amount: Double): String {
    val locale = Locale.Builder().setLanguage("id").setRegion("ID").build()
    val format = NumberFormat.getCurrencyInstance(locale)
    format.maximumFractionDigits = 0  // This sets the maximum decimal places to 0
    format.minimumFractionDigits = 0
    return format.format(amount)
}