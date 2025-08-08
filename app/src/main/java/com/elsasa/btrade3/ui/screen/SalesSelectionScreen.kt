package com.elsasa.btrade3.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.elsasa.btrade3.model.SalesPerson
import com.elsasa.btrade3.viewmodel.SalesSelectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesSelectionScreen(
    navController: NavController,
    viewModel: SalesSelectionViewModel
) {
    val salesPersons by viewModel.salesPersons.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Sales Person") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (salesPersons.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No sales persons found")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(salesPersons) { salesPerson ->
                    SalesPersonItem(
                        salesPerson = salesPerson,
                        onClick = {
                            // Navigate back with selected sales person
                            navController.previousBackStackEntry?.savedStateHandle?.set(
                                "selected_sales_name", salesPerson.salesPersonName
                            )
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SalesPersonItem(
    salesPerson: SalesPerson,
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
                text = salesPerson.salesPersonName,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Code: ${salesPerson.salesPersonCode}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}