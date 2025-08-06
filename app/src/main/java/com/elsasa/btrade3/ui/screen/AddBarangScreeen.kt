package com.elsasa.btrade3.ui.screen

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.elsasa.btrade3.model.Barang
import com.elsasa.btrade3.viewmodel.AddBarangViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBarangScreen(
    navController: NavController,
    viewModel: AddBarangViewModel,
    fakturId: String,
    itemId: String? = null
) {
    val selectedBarang by viewModel.selectedBarang.collectAsState()
    val qty by viewModel.qty.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.setFakturId(fakturId)
        itemId?.let {
            id -> viewModel.loadItemForEditing(id) }
    }

    // Handle result from BarangSelectionScreen
    val selectedBarangResult = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<Barang>("selected_barang")

    LaunchedEffect(selectedBarangResult) {
        selectedBarangResult?.let { barang ->
            viewModel.selectBarang(barang)
            navController.currentBackStackEntry?.savedStateHandle?.remove<Barang>("selected_barang")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if(itemId == null) "Add Item" else "Edit Item")},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        AddBarangContent(
            selectedBarang = selectedBarang,
            qty = qty,
            onBarangSelect = {
                navController.navigate("barang_selection") {
                    popUpTo("add_barang") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onQtyChange = { viewModel.setQty(it) },
            onSave = {
                if (itemId == null) {
                    viewModel.saveItem()
                } else {
                    viewModel.updateItem(itemId)
                }
                navController.popBackStack()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}

@Composable
fun AddBarangContent(
    selectedBarang: Barang?,
    qty: Int,
    onBarangSelect: () -> Unit,
    onQtyChange: (Int) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Log.d("AddBarangTag", "Recomposing with barang: ${selectedBarang?.brgName}")

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Item",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onBarangSelect,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = selectedBarang?.brgName ?: "Search for an item..."
                    )
                }
            }
        }

        if (selectedBarang != null) {
            Card(elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = selectedBarang.brgName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Code: ${selectedBarang.brgCode}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Category: ${selectedBarang.kategoriName}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Unit: ${selectedBarang.satKecil}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Price: ${formatCurrency(selectedBarang.hrgSat)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Card(elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Quantity",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (qty > 1) onQtyChange(qty - 1)
                            }
                        ) {
                            Text("-", style = MaterialTheme.typography.headlineMedium)
                        }
                        Text(
                            text = qty.toString(),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        IconButton(
                            onClick = { onQtyChange(qty + 1) }
                        ) {
                            Text("+", style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }
            }

            Card(elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Line Total:",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = formatCurrency(selectedBarang.hrgSat * qty),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add to Order")
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