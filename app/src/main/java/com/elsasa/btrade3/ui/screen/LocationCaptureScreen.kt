package com.elsasa.btrade3.ui.screen


import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.elsasa.btrade3.model.Customer
import com.elsasa.btrade3.util.LocationStatus
import com.elsasa.btrade3.util.LocationUtils
import com.elsasa.btrade3.util.MapUtils
import com.elsasa.btrade3.viewmodel.LocationCaptureViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationCaptureScreen(
    navController: NavController,
    customerId: String,
    customerName: String,
    customerAddress: String,
    customerCity: String,
    userEmail: String, // Add userEmail parameter
    viewModel: LocationCaptureViewModel,
) {
    val locationStatus by viewModel.locationStatus.collectAsState()
    val location by viewModel.location.collectAsState()
    val accuracy by viewModel.accuracy.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val address by viewModel.address.collectAsState()
    val originalLocation by viewModel.originalLocation.collectAsState()
    val nearbyCustomers by viewModel.nearbyCustomers.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCustomerLocation(customerId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set Customer Location") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Card
            item {
                StatusCard(
                    locationStatus = locationStatus,
                    accuracy = accuracy,
                    isLoading = isLoading,
                    hasOriginalLocation = originalLocation != null
                )
            }

            // Location Preview + Capture Button
            item {
                LocationPreviewCard(
                    customerName = customerName,
                    customerAddress = customerAddress,
                    customerCity = customerCity,
                    location = location,
                    accuracy = accuracy,
                    address = address,
                    hasOriginalLocation = originalLocation != null,
                    onCaptureClick = { viewModel.startLocationCapture() },
                    canCapture = viewModel.checkLocationPermission(),
                )
            }

            // Action Buttons (Cancel / Save)
            item {
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
                            viewModel.saveLocationForCustomer(customerId, userEmail)
                            navController.popBackStack()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = location != null
                    ) {
                        Text("Save Location")
                    }
                }
            }

            // Nearby Customers (Scrollable)
            item {
                if (location != null && nearbyCustomers.isNotEmpty()) {
                    NearbyCustomersCard(
                        nearbyCustomers = nearbyCustomers,
                        currentLocation = location!!,
                        onCustomerClick = { customer ->
                            navController.navigate("location_capture/${customer.customerId}/${customer.customerName}")
                        }
                    )
                } else if (location != null && nearbyCustomers.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = "Nearby",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Nearby Customers",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No customers found within 100 meters.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun InfoCard(icon: ImageVector, title: String, message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun NearbyCustomersCard(
    nearbyCustomers: List<Customer>,
    currentLocation: android.location.Location,
    onCustomerClick: (Customer) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.People, contentDescription = "Nearby", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Nearby Customers (${nearbyCustomers.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Scrollable area within limited height
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp)
            ) {
                items(nearbyCustomers) { customer ->
                    val distance = LocationUtils.calculateDistance(
                        currentLocation.latitude,
                        currentLocation.longitude,
                        customer.latitude,
                        customer.longitude
                    )
                    CustomerItem(customer, distance, onClick = { onCustomerClick(customer) })
                }
            }
        }
    }
}

@Composable
fun CustomerItem(customer: Customer, distance: Float, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(customer.customerName, fontWeight = FontWeight.Medium)
                Text(customer.customerCode, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    customer.alamat,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Distance:${distance.toInt()}m",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Accuracy:±${customer.accuracy.roundToInt()}m",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
    customerName: String,
    customerAddress: String,
    customerCity: String,
    location: android.location.Location?,
    accuracy: Float,
    address: String?,
    hasOriginalLocation: Boolean,
    onCaptureClick: () -> Unit,
    canCapture: Boolean,
    context: Context = LocalContext.current
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            //verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Customer name header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top // Change from CenterVertically to Top
            ) {
                Column {
                    Text(
                        text = customerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = customerAddress,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = customerCity,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Normal
                    )
                }
                if (location != null) {
                    IconButton(
                        onClick = {
                            MapUtils.openInGoogleMaps(
                                context = context,
                                latitude = location.latitude,
                                longitude = location.longitude,
                                label = customerName
                            )
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Open Map",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }
            if (location != null) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 3.dp,
                    color = DividerDefaults.color
                )

                // Compact line: Lat | Long | Accuracy
                Text(
                    text = buildString {
                        append("Lat: %.5f".format(location.latitude))
                        append("   |   Lon: %.5f".format(location.longitude))
                        append("   |   ±${accuracy.roundToInt()}m")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Optional address line
                address?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Text(
                    text = "No location set. Capture new location or save without location.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )
            Button(
                onClick = onCaptureClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = canCapture
            ) {
                Icon(
                    imageVector = Icons.Default.LocationSearching,
                    contentDescription = "GPS",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Capture New Location")
            }
        }
    }
}
