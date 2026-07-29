package com.housiness.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.housiness.HousinessApp
import com.housiness.ui.screens.*
import kotlinx.coroutines.launch

enum class BottomTab(val label: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    PRODUCTS("Products", Icons.Filled.Inventory2, Icons.Outlined.Inventory2),
    ORDERS("Orders", Icons.Filled.ListAlt, Icons.Outlined.ListAlt),
    MORE("More", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val app = context.applicationContext as HousinessApp
    val settingsRepo = app.serviceLocator.settingsRepository
    val scope = rememberCoroutineScope()

    var showSetup by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        val settings = settingsRepo.get()
        showSetup = settings == null || settings.businessName.isBlank()
    }

    val setupPending = showSetup
    if (setupPending == true) {
        SetupScreen(
            settingsRepository = settingsRepo,
            onComplete = { showSetup = false }
        )
        return
    }

    val navController = rememberNavController()
    val tabs = BottomTab.entries

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                tabs.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.name } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.name) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomTab.HOME.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomTab.HOME.name) {
                HomeScreen(
                    onNavigateToProducts = {
                        navController.navigate(BottomTab.PRODUCTS.name) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(BottomTab.PRODUCTS.name) { ProductsScreen() }
            composable(BottomTab.ORDERS.name) { OrdersScreen() }
            composable(BottomTab.MORE.name) { SettingsScreen() }
        }
    }
}
