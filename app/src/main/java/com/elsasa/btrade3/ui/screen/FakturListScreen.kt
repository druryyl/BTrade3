package com.elsasa.btrade3.ui.screen

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.elsasa.btrade3.model.Faktur
import com.elsasa.btrade3.ui.component.ModernFakturCard
import com.elsasa.btrade3.ui.logoutUser
import com.elsasa.btrade3.util.MovableFloatingActionButton
import com.elsasa.btrade3.viewmodel.FakturListViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FakturListScreen(
    navController: NavController,
    viewModel: FakturListViewModel,
    context: Context = LocalContext.current
) {
    val fakturs by viewModel.fakturs.collectAsState()

    var fakturToDelete by remember { mutableStateOf<Faktur?>(null) }

    // Delete confirmation dialog
    if (fakturToDelete != null) {
        AlertDialog(
            onDismissRequest = { fakturToDelete = null },
            title = { Text("Delete Sales Order") },
            text = {
                Text(
                    "Are you sure you want to delete this sales order?\n\n" +
                            (fakturToDelete?.customerName ?: "")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        fakturToDelete?.let { viewModel.deleteFaktur(it) }
                        fakturToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { fakturToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Sales Orders",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = {
                        logoutUser(context)
                        navController.navigate("login") {
                            popUpTo("faktur_list") { inclusive = true }
                        }
                    }) {
                        Icon(
                            Icons.Outlined.Face,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { navController.navigate("sync") }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Sync Data",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = { navController.navigate("faktur_entry/new") },
//                containerColor = MaterialTheme.colorScheme.primary
//            ) {
//                Icon(Icons.Default.Add, contentDescription = "Add")
//            }
//        }
        floatingActionButton = {
            MovableFloatingActionButton(
                onClick = { navController.navigate("faktur_entry/new") },
                modifier = Modifier
                    .padding(end = 16.dp, bottom = 16.dp) // Initial position
            )
        }
    ) { padding ->
        if (fakturs.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No sales orders found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Create a new one to get started",
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
                items(fakturs) { faktur ->
                    ModernFakturCard(
                        faktur = faktur,
                        onEditClick = {
                            navController.navigate("faktur_entry/${faktur.fakturId}")
                        },
                        onDeleteClick = {
                            fakturToDelete = faktur // Trigger dialog
                        },
                        onSyncClick = {}
                    )
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