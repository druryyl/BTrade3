package com.elsasa.btrade3.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
    val qtyKecil by viewModel.qtyKecil.collectAsState()
    val qtyBesar by viewModel.qtyBesar.collectAsState()
    val qtyBonus by viewModel.qtyBonus.collectAsState()
    val disc1 by viewModel.disc1.collectAsState()
    val disc2 by viewModel.disc2.collectAsState()
    val disc3 by viewModel.disc3.collectAsState()
    val disc4 by viewModel.disc4.collectAsState()

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
            qtyBesar = qtyBesar,
            qtyKecil = qtyKecil,
            qtyBonus = qtyBonus,
            disc1 = disc1,
            disc2 = disc2,
            disc3 = disc3,
            disc4 = disc4,
            onBarangSelect = {
                navController.navigate("barang_selection") {
                    popUpTo("add_barang") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onQtyBesarChange = { viewModel.setQtyBesar(it) },
            onQtyKecilChange = { viewModel.setQtyKecil(it) },
            onQtyBonusChange = { viewModel.setQtyBonus(it) },
            onDisc1Change = { viewModel.setDisc1(it) },
            onDisc2Change = { viewModel.setDisc2(it) },
            onDisc3Change = { viewModel.setDisc3(it) },
            onDisc4Change = { viewModel.setDisc4(it) },
            onSave = {
                if (itemId == null) {
                    viewModel.saveItem()
                } else {
                    viewModel.updateItem()
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
    qtyBesar: Int,
    qtyKecil: Int,
    qtyBonus: Int,
    disc1: Double,
    disc2: Double,
    disc3: Double,
    disc4: Double,
    onBarangSelect: () -> Unit,
    onQtyBesarChange: (Int) -> Unit,
    onQtyKecilChange: (Int) -> Unit,
    onQtyBonusChange: (Int) -> Unit,
    onDisc1Change: (Double) -> Unit,
    onDisc2Change: (Double) -> Unit,
    onDisc3Change: (Double) -> Unit,
    onDisc4Change: (Double) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Select Item Card
        Card(
            elevation = CardDefaults.cardElevation(2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Item",
                    style = MaterialTheme.typography.titleMedium
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
            // Item Info
            Card(
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = selectedBarang.brgName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Code: ${selectedBarang.brgCode} (${selectedBarang.brgId})",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Category: ${selectedBarang.kategoriName}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Qty Input Card
            Card(
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (selectedBarang.konversi > 1) {
                        QtyInputField(
                            label = "Qty Besar (${selectedBarang.satBesar})",
                            value = qtyBesar,
                            onValueChange = onQtyBesarChange
                        )
                    }

                    QtyInputField(
                        label = "Qty Kecil (${selectedBarang.satKecil})",
                        value = qtyKecil,
                        onValueChange = onQtyKecilChange
                    )

                    QtyInputField(
                        label = "Qty Bonus (${selectedBarang.satKecil})",
                        value = qtyBonus,
                        onValueChange = onQtyBonusChange
                    )
                }
            }

            // Discount Input Card
            Card(
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Discounts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Arrange discounts in grid (2 columns)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        maxItemsInEachRow = 2
                    ) {
                        DiscountInputField(
                            label = "Disc 1 (%)",
                            value = disc1,
                            onValueChange = onDisc1Change,
                            modifier = Modifier.weight(1f)
                        )
                        DiscountInputField(
                            label = "Disc 2 (%)",
                            value = disc2,
                            onValueChange = onDisc2Change,
                            modifier = Modifier.weight(1f)
                        )
                        DiscountInputField(
                            label = "Disc 3 (%)",
                            value = disc3,
                            onValueChange = onDisc3Change,
                            modifier = Modifier.weight(1f)
                        )
                        DiscountInputField(
                            label = "Disc 4 (%)",
                            value = disc4,
                            onValueChange = onDisc4Change,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // --- inside AddBarangContent, after Discounts Card ---
            Card(
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Order Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    val totalBesar = selectedBarang.hrgSat * selectedBarang.konversi * qtyBesar
                    val totalKecil = selectedBarang.hrgSat * qtyKecil
                    val subTotal = totalBesar + totalKecil

                    // Discount calculations
                    val disc1Amount = subTotal * disc1 / 100
                    val afterDisc1 = subTotal - disc1Amount
                    val disc2Amount = afterDisc1 * disc2 / 100
                    val afterDisc2 = afterDisc1 - disc2Amount
                    val disc3Amount = afterDisc2 * disc3 / 100
                    val afterDisc3 = afterDisc2 - disc3Amount
                    val disc4Amount = afterDisc3 * disc4 / 100
                    val lineTotal = afterDisc3 - disc4Amount

                    Spacer(Modifier.height(8.dp))

                    // --- Qty Besar row ---
                    if (qtyBesar > 0) {
                        SummaryRow(
                            label = selectedBarang.satBesar,
                            qty = qtyBesar,
                            unitPrice = selectedBarang.hrgSat * selectedBarang.konversi,
                            total = totalBesar
                        )
                    }

                    // --- Qty Kecil row ---
                    if (qtyKecil > 0) {
                        SummaryRow(
                            label = selectedBarang.satKecil,
                            qty = qtyKecil,
                            unitPrice = selectedBarang.hrgSat,
                            total = totalKecil
                        )
                    }

                    // --- Qty Bonus ---
                    if (qtyBonus > 0) {
                        SummaryRow(
                            label = "${selectedBarang.satKecil} (Bonus)",
                            qty = qtyBonus,
                            unitPrice = 0.0,
                            total = 0.0
                        )
                    }
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                    // --- Discounts breakdown ---
                    if (disc1 > 0) DiscountRow("Disc 1 (${disc1}%)", disc1Amount)
                    if (disc2 > 0) DiscountRow("Disc 2 (${disc2}%)", disc2Amount)
                    if (disc3 > 0) DiscountRow("Disc 3 (${disc3}%)", disc3Amount)
                    if (disc4 > 0) DiscountRow("Disc 4 (${disc4}%)", disc4Amount)

                    HorizontalDivider(thickness = 2.dp, color = DividerDefaults.color)

                    // --- Line Total ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Line Total:",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Text(
                            text = formatCurrency(lineTotal),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
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

@Composable
fun QtyInputField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    OutlinedTextField(
        value = if (value == 0) "" else value.toString(),
        onValueChange = { newText ->
            val intValue = newText.toIntOrNull() ?: 0
            onValueChange(intValue.coerceAtLeast(0))
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun DiscountInputField(
    label: String,
    value: Double,
    onValueChange: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var textValue by remember(value) { mutableStateOf(if (value == 0.0) "" else value.toString()) }

    OutlinedTextField(
        value = textValue,
        onValueChange = { newText ->
            textValue = newText
            newText.toDoubleOrNull()?.let {
                onValueChange(it.coerceIn(0.0, 100.0))
            }
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        trailingIcon = { Text("%") },
        modifier = modifier
    )
}

// --- helper composables ---
@Composable
fun SummaryRow(label: String, qty: Int, unitPrice: Double, total: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label  $qty × ${formatCurrency(unitPrice)}",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace
            )
        )
        Text(
            text = formatCurrency(total),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
fun DiscountRow(label: String, amount: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace
            )
        )
        Text(
            text = "-${formatCurrency(amount)}",
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace
            ),
            color = MaterialTheme.colorScheme.error
        )
    }
}


private fun formatCurrency(amount: Double): String {
    val locale = Locale.Builder().setLanguage("id").setRegion("ID").build()
    val format = NumberFormat.getCurrencyInstance(locale)
    format.maximumFractionDigits = 0  // This sets the maximum decimal places to 0
    format.minimumFractionDigits = 0
    return format.format(amount)
}