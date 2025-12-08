package com.elsasa.btrade3.ui.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.elsasa.btrade3.model.OrderItem
import com.elsasa.btrade3.util.MovableFloatingActionButton
import com.elsasa.btrade3.viewmodel.ItemListViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemListScreen(
    navController: NavController,
    viewModel: ItemListViewModel,
    fakturId: String,
    statusSync: String
) {
    val items by viewModel.items.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<OrderItem?>(null) }
    val context = LocalContext.current // Get context here, outside the lambda

    LaunchedEffect(Unit) {
        viewModel.setFakturId(fakturId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Items") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Shortcut button to Order List Screen - only show when there are items
                    if (items.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                // Navigate to Order List Screen (pop back to the main order list)
                                navController.navigate("faktur_list") {
                                    popUpTo("faktur_list") { inclusive = false }
                                    launchSingleTop = true
                                }
                            },
                            modifier = Modifier,
                            enabled = items.isNotEmpty() // Only enabled when there are items
                           ) {
                            Text(
                                "Selesai"
                            )
                        }
                    }
                }

            )
        },
        floatingActionButton = {
            MovableFloatingActionButton(
                onClick = {
                    if (statusSync != "DRAFT") {
                        // Show toast message
                        Toast.makeText(
                            context,
                            "Item cannot be edited",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@MovableFloatingActionButton
                    }

                    navController.navigate("add_barang/$fakturId")
                }
            )
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No items found. Add some items!")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(items) { item ->
                        ItemCard2(
                            item = item,
                            onEditClick = {
                                if (statusSync != "DRAFT") {
                                    // Show toast message
                                    Toast.makeText(
                                        context,
                                        "Item cannot be edited",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@ItemCard2
                                }
                                val itemId = "${item.orderId}-${item.noUrut}"
                                navController.navigate("add_barang/$fakturId?itemId=$itemId")
                            },
                            onDeleteClick = {
                                if (statusSync != "DRAFT") {
                                    // Show toast message
                                    Toast.makeText(
                                        context,
                                        "Item cannot be edited",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@ItemCard2
                                }
                                itemToDelete = item
                                showDeleteDialog = true
                            }
                        )
                    }
                    item {
                        TotalAmountCard(
                            total = items.sumOf { it.lineTotal },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && itemToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                itemToDelete = null
            },
            title = {
                Text("Delete Item")
            },
            text = {
                Text("Are you sure you want to delete this item?\n\n${itemToDelete?.brgName ?: ""}")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemToDelete?.let { item ->
                            viewModel.deleteItem(item)
                        }
                        showDeleteDialog = false
                        itemToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        itemToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ItemCard2(
    item: OrderItem,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onEditClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.brgName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.kategoriName} - Code: ${item.brgCode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Combined quantity-price row
            if (item.qtyBesar > 0 || item.qtyKecil > 0) {
                Column {
                    // FlowRow makes children wrap automatically
                    FlowRow(
                        modifier = Modifier.padding(top = 8.dp),
                        //verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (item.qtyBesar > 0) {
                            QuantityChip(
                                qty = item.qtyBesar,
                                unit = item.satBesar,
                                price = item.unitPrice * item.konversi
                            )
                        }

                        if (item.qtyKecil > 0) {
                            QuantityChip(
                                qty = item.qtyKecil,
                                unit = item.satKecil,
                                price = item.unitPrice
                            )
                        }

                        val totalDisc = item.disc1 + item.disc2 + item.disc3 + item.disc4
                        if (totalDisc > 0) {
                            DiscountChip(item.disc1, item.disc2, item.disc3, item.disc4)
                        }
                    }
                }
            }

            // Total row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = formatCurrency(item.lineTotal),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun QuantityChip(qty: Int, unit: String, price: Double) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), // Slightly transparent
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        modifier = Modifier.height(24.dp) // Fixed height for consistency
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$qty $unit",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = " × ${formatCurrency(price)}",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace)
            )
        }
    }
}

@Composable
private fun DiscountChip(disc1: Double, disc2: Double, disc3: Double, disc4: Double) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), // Slightly transparent
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        modifier = Modifier.height(24.dp) // Fixed height for consistency
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${disc1.formatSmart()}% ${disc2.formatSmart()}% ${disc3.formatSmart()}% ${disc4.formatSmart()}%",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
@Composable
fun TotalAmountCard(
    total: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total Amount",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = formatCurrency(total),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = FontFamily.Monospace),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview
@Composable
fun ItemCardPreview() {
    ItemCard2(
        OrderItem(
            orderId = "123",
            noUrut = 1,
            brgId = "BRG001",
            brgCode = "CD123",
            brgName = "Baju Pria Hitam Lengan Panjang",
            kategoriName = "Pakaian",
            qtyBesar = 2,
            satBesar = "crt",
            qtyKecil = 2,
            satKecil = "pcs",
            qtyBonus = 2,
            konversi = 1,
            unitPrice = 100.0,
            disc1 = 10.0,
            disc2 = 5.0,
            disc3 = 0.0,
            disc4 = 0.0,
            lineTotal = 200.0
        ),
        onEditClick = {  },
        onDeleteClick = {  }
    )
}

private fun formatCurrency(amount: Double): String {
    val locale = Locale.Builder().setLanguage("id").setRegion("ID").build()
    val format = NumberFormat.getCurrencyInstance(locale)
    format.maximumFractionDigits = 0  // This sets the maximum decimal places to 0
    format.minimumFractionDigits = 0
    return format.format(amount)
}

fun Double.formatSmart(): String {
    return if (this % 1 == 0.0) {
        "%.0f".format(this)
    } else {
        this.toString()
    }
}