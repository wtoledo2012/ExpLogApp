package com.wtoledo.explog.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.wtoledo.explog.viewModels.ExpenseViewModel
import com.wtoledo.explog.viewModels.ExpensesListViewModel
import com.wtoledo.explog.viewModels.GraphViewModel
import com.wtoledo.explog.views.ExpenseView
import com.wtoledo.explog.views.ExpensesListView
import com.wtoledo.explog.views.GraphView
import com.wtoledo.explog.views.HomeView

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavigationItem("Gastos", Icons.Filled.Menu, NavRoutes.EXPENSES_LIST),
        BottomNavigationItem("Inicio", Icons.Filled.Home, NavRoutes.HOME),
        BottomNavigationItem("Gráfico", Icons.Filled.Place, NavRoutes.GRAPH)
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
