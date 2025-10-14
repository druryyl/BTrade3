package com.elsasa.btrade3.ui.screen


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.elsasa.btrade3.util.LocationStatus
import com.elsasa.btrade3.viewmodel.LocationCaptureViewModel
import com.elsasa.btrade3.viewmodel.LocationCaptureViewModelFactory
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationCaptureScreen(
    navController: NavController,
    customerId: String,
    customerName: String,
    viewModel: LocationCaptureViewModel
) {
    val locationStatus by viewModel.locationStatus.collectAsState()
    val location by viewModel.location.collectAsState()
    val accuracy by viewModel.accuracy.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val address by viewModel.address.collectAsState()
    val originalLocation by viewModel.originalLocation.collectAsState()

    // Load existing customer location when screen is created
    LaunchedEffect(Unit) {
        viewModel.loadCustomerLocation(customerId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set Location for $customerName") },
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // GPS Status Card
            StatusCard(
                locationStatus = locationStatus,
                accuracy = accuracy,
                isLoading = isLoading,
                hasOriginalLocation = originalLocation != null
            )

            // Location Preview Card
            if (location != null) {
                LocationPreviewCard(
                    location = location!!,
                    accuracy = accuracy,
                    address = address,
                    hasOriginalLocation = originalLocation != null
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        viewModel.saveLocationForCustomer(customerId)
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = locationStatus == LocationStatus.LOCKED
                ) {
                    Text("Save Location")
                }
            }

            // Manual GPS Capture Button

            Button(
                onClick = { viewModel.startLocationCapture() },
                modifier = Modifier.fillMaxWidth(),
                enabled = viewModel.checkLocationPermission()
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "GPS",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Capture New Location")
            }

        }
    }
}

@Composable
fun StatusCard(
    locationStatus: LocationStatus,
    accuracy: Float,
    isLoading: Boolean,
    hasOriginalLocation: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                        LocationStatus.ACQUIRING -> MaterialTheme.colorScheme.secondary // this should be 'warning'
                        LocationStatus.LOCKED -> MaterialTheme.colorScheme.primary
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (locationStatus) {
                        LocationStatus.NO_PERMISSION -> "Permission Required"
                        LocationStatus.NO_SIGNAL -> if (hasOriginalLocation) "No New GPS Signal" else "No Location Set"
                        LocationStatus.ACQUIRING -> "Acquiring Location..."
                        LocationStatus.LOCKED -> "Location Acquired"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (locationStatus) {
                        LocationStatus.NO_PERMISSION -> MaterialTheme.colorScheme.error
                        LocationStatus.NO_SIGNAL -> MaterialTheme.colorScheme.error
                        LocationStatus.ACQUIRING -> MaterialTheme.colorScheme.secondary  // this should be 'warning'
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

            when (locationStatus) {
                LocationStatus.NO_PERMISSION -> {
                    Text(
                        text = "Location permission is required to set customer location.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                LocationStatus.NO_SIGNAL -> {
                    if (hasOriginalLocation) {
                        Text(
                            text = "No new GPS signal available. Using saved location.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    } else {
                        Text(
                            text = "No location set. Capture new location or save without location.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                LocationStatus.ACQUIRING -> {
                    Text(
                        text = "Searching for GPS signal...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                LocationStatus.LOCKED -> {
                    Text(
                        text = "Current location is displayed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun LocationPreviewCard(
    location: android.location.Location?,
    accuracy: Float,
    address: String?,
    hasOriginalLocation: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Location Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (location != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Latitude:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = location.latitude.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Longitude:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = location.longitude.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Accuracy:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "±${accuracy.roundToInt()}m",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = when (accuracy) {
                            in 0f..10f -> Color.Green
                            in 11f..50f -> Color.Yellow
                            else -> Color.Red
                        }
                    )
                }

                // Show address if available
                address?.let { addr ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Address:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = addr,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "No location set. Capture new location or save without location.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}