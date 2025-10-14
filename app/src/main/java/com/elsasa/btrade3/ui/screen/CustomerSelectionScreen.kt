package com.elsasa.btrade3.ui.screen

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.elsasa.btrade3.model.Customer
import com.elsasa.btrade3.ui.component.SearchBar
import com.elsasa.btrade3.util.MapUtils
import com.elsasa.btrade3.util.RecentSearchManager
import com.elsasa.btrade3.viewmodel.CustomerSelectionViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSelectionScreen(
    navController: NavController,
    viewModel: CustomerSelectionViewModel,
    context: Context = LocalContext.current,
    fromMain: Boolean = false // New parameter to indicate if called from main menu

) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val isSearchFocused = remember {mutableStateOf(false)}

    var searchText by rememberSaveable { mutableStateOf(searchQuery) }
    val recentSearchManager = remember { RecentSearchManager(context, "customer") }
    var recentSearches by remember { mutableStateOf(recentSearchManager.getRecentSearches()) }

    val filteredCustomers = remember(customers, searchQuery) {
        if (searchQuery.isBlank()) {
            customers
        } else {
            // Split query into words, trim, and keep only non-empty
            val queryWords = searchQuery.trim()
                .split("\\s+".toRegex())
                .filter { it.isNotEmpty() }
                .map { it.lowercase() }

            customers.filter { customer ->
                val customerText = buildString {
                    append(customer.customerCode).append(" ")
                    append(customer.customerName).append(" ")
                    append(customer.alamat)
                }.lowercase()

                // Check if ALL words in query are present in the combined customer text
                queryWords.all { word -> customerText.contains(word) }
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Customers") }, // Changed title for main menu context
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar with Recent Searches
            Column {
                SearchBar(
                    query = searchText,
                    onQueryChange = { newQuery ->
                        searchText = newQuery
                        viewModel.setSearchQuery(newQuery)
                    },
                    onSearch = { query ->
                        if (query.isNotBlank()) {
                            recentSearchManager.addRecentSearch(query)
                            recentSearches = recentSearchManager.getRecentSearches()
                        }
                    },
                    placeholder = "Search by code, name, address, or region",
                    onFocusChange = { focused ->
                        isSearchFocused.value = focused
                    }
                )

                // Recent Searches Dropdown
                if (isSearchFocused.value && recentSearches.isNotEmpty() && searchText.isEmpty()) {
                    Card(
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recent Searches",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(
                                    onClick = {
                                        recentSearchManager.clearRecentSearches()
                                        recentSearches = emptyList()
                                    }
                                ) {
                                    Text(
                                        text = "Clear",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }

                            HorizontalDivider(
                                Modifier,
                                DividerDefaults.Thickness,
                                DividerDefaults.color
                            )

                            LazyColumn(
                                modifier = Modifier.heightIn(max = 200.dp)
                            ) {
                                items(recentSearches) { recentSearch ->
                                    RecentSearchItem(
                                        searchQuery = recentSearch,
                                        onClick = { selectedQuery ->
                                            searchText = selectedQuery
                                            viewModel.setSearchQuery(selectedQuery)
                                            recentSearchManager.addRecentSearch(selectedQuery)
                                            recentSearches = recentSearchManager.getRecentSearches()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (filteredCustomers.isEmpty() && searchText.isNotEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No customers found")
                }
            } else if (searchText.isEmpty() && !isSearchFocused.value) {
                LazyColumn {
                    items(customers) { customer ->
                        CustomerItem(
                            customer = customer,
                            onClick = {
                                if (fromMain) {
                                    // If called from main menu, just stay on the customer list
                                    // or navigate to a customer detail screen
                                    // For now, we'll just keep them on the customer list
                                } else {
                                    // If called from order creation, return to order screen
                                    navController.previousBackStackEntry?.savedStateHandle?.set(
                                        "selected_customer_id", customer.customerId
                                    )
                                    navController.previousBackStackEntry?.savedStateHandle?.set(
                                        "selected_customer_code", customer.customerCode
                                    )
                                    navController.previousBackStackEntry?.savedStateHandle?.set(
                                        "selected_customer_name", customer.customerName
                                    )
                                    navController.previousBackStackEntry?.savedStateHandle?.set(
                                        "selected_customer_address", customer.alamat
                                    )
                                    // Pass location data as well
                                    navController.previousBackStackEntry?.savedStateHandle?.set(
                                        "selected_customer_latitude", customer.latitude.toString()
                                    )
                                    navController.previousBackStackEntry?.savedStateHandle?.set(
                                        "selected_customer_longitude", customer.longitude.toString()
                                    )
                                    navController.popBackStack()
                                }
                            },
                            onLocationClick = { customer ->
                                // Navigate to location capture screen
                                navController.navigate("location_capture/${customer.customerId}/${customer.customerName}")
                            },
                            onOpenInMaps = { customer ->
                                // Open customer location in Google Maps
                                if (customer.latitude != 0.0 && customer.longitude != 0.0) {
                                    MapUtils.openInGoogleMaps(
                                        context = context,
                                        latitude = customer.latitude,
                                        longitude = customer.longitude,
                                        label = customer.customerName
                                    )
                                }
                            }
                        )
                    }
                }
            } else {
                LazyColumn {
                    items(filteredCustomers) { customer ->
                        CustomerItem(
                            customer = customer,
                            onClick = {
                                if (searchText.isNotBlank()) {
                                    recentSearchManager.addRecentSearch(searchText)
                                    recentSearches = recentSearchManager.getRecentSearches()
                                }
                                if (fromMain) {
                                    // If called from main menu, just stay on the customer list
                                    // or navigate to a customer detail screen
                                    // For now, we'll just keep them on the customer list
                                } else {
                                    // If called from order creation, return to order screen
                                    navController.previousBackStackEntry?.savedStateHandle?.set(
                                        "selected_customer_id", customer.customerId
                                    )
                                    navController.previousBackStackEntry?.savedStateHandle?.set(
                                        "selected_customer_code", customer.customerCode
                                    )
                                    navController.previousBackStackEntry?.savedStateHandle?.set(
                                        "selected_customer_name", customer.customerName
                                    )
                                    navController.previousBackStackEntry?.savedStateHandle?.set(
                                        "selected_customer_address", customer.alamat
                                    )
                                    // Pass location data as well
                                    navController.previousBackStackEntry?.savedStateHandle?.set(
                                        "selected_customer_latitude", customer.latitude.toString()
                                    )
                                    navController.previousBackStackEntry?.savedStateHandle?.set(
                                        "selected_customer_longitude", customer.longitude.toString()
                                    )
                                    navController.popBackStack()
                                }
                            },
                            onLocationClick = { customer ->
                                // Navigate to location capture screen
                                navController.navigate("location_capture/${customer.customerId}/${customer.customerName}")
                            },
                            onOpenInMaps = { customer ->
                                // Open customer location in Google Maps
                                if (customer.latitude != 0.0 && customer.longitude != 0.0) {
                                    MapUtils.openInGoogleMaps(
                                        context = context,
                                        latitude = customer.latitude,
                                        longitude = customer.longitude,
                                        label = customer.customerName
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerItem(
    customer: Customer,
    onClick: () -> Unit,
    onLocationClick: (Customer) -> Unit,
    modifier: Modifier = Modifier,
    onOpenInMaps: (Customer) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Customer Name - Single line with ellipsis
            Text(
                text = customer.customerName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Customer Code - Inline with smaller text
            Text(
                text = customer.customerCode,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Address - Compact single line
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Address",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = customer.alamat,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            if (customer.latitude != 0.0 && customer.longitude != 0.0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location set",
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "±${customer.accuracy.roundToInt()}m accuracy",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row { // Changed from single IconButton to Row of buttons
                IconButton(
                    onClick = { onLocationClick(customer) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Set location",
                        tint = if (customer.latitude != 0.0 && customer.longitude != 0.0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                // New "Open in Maps" button - only show if location is set
                if (customer.latitude != 0.0 && customer.longitude != 0.0) {
                    IconButton(
                        onClick = { onOpenInMaps(customer) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Open in Maps",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }


        }
    }
}