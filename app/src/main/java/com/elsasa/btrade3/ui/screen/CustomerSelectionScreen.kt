package com.elsasa.btrade3.ui.screen

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.elsasa.btrade3.model.Customer
import com.elsasa.btrade3.ui.component.SearchBar
import com.elsasa.btrade3.util.RecentSearchManager
import com.elsasa.btrade3.viewmodel.CustomerSelectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSelectionScreen(
    navController: NavController,
    viewModel: CustomerSelectionViewModel,
    context: Context = LocalContext.current
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val isSearchFocused = remember {mutableStateOf(false)}

    var searchText by rememberSaveable { mutableStateOf(searchQuery) }
    val recentSearchManager = remember { RecentSearchManager(context, "customer") }
    var recentSearches by remember { mutableStateOf(recentSearchManager.getRecentSearches()) }

    val filteredCustomers = remember(customers, searchText){
        if (searchQuery.isEmpty()) {
            customers
        } else {
            customers.filter { customer ->
                customer.customerCode.contains(searchQuery, ignoreCase = true) ||
                customer.customerName.contains(searchQuery, ignoreCase = true) ||
                customer.alamat.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Customer") },
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
                    placeholder = "Search by code, name, or category",
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
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text("No customers found")
                }
            } else if (searchText.isEmpty() && !isSearchFocused.value) {
                LazyColumn {
                    items(customers) { customer ->
                        CustomerItem(
                            customer = customer,
                            onClick = {
                                // Navigate back with selected customer
                                navController.previousBackStackEntry?.savedStateHandle?.set(
                                    "selected_customer_code", customer.customerCode
                                )
                                navController.previousBackStackEntry?.savedStateHandle?.set(
                                    "selected_customer_name", customer.customerName
                                )
                                navController.popBackStack()
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
                                navController.previousBackStackEntry?.savedStateHandle?.set(
                                    "selected_customer_code", customer.customerCode
                                )
                                navController.previousBackStackEntry?.savedStateHandle?.set(
                                    "selected_customer_name", customer.customerName
                                )
                                navController.popBackStack()
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
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "${customer.customerCode} - ${customer.customerName}",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = customer.alamat,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}