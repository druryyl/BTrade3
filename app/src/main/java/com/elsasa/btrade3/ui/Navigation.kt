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
import com.elsasa.btrade3.repository.OrderRepository
import com.elsasa.btrade3.repository.NetworkRepository
import com.elsasa.btrade3.repository.OrderSyncRepository
import com.elsasa.btrade3.repository.SalesPersonRepository
import com.elsasa.btrade3.repository.SyncRepository
import com.elsasa.btrade3.ui.screen.AddBarangScreen
import com.elsasa.btrade3.ui.screen.BarangSelectionScreen
import com.elsasa.btrade3.ui.screen.CustomerSelectionScreen
import com.elsasa.btrade3.ui.screen.OrderEntryScreen
import com.elsasa.btrade3.ui.screen.OrderListScreen
import com.elsasa.btrade3.ui.screen.ItemListScreen
import com.elsasa.btrade3.ui.screen.LoginScreen
import com.elsasa.btrade3.ui.screen.OrderSyncScreen
import com.elsasa.btrade3.ui.screen.SalesSelectionScreen
import com.elsasa.btrade3.ui.screen.SyncScreen
import com.elsasa.btrade3.viewmodel.AddBarangViewModel
import com.elsasa.btrade3.viewmodel.AddBarangViewModelFactory
import com.elsasa.btrade3.viewmodel.BarangSelectionViewModel
import com.elsasa.btrade3.viewmodel.BarangSelectionViewModelFactory
import com.elsasa.btrade3.viewmodel.CustomerSelectionViewModel
import com.elsasa.btrade3.viewmodel.CustomerSelectionViewModelFactory
import com.elsasa.btrade3.viewmodel.OrderEntryViewModel
import com.elsasa.btrade3.viewmodel.OrderEntryViewModelFactory
import com.elsasa.btrade3.viewmodel.OrderListViewModel
import com.elsasa.btrade3.viewmodel.OrderListViewModelFactory
import com.elsasa.btrade3.viewmodel.ItemListViewModel
import com.elsasa.btrade3.viewmodel.ItemListViewModelFactory
import com.elsasa.btrade3.viewmodel.OrderSyncViewModel
import com.elsasa.btrade3.viewmodel.OrderSyncViewModelFactory
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
    val orderRepository = OrderRepository(
        database.orderDao(),
        database.orderItemDao()
    )
    val barangRepository = BarangRepository(database.barangDao())
    val customerRepository = CustomerRepository(database.customerDao())
    val salesPersonRepository = SalesPersonRepository(database.salesPersonDao()) // Add this

    val apiService = NetworkModule.createApiService()
    val networkRepository = NetworkRepository(apiService)
    val syncRepository = SyncRepository(networkRepository, barangRepository, customerRepository, salesPersonRepository)
    val orderSyncRepository = OrderSyncRepository(apiService, orderRepository)

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
            val viewModel: OrderListViewModel = viewModel(
                factory = OrderListViewModelFactory(orderRepository)
            )
            OrderListScreen(navController, viewModel)
        }

        composable(
            "faktur_entry/{orderId}/{statusSync}",
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val fakturId = backStackEntry.arguments?.getString("orderId")
            val statusSync = backStackEntry.arguments?.getString("statusSync")
            val context = LocalContext.current // Get context here
            val viewModel: OrderEntryViewModel = viewModel(
                factory = OrderEntryViewModelFactory(orderRepository, context)
            )
            OrderEntryScreen(navController, viewModel, fakturId, statusSync)
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
            "item_list/{orderId}/{statusSync}",
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            val statusSync = backStackEntry.arguments?.getString("statusSync") ?: ""
            val viewModel: ItemListViewModel = viewModel(
                factory = ItemListViewModelFactory(orderRepository)
            )
            ItemListScreen(navController, viewModel, orderId, statusSync)
        }

        composable(
            "add_barang/{orderId}?itemId={itemId}",
            arguments = listOf(
                navArgument("orderId") { type = NavType.StringType },
                navArgument("itemId") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val fakturId = backStackEntry.arguments?.getString("orderId") ?: ""
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            val viewModel: AddBarangViewModel = viewModel(
                factory = AddBarangViewModelFactory(orderRepository) // Updated
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
        composable("order_sync") {
            val viewModel: OrderSyncViewModel = viewModel(
                factory = OrderSyncViewModelFactory(orderSyncRepository)
            )
            OrderSyncScreen(navController, viewModel)
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