package com.elsasa.btrade3.ui.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.elsasa.btrade3.model.CheckIn
import com.elsasa.btrade3.viewmodel.CheckInHistoryViewModel
import java.util.*
import kotlin.math.roundToInt
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInHistoryScreen(
    navController: NavController,
    viewModel: CheckInHistoryViewModel
) {
    val checkIns by viewModel.checkIns.collectAsState()
    val filteredCheckIns by viewModel.filteredCheckIns.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val checkInCounts by viewModel.checkInCounts.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        // Set default to today
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        viewModel.setSelectedDate(today)
        viewModel.loadCheckIns()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Check-In History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Modern Date Filter Card
            ModernDateFilterCard(
                selectedDate = selectedDate,
                checkInCounts = checkInCounts,
                onDateSelected = { viewModel.setSelectedDate(it) },
                onPreviousDay = { viewModel.navigateToDate(-1) },
                onNextDay = { viewModel.navigateToDate(1) },
                context = context
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else if (filteredCheckIns.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Card(
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.size(96.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "No Check-ins",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = if (selectedDate.isNotEmpty()) "No check-ins for selected date" else "No check-in history found",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedDate.isNotEmpty()) "Date: $selectedDate" else "Start checking in to see your history here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(filteredCheckIns) { index, checkIn ->
                        // Calculate sequential number in descending order (most recent first = higher number)
                        val sequentialNumber = filteredCheckIns.size - index
                        ModernCheckInHistoryCard(
                            checkIn = checkIn,
                            sequentialNumber = sequentialNumber,
                            onOpenInMap = { checkIn ->
                                openInGoogleMaps(
                                    context = context,
                                    latitude = checkIn.checkInLatitude,
                                    longitude = checkIn.checkInLongitude,
                                    label = "${checkIn.customerName} - ${checkIn.checkInDate} ${checkIn.checkInTime}"
                                )
                            },
                            onDelete = { checkIn ->
                                viewModel.deleteCheckIn(checkIn.checkInId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernDateFilterCard(
    selectedDate: String,
    checkInCounts: Map<String, Int>,
    onDateSelected: (String) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    context: Context
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter by Date",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (selectedDate.isNotEmpty()) {
                    val count = checkInCounts[selectedDate] ?: 0
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Check-ins",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$count check-ins",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPreviousDay,
                    enabled = selectedDate.isNotEmpty(),
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (selectedDate.isNotEmpty())
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.NavigateBefore,
                        contentDescription = "Previous Day",
                        tint = if (selectedDate.isNotEmpty())
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }

                OutlinedCard(
                    onClick = { showDatePicker = true },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedDate.isNotEmpty()) selectedDate else "Select Date",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onNextDay,
                    enabled = selectedDate.isNotEmpty(),
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (selectedDate.isNotEmpty())
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.NavigateNext,
                        contentDescription = "Next Day",
                        tint = if (selectedDate.isNotEmpty())
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
                            onDateSelected(formattedDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernCheckInHistoryCard(
    checkIn: CheckIn,
    sequentialNumber: Int,
    onOpenInMap: (CheckIn) -> Unit,
    onDelete: (CheckIn) -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with sequential number and customer info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "#$sequentialNumber",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = checkIn.customerName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Code: ${checkIn.customerCode}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Action Buttons
                Row {
                    IconButton(
                        onClick = { onOpenInMap(checkIn) },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Open in Map",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Address
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Address",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = checkIn.customerAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (expanded) 5 else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Date and Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Date",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = checkIn.checkInDate,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = checkIn.checkInTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (checkIn.statusSync == "DRAFT") {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                ) {
                    Text(
                        text = checkIn.statusSync,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (checkIn.statusSync == "DRAFT") {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            // Additional details shown when expanded
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Location Details",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Accuracy: ±${checkIn.accuracy.roundToInt()}m",
                            style = MaterialTheme.typography.bodySmall,
                            color = when (checkIn.accuracy) {
                                in 0f..10f -> MaterialTheme.colorScheme.primary
                                in 11f..50f -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.error
                            }
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Coordinates",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Lat: ${checkIn.checkInLatitude}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Lng: ${checkIn.checkInLongitude}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Delete Check-In",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to delete this check-in?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = checkIn.customerName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "${checkIn.checkInDate} ${checkIn.checkInTime}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(checkIn)
                        showDeleteDialog = false
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Extension function to convert date string to milliseconds
fun String.toMillis(): Long? {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.parse(this)?.time
    } catch (e: Exception) {
        null
    }
}

// Utility function to open location in Google Maps
fun openInGoogleMaps(context: Context, latitude: Double, longitude: Double, label: String = "") {
    val uri = if (label.isNotEmpty()) {
        "geo:$latitude,$longitude?q=$latitude,$longitude($label)"
    } else {
        "geo:$latitude,$longitude"
    }

    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
    intent.setPackage("com.google.android.apps.maps")

    try {
        context.startActivity(intent)
    } catch (e: android.content.ActivityNotFoundException) {
        // Google Maps app not installed, try web version
        val webUri = "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUri))
        context.startActivity(webIntent)
    }
}