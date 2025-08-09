package com.elsasa.btrade3.ui.screen

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.elsasa.btrade3.model.Barang
import com.elsasa.btrade3.ui.component.SearchBar
import com.elsasa.btrade3.util.RecentSearchManager
import com.elsasa.btrade3.viewmodel.BarangSelectionViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarangSelectionScreen(
    navController: NavController,
    viewModel: BarangSelectionViewModel,
    context: Context = LocalContext.current
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val barangs by viewModel.barangs.collectAsState()
    val isSearchFocused = remember { mutableStateOf(false) }

    var searchText by rememberSaveable { mutableStateOf(searchQuery) }
    val recentSearchManager = remember { RecentSearchManager(context, "barang") }
    var recentSearches by remember { mutableStateOf(recentSearchManager.getRecentSearches()) }

    val filteredBarangs = remember(barangs, searchText) {
        if (searchText.isEmpty()) {
            barangs
        } else {
            barangs.filter { barang ->
                barang.brgCode.lowercase().contains(searchText.lowercase()) ||
                barang.brgName.lowercase().contains(searchText.lowercase()) ||
                barang.kategoriName.lowercase().contains(searchText.lowercase())
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Item") },
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

            if (filteredBarangs.isEmpty() && searchText.isNotEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No items found")
                }
            } else if (searchText.isEmpty() && !isSearchFocused.value) {
                LazyColumn {
                    items(filteredBarangs) { barang ->
                        BarangItem(
                            barang = barang,
                            onClick = {
                                // Navigate back with selected barang
                                navController.previousBackStackEntry?.savedStateHandle?.set(
                                    "selected_barang", barang
                                )
                                navController.popBackStack()
                            }
                        )
                    }
                }
            } else {
                // Show filtered items
                LazyColumn {
                    items(filteredBarangs) { barang ->
                        BarangItem(
                            barang = barang,
                            onClick = {
                                // Save the search query when an item is selected
                                if (searchText.isNotBlank()) {
                                    recentSearchManager.addRecentSearch(searchText)
                                    recentSearches = recentSearchManager.getRecentSearches()
                                }

                                // Navigate back with selected barang
                                navController.previousBackStackEntry?.savedStateHandle?.set(
                                    "selected_barang", barang
                                )
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }        }
    }
}

@Composable
fun RecentSearchItem(
    searchQuery: String,
    onClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(searchQuery) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Recent search",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = searchQuery,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun BarangItemOriginal(
    barang: Barang,
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
                text = "${barang.brgName} (${barang.brgCode})",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Category: ${barang.kategoriName}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Stock: ${barang.stok}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${barang.satKecil}: ${formatCurrency(barang.hrgSat)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BarangItem(
    barang: Barang,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.small,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Line 1: Product Name
            Text(
                text = barang.brgName,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            // Line 2: Code • Category • Unit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = barang.brgCode,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = " • ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Text(
                    text = barang.kategoriName,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = " • ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Text(
                    text = barang.satKecil,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Line 3: Price and Stock
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StockBadge(stock = barang.stok)
                Text(
                    text = formatCurrency(barang.hrgSat),
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun StockBadge(stock: Int) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(20.dp)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(
                color = when {
                    stock <= 0 -> MaterialTheme.colorScheme.errorContainer
                    stock < 10 -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.secondaryContainer
                }
            )
            .padding(horizontal = 6.dp)
    ) {
        Text(
            text = "Stock: $stock",
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
            color = when {
                stock <= 0 -> MaterialTheme.colorScheme.onErrorContainer
                stock < 10 -> MaterialTheme.colorScheme.onTertiaryContainer
                else -> MaterialTheme.colorScheme.onSecondaryContainer
            }
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