package com.elsasa.btrade3.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.elsasa.btrade3.ui.component.FakturCard
import com.elsasa.btrade3.ui.logoutUser
import com.elsasa.btrade3.viewmodel.FakturListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FakturListScreen(
    navController: NavController,
    viewModel: FakturListViewModel,
    context: android.content.Context = LocalContext.current
) {
    val fakturs by viewModel.fakturs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sales Orders") },
                actions = {
                    IconButton(onClick = {
                        logoutUser(context)
                        navController.navigate("login"){
                            popUpTo("faktur_list") {inclusive = true}
                        }
                    }){
                        Icon(Icons.Outlined.Face, contentDescription = "Logout")
                    }
                    IconButton(onClick = {
                        navController.navigate("sync")
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync Data")
                    }
                },


            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("faktur_entry/new")
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        if (fakturs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No sales orders found. Create a new one!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(fakturs) { faktur ->
                    FakturCard(
                        faktur = faktur,
                        onEditClick = {
                            navController.navigate("faktur_entry/${faktur.fakturId}")
                        },
                        onDeleteClick = {
                            viewModel.deleteFaktur(faktur)
                        }
                    )
                }
            }
        }
    }
}