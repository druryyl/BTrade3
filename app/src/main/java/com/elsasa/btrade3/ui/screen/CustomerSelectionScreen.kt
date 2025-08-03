package com.elsasa.btrade3.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.elsasa.btrade3.model.Customer
import com.elsasa.btrade3.ui.component.SearchBar
import com.elsasa.btrade3.viewmodel.CustomerSelectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSelectionScreen(
    navController: NavController,
    viewModel: CustomerSelectionViewModel
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredCustomers = viewModel.getFilteredCustomers()

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
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) },
                onSearch = { /* Handle search if needed */ },
                placeholder = "Search by code, name, or address"
            )

            if (filteredCustomers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text("No customers found")
                }
            } else {
                LazyColumn {
                    items(filteredCustomers) { customer ->
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
                text = customer.address,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}