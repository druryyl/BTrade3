package com.elsasa.btrade3.ui.screen

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.filled.FmdGood // A more modern location pin icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview

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
                            onManageLocationClick = { customer ->
                                // Navigate to location capture screen
                                navController.navigate("location_capture/${customer.customerId}/${customer.customerName}/${customer.alamat}/${customer.wilayah}")
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
                            onManageLocationClick = { customer ->
                                // Navigate to location capture screen
                                navController.navigate("location_capture/${customer.customerId}/${customer.customerName}/${customer.alamat}/${customer.wilayah}")
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
    onManageLocationClick: (Customer) -> Unit,
    onOpenInMaps: (Customer) -> Unit,
    modifier: Modifier = Modifier
) {
    val hasLocation = customer.latitude != 0.0 && customer.longitude != 0.0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (hasLocation) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = customer.customerName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (hasLocation) {
//                    Text(
//                        text = "±${customer.accuracy.roundToInt()}m",
//                        style = MaterialTheme.typography.labelSmall,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                    Spacer(Modifier.width(4.dp))
//                    Icon(
//                        imageVector = Icons.Default.MyLocation,
//                        contentDescription = "Accurate",
//                        modifier = Modifier.size(14.dp),
//                        tint = MaterialTheme.colorScheme.primary
//                    )
                    IconButton(
                        onClick = { onOpenInMaps(customer) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Open Map",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }                }
            }

            Text(
                text = customer.customerCode,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 1.dp, bottom = 2.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Address",
                    modifier = Modifier.size(13.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = customer.alamat,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(6.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(Modifier.height(3.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton(
                    onClick = { onManageLocationClick(customer) },
                    modifier = Modifier
                        .size(28.dp)
                        .padding(end = 8.dp) //
                ) {
                    Icon(
                        imageVector = Icons.Default.EditLocationAlt,
                        contentDescription = "Manage Location",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                if (hasLocation) {
//                    IconButton(
//                        onClick = { onOpenInMaps(customer) },
//                        modifier = Modifier.size(28.dp)
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Map,
//                            contentDescription = "Open Map",
//                            tint = MaterialTheme.colorScheme.primary
//                        )
//                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CustomerItemPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Column(Modifier.padding(8.dp)) {
            CustomerItem(
                customer = Customer(
                    customerId = "1",
                    customerName = "John Doe Clinic",
                    customerCode = "CUST-00123",
                    alamat = "Jl. Mawar No. 45, Jakarta Selatan",
                    latitude = -6.2000,
                    longitude = 106.8167,
                    accuracy = 5.2f,
                    locationTimestamp = System.currentTimeMillis(),
                    wilayah = "AAA",

                ),
                onClick = {},
                onManageLocationClick = {},
                onOpenInMaps = {}
            )

            Spacer(Modifier.height(8.dp))

            CustomerItem(
                customer = Customer(
                    customerId = "1",
                    customerName = "Sunrise Pharmacy",
                    customerCode = "CUST-00987",
                    alamat = "Jl. Kenanga Raya No. 88, Bandung",
                    latitude = 0.0,
                    longitude = 0.0,
                    accuracy = 30.5f,
                    locationTimestamp = System.currentTimeMillis(),
                    wilayah = "AAA",
                ),
                onClick = {},
                onManageLocationClick = {},
                onOpenInMaps = {}
            )
        }
    }
}