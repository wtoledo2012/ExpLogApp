package com.wtoledo.explog.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.wtoledo.explog.viewModels.ExpenseViewModel
import com.wtoledo.explog.views.ExpenseView
import com.wtoledo.explog.viewModels.ExpensesListViewModel
import com.wtoledo.explog.views.ExpensesListView
import com.wtoledo.explog.viewModels.GraphViewModel
import com.wtoledo.explog.views.GraphView
import com.wtoledo.explog.views.HomeView

@Composable
fun NavGraph(
    navController: NavHostController,
    expenseViewModel: ExpenseViewModel,
    expensesListViewModel: ExpensesListViewModel,
    graphViewModel: GraphViewModel,
    modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = NavRoutes.EXPENSES_LIST, modifier = modifier) {
        composable(NavRoutes.EXPENSES) {
            ExpenseView(expenseViewModel = expenseViewModel , navController = navController)
        }
        composable(NavRoutes.EXPENSES_LIST) {
            ExpensesListView(expensesListViewModel = expensesListViewModel, navController = navController, expenseViewModel = expenseViewModel)
        }
        composable(NavRoutes.HOME) {
            HomeView()
        }
        composable(NavRoutes.GRAPH) {
            GraphView(graphViewModel = graphViewModel)
        }
    }
}

@Composable
fun CenterText(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text)
    }
}