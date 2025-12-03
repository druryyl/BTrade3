package com.elsasa.btrade3.ui.screen

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.KeyboardBackspace
import androidx.compose.material.icons.automirrored.filled.LabelOff
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.elsasa.btrade3.util.LocationStatus
import com.elsasa.btrade3.viewmodel.CheckInViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(
    navController: NavController,
    userEmail: String,
    viewModel: CheckInViewModel
) {
    val locationStatus by viewModel.locationStatus.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val accuracy by viewModel.accuracy.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val nearbyCustomers by viewModel.nearbyCustomers.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val currentAddress by viewModel.currentAddress.collectAsState() // Add this line

    LaunchedEffect(Unit) {
        viewModel.loadNearbyCustomers()
        viewModel.startLocationCapture()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Check In") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // Make the entire column scrollable
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current Location Status Card
// Current Location Status Card
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "GPS Status",
                            tint = when (locationStatus) {
                                LocationStatus.NO_PERMISSION -> MaterialTheme.colorScheme.error
                                LocationStatus.NO_SIGNAL -> MaterialTheme.colorScheme.error
                                LocationStatus.ACQUIRING -> MaterialTheme.colorScheme.error
                                LocationStatus.LOCKED -> MaterialTheme.colorScheme.primary
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (locationStatus) {
                                LocationStatus.NO_PERMISSION -> "Permission Required"
                                LocationStatus.NO_SIGNAL -> "No GPS Signal"
                                LocationStatus.ACQUIRING -> "Acquiring Location..."
                                LocationStatus.LOCKED -> "Location Acquired"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (locationStatus) {
                                LocationStatus.NO_PERMISSION -> MaterialTheme.colorScheme.error
                                LocationStatus.NO_SIGNAL -> MaterialTheme.colorScheme.error
                                LocationStatus.ACQUIRING -> MaterialTheme.colorScheme.secondary
                                LocationStatus.LOCKED -> MaterialTheme.colorScheme.primary
                            }
                        )
                    }

                    if (isLoading) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Current Location Details
                    currentLocation?.let { location ->
                        Column {
                            Text(
                                text = "Current Location:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Lat: ${location.latitude}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Lng: ${location.longitude}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Accuracy: ±${accuracy.roundToInt()}m",
                                style = MaterialTheme.typography.bodySmall,
                                color = when (accuracy) {
                                    in 0f..10f -> Color.Green
                                    in 11f..50f -> Color.Yellow
                                    else -> Color.Red
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Current Address Section
                            Text(
                                text = "Current Address:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            currentAddress?.let { address ->
                                Text(
                                    text = address,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } ?: run {
                                Text(
                                    text = "Address not available",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    } ?: run {
                        Text(
                            text = "No location available",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }


            // Check In Button (moved to top of selected customer section)
            selectedCustomer?.let { customer ->
                Button(
                    onClick = {
                        viewModel.checkIn(userEmail)
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Check In",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Check In - ${customer.customerName}")
                }
                // Selected Customer Card
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = customer.customerName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Code: ${customer.customerCode}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = customer.alamat,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(
                                onClick = { viewModel.unselectCustomer() },
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Remove")
                                //Text("Remove")
                            }
                        }
                    }
                }
            }

            // Nearby Customers Card
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Nearby",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Nearby Customers (${nearbyCustomers.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (nearbyCustomers.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 300.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(nearbyCustomers) { customer ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectCustomer(customer)
                                        },
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = customer.customerName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = customer.customerCode,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = customer.alamat,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (currentLocation != null) "No customers found within 100 meters" else "Waiting for location...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Refresh Location Button
            OutlinedButton(
                onClick = {
                    viewModel.startLocationCapture() // This will also update the address
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = viewModel.checkLocationPermission()
            ) {
                Text("Refresh Location")
            }
        }
    }
}