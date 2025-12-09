package com.elsasa.btrade3.ui.screen

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.elsasa.btrade3.repository.OrderSyncRepository
import com.elsasa.btrade3.ui.getUserEmail
import com.elsasa.btrade3.viewmodel.OrderSyncViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderSyncScreen(
    navController: NavController,
    userEmail: String, // This will be used for sync operations
    viewModel: OrderSyncViewModel
) {
    val syncState by viewModel.syncState.collectAsState()
    val context = LocalContext.current // Get context here instead of passing it

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sync Transaction") },
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
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Sync Draft Orders",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Send your draft orders to the server",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            // Sync Status Card
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Sync Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    when (val state = syncState) {
                        is OrderSyncRepository.SyncResult.Loading -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Preparing to sync...",
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }

                        is OrderSyncRepository.SyncResult.Progress -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Syncing order: ${state.orderCode}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${state.current} of ${state.total}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                LinearProgressIndicator(
                                progress = { state.current.toFloat() / state.total.toFloat() },
                                modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .height(4.dp),
                                color = ProgressIndicatorDefaults.linearColor,
                                trackColor = ProgressIndicatorDefaults.linearTrackColor,
                                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                                )
                            }
                        }

                        is OrderSyncRepository.SyncResult.Success -> {
                            val iconColor = if (isSystemInDarkTheme()) Color.Green else Color(0xFF2E7D32)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Success",
                                    tint = iconColor
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = iconColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }

                        is OrderSyncRepository.SyncResult.Error -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme .error
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }

            // Sync Button
            Button(
                onClick = { viewModel.syncDraftOrdersWithProgress() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = syncState !is OrderSyncRepository.SyncResult.Loading &&
                        syncState !is OrderSyncRepository.SyncResult.Progress
            ) {
                if (syncState is OrderSyncRepository.SyncResult.Loading ||
                    syncState is OrderSyncRepository.SyncResult.Progress) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sync Draft Orders")
                    }
                }
            }
            Button(
                onClick = {
                    // Use the passed userEmail directly
                    viewModel.syncDraftCheckInsWithProgress(userEmail, context)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = syncState !is OrderSyncRepository.SyncResult.Loading &&
                        syncState !is OrderSyncRepository.SyncResult.Progress
            ) {
                if (syncState is OrderSyncRepository.SyncResult.Loading ||
                    syncState is OrderSyncRepository.SyncResult.Progress) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Sync Check-ins",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sync Check-In Data")
                    }
                }
            }


            // Info Card
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Only draft orders will be synced",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Successfully synced orders will be marked as SENT",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Orders with SENT status won't be synced again",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}