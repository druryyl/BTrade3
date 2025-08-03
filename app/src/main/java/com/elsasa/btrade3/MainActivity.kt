package com.elsasa.btrade3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.elsasa.btrade3.database.AppDatabase
import com.elsasa.btrade3.ui.AppNavigation
import com.elsasa.btrade3.ui.theme.BTrade3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BTrade3Theme {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    SalesOrderApp()
                }
            }
        }
    }
}

@Composable
fun SalesOrderApp() {
    val navController = rememberNavController()
    val database = AppDatabase.getDatabase(LocalContext.current)

    AppNavigation(navController, database)
}