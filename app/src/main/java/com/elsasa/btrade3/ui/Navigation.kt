package com.elsasa.btrade3.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.elsasa.btrade3.database.AppDatabase
import com.elsasa.btrade3.network.NetworkModule
import com.elsasa.btrade3.repository.BarangRepository
import com.elsasa.btrade3.repository.CustomerRepository
import com.elsasa.btrade3.repository.FakturRepository
import com.elsasa.btrade3.repository.NetworkRepository
import com.elsasa.btrade3.repository.SalesPersonRepository
import com.elsasa.btrade3.repository.SyncRepository
import com.elsasa.btrade3.ui.screen.AddBarangScreen
import com.elsasa.btrade3.ui.screen.BarangSelectionScreen
import com.elsasa.btrade3.ui.screen.CustomerSelectionScreen
import com.elsasa.btrade3.ui.screen.FakturEntryScreen
import com.elsasa.btrade3.ui.screen.FakturListScreen
import com.elsasa.btrade3.ui.screen.ItemListScreen
import com.elsasa.btrade3.ui.screen.LoginScreen
import com.elsasa.btrade3.ui.screen.SalesSelectionScreen
import com.elsasa.btrade3.ui.screen.SyncScreen
import com.elsasa.btrade3.viewmodel.AddBarangViewModel
import com.elsasa.btrade3.viewmodel.AddBarangViewModelFactory
import com.elsasa.btrade3.viewmodel.BarangSelectionViewModel
import com.elsasa.btrade3.viewmodel.BarangSelectionViewModelFactory
import com.elsasa.btrade3.viewmodel.CustomerSelectionViewModel
import com.elsasa.btrade3.viewmodel.CustomerSelectionViewModelFactory
import com.elsasa.btrade3.viewmodel.FakturEntryViewModel
import com.elsasa.btrade3.viewmodel.FakturEntryViewModelFactory
import com.elsasa.btrade3.viewmodel.FakturListViewModel
import com.elsasa.btrade3.viewmodel.FakturListViewModelFactory
import com.elsasa.btrade3.viewmodel.ItemListViewModel
import com.elsasa.btrade3.viewmodel.ItemListViewModelFactory
import com.elsasa.btrade3.viewmodel.SalesSelectionViewModel
import com.elsasa.btrade3.viewmodel.SalesSelectionViewModelFactory
import com.elsasa.btrade3.viewmodel.SyncViewModel
import com.elsasa.btrade3.viewmodel.SyncViewModelFactory

@Composable
fun AppNavigation(
    navController: NavHostController,
    database: AppDatabase
) {
    val context = LocalContext.current
    val fakturRepository = FakturRepository(
        database.fakturDao(),
        database.fakturItemDao()
    )
    val barangRepository = BarangRepository(database.barangDao())
    val customerRepository = CustomerRepository(database.customerDao())
    val salesPersonRepository = SalesPersonRepository(database.salesPersonDao()) // Add this

    val apiService = NetworkModule.createApiService()
    val networkRepository = NetworkRepository(apiService)
    val syncRepository = SyncRepository(networkRepository, barangRepository, customerRepository, salesPersonRepository)

    val isLoggedIn = remember { checkIfUserIsLoggedIn(context) }

    NavHost(
        navController = navController,
        startDestination =  if (isLoggedIn) "faktur_list" else "login"
    ) {
        composable("login") {
            LoginScreen(
                navController = navController,
                onUserSignedIn = { email ->
                    saveUserEmail(context,email) // Save to SharedPreferences
                }
            )
        }

        composable("faktur_list") {
            val viewModel: FakturListViewModel = viewModel(
                factory = FakturListViewModelFactory(fakturRepository)
            )
            FakturListScreen(navController, viewModel)
        }

        composable(
            "faktur_entry/{fakturId}",
            arguments = listOf(navArgument("fakturId") { type = NavType.StringType })
        ) { backStackEntry ->
            val fakturId = backStackEntry.arguments?.getString("fakturId")
            val context = LocalContext.current // Get context here
            val viewModel: FakturEntryViewModel = viewModel(
                factory = FakturEntryViewModelFactory(fakturRepository, context)
            )
            FakturEntryScreen(navController, viewModel, fakturId)
        }

        composable("customer_selection") {
            val viewModel: CustomerSelectionViewModel = viewModel(
                factory = CustomerSelectionViewModelFactory(customerRepository)
            )
            CustomerSelectionScreen(navController, viewModel)
        }

        composable("sales_selection") {
            val viewModel: SalesSelectionViewModel = viewModel(
                factory = SalesSelectionViewModelFactory(salesPersonRepository)
            )
            SalesSelectionScreen(navController, viewModel)
        }

        composable(
            "item_list/{fakturId}",
            arguments = listOf(navArgument("fakturId") { type = NavType.StringType })
        ) { backStackEntry ->
            val fakturId = backStackEntry.arguments?.getString("fakturId") ?: ""
            val viewModel: ItemListViewModel = viewModel(
                factory = ItemListViewModelFactory(fakturRepository)
            )
            ItemListScreen(navController, viewModel, fakturId)
        }

        composable(
            "add_barang/{fakturId}?itemId={itemId}",
            arguments = listOf(
                navArgument("fakturId") { type = NavType.StringType },
                navArgument("itemId") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val fakturId = backStackEntry.arguments?.getString("fakturId") ?: ""
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            val viewModel: AddBarangViewModel = viewModel(
                factory = AddBarangViewModelFactory(fakturRepository) // Updated
            )
            AddBarangScreen(navController, viewModel, fakturId, itemId.ifEmpty { null })
        }

        composable("barang_selection") {
            val viewModel: BarangSelectionViewModel = viewModel(
                factory = BarangSelectionViewModelFactory(barangRepository)
            )
            BarangSelectionScreen(navController, viewModel)
        }
        composable("sync") {
            val viewModel: SyncViewModel = viewModel(
                factory = SyncViewModelFactory(syncRepository)
            )
            SyncScreen(navController, viewModel)
        }
    }
}

// Updated helper functions using modern approach
private const val PREFS_NAME = "sales_order_prefs"
private const val USER_EMAIL_KEY = "user_email"

private fun checkIfUserIsLoggedIn(context: android.content.Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
    return prefs.getString(USER_EMAIL_KEY, null) != null
}

private fun saveUserEmail(context: android.content.Context, email: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
    prefs.edit { putString(USER_EMAIL_KEY, email) }
}

fun getUserEmail(context: android.content.Context): String? {
    val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
    return prefs.getString(USER_EMAIL_KEY, null)
}

fun logoutUser(context: android.content.Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
    prefs.edit { remove(USER_EMAIL_KEY) }
}