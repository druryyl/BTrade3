package com.elsasa.btrade3.ui.screen

import android.app.Activity
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.elsasa.btrade3.R
import com.elsasa.btrade3.network.NetworkModule
import com.elsasa.btrade3.util.GoogleSignInHelper
import com.elsasa.btrade3.util.ServerHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    onUserSignedIn: (String) -> Unit,
    context: Context = LocalContext.current
) {
    val googleSignInHelper = remember { GoogleSignInHelper(context) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Server selection
    var selectedServer by remember { mutableStateOf("JOG") }
    var serverOptions by remember { mutableStateOf(listOf<String>()) }
    var isServerLoaded by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Load server data
    LaunchedEffect(Unit) {
        selectedServer = ServerHelper.getSelectedServer(context)

        serverOptions = listOf(
            "JOG - Server Jogja",
            "MGL - Server Magelang"
        )

        isServerLoaded = true
    }

    // Google sign-in launcher
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)
                val email = account.email

                if (email != null) {
                    // Keep isLoading = true while validating
                    scope.launch {
                        try {
                            val apiService = NetworkModule.createApiService()
                            val response = apiService.checkEmailExists(email)
                            if (response.isSuccessful) {
                                val isValid = response.body()?.data?.isValid
                                if (isValid == 1) {
                                    onUserSignedIn(email)
                                    navController.navigate("faktur_list") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                } else {
                                    googleSignInHelper.signOut()
                                    errorMessage = "Akun '$email' tidak terdaftar. Hubungi administrator."
                                    isLoading = false
                                }
                            } else {
                                googleSignInHelper.signOut()
                                errorMessage = "Validasi gagal (${response.code()}). Coba lagi."
                                isLoading = false
                            }
                        } catch (e: Exception) {
                            googleSignInHelper.signOut()
                            errorMessage = "Tidak dapat terhubung ke server. Periksa koneksi internet."
                            isLoading = false
                        }
                    }
                } else {
                    errorMessage = "Failed to retrieve email"
                    isLoading = false
                }
            } catch (e: ApiException) {
                errorMessage = "Google Sign-In failed (${e.statusCode})"
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceContainer,
                            MaterialTheme.colorScheme.surfaceContainer,
                            MaterialTheme.colorScheme.primaryContainer
                            //MaterialTheme.colorScheme.secondaryContainer,
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ───────────────────────────────────────────────────────────
            //  LOGO + TITLE SECTION
            // ───────────────────────────────────────────────────────────
            Spacer(modifier = Modifier.height(60.dp))

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 4.dp,
                modifier = Modifier.size(120.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(R.drawable.sharp_connect_without_contact_24),
                        contentDescription = "Logo",
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Sales Order App",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Sign in to manage your sales orders",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // ───────────────────────────────────────────────────────────
            //  SERVER SELECTION CARD
            // ───────────────────────────────────────────────────────────
            if (isServerLoaded) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {

                        Text(
                            text = "Server Target",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {

                            OutlinedTextField(
                                value = serverOptions.firstOrNull {
                                    it.startsWith("$selectedServer -")
                                } ?: "",
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                label = { Text("Select Server") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
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
                                            val newServer = option.substring(0, 3)
                                            selectedServer = newServer
                                            expanded = false

                                            scope.launch {
                                                ServerHelper.setSelectedServer(context, newServer)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                CircularProgressIndicator()
            }

            Spacer(modifier = Modifier.height(30.dp))

            // ───────────────────────────────────────────────────────────
            //  GOOGLE SIGN-IN BUTTON
            // ───────────────────────────────────────────────────────────
            GoogleSignInButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(52.dp),
                isLoading = isLoading
            ) {
                isLoading = true
                errorMessage = null
                launcher.launch(googleSignInHelper.getSignInIntent())
            }

            // Error Message
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .padding(horizontal = 32.dp)
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

            // FOOTER
            Text(
                text = "Your login session will be remembered",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}


// Custom Google Sign-In Button
@Composable
fun GoogleSignInButton(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 3.dp,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .clickable(enabled = !isLoading) { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.ic_google_logo),
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(22.dp)
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