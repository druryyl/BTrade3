package com.elsasa.btrade3.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.elsasa.btrade3.R
import com.elsasa.btrade3.util.GoogleSignInHelper
import com.elsasa.btrade3.util.ServerHelper
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    onUserSignedIn: (String) -> Unit,
    context: android.content.Context = LocalContext.current
) {
    val googleSignInHelper = remember { GoogleSignInHelper(context) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Server selection state - initialize with default value first
    var selectedServer by remember { mutableStateOf("JOG") } // Default value
    var serverOptions by remember { mutableStateOf(listOf<String>()) }
    var isServerDataLoaded by remember { mutableStateOf(false) }

    // Create a coroutine scope
    val coroutineScope = rememberCoroutineScope()

    // Load server data when composable is launched
    LaunchedEffect(context) {
        // Get the selected server from preferences
        val savedServer = ServerHelper.getSelectedServer(context)
        selectedServer = savedServer

        // Initialize server options
        serverOptions = listOf(
            "JOG - Server Jogja",
            "MGL - Server Magelang"
        )

        isServerDataLoaded = true
    }

    var expanded by remember { mutableStateOf(false) }

    // Get the display text for the selected server
    val selectedServerDisplay = serverOptions.find {
        it.startsWith("${selectedServer} -")
    } ?: "JOG - Server Jogja"

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isLoading = false
        when (result.resultCode) {
            android.app.Activity.RESULT_OK -> {
                try {
                    val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                    account.email?.let { email ->
                        onUserSignedIn(email)
                        navController.navigate("faktur_list") {
                            popUpTo("login") { inclusive = true }
                        }
                    } ?: run {
                        errorMessage = "Failed to get email from Google account"
                    }
                } catch (e: ApiException) {
                    errorMessage = "Google Sign-In failed: ${e.statusCode}"
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // App Logo Section
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.sharp_connect_without_contact_24),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    contentDescription = "App Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                )
            }

            // Title Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Text(
                    text = "Sales Order App",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Sign in to manage your sales orders",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Server Selection Dropdown
            if (isServerDataLoaded) { // Only show when server data is loaded
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Select Server Target",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedServerDisplay,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                                label = { Text("Server") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                serverOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            val newServer = option.substring(0, 3) // Extract "JOG" or "MGL"
                                            selectedServer = newServer
                                            expanded = false

                                            // Save the new server selection using the coroutine scope
                                            coroutineScope.launch {
                                                ServerHelper.setSelectedServer(context, newServer)
                                            }
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Show loading indicator while server data is loading
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Google Sign-In Button
            GoogleSignInButton(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    val signInIntent = googleSignInHelper.getSignInIntent()
                    launcher.launch(signInIntent)
                },
                isLoading = isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp)
                    .height(50.dp)
            )

            // Error Message
            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer
            Text(
                text = "Your login session will be remembered",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

// Custom Google Sign-In Button
@Composable
private fun GoogleSignInButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_google_logo), // Add your Google logo asset
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Continue with Google",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
//
///**
// * Custom Composable for a Google-branded Sign-In Button.
// */
//@Composable
//fun GoogleSignInButton2(onClick: () -> Unit, isLoading: Boolean) {
//    Button(
//        onClick = onClick,
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(56.dp),
//        colors = ButtonDefaults.buttonColors(
//            containerColor = Color.White,
//            contentColor = Color.Black
//        ),
//        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
//        enabled = !isLoading
//    ) {
//        if (isLoading) {
//            CircularProgressIndicator(
//                color = MaterialTheme.colorScheme.primary,
//                modifier = Modifier.size(24.dp)
//            )
//        } else {
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.Center
//            ) {
//                // **IMPORTANT: Replace with your Google logo drawable resource.**
//                Image(
//                    painter = painterResource(id = R.drawable.ic_google_logo),
//                    contentDescription = "Google Logo",
//                    modifier = Modifier.size(24.dp)
//                )
//                Spacer(modifier = Modifier.width(16.dp))
//                Text(
//                    text = "Sign in with Google",
//                    style = MaterialTheme.typography.bodyLarge,
//                    fontWeight = FontWeight.Medium
//                )
//            }
//        }
//    }
//}