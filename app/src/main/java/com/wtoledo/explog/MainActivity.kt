package com.wtoledo.explog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.wtoledo.explog.navigation.BottomNavigationBar
import com.wtoledo.explog.navigation.NavGraph
import com.wtoledo.explog.ui.theme.ExpLogTheme
import com.wtoledo.explog.viewModels.ExpenseViewModel
import com.wtoledo.explog.viewModels.ExpensesListViewModel
import com.wtoledo.explog.viewModels.GraphViewModel

class MainActivity : ComponentActivity() {

    private val expensesListViewModel: ExpensesListViewModel by viewModels()
    private val expenseViewModel: ExpenseViewModel by viewModels()
    private val graphViewModel: GraphViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpLogTheme {
                //ExpenseView(expenseViewModel = expenseViewModel)
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = { BottomNavigationBar(navController = navController) }
                ) { padding ->
                    NavGraph(
                        navController = navController,
                        expenseViewModel = expenseViewModel,
                        expensesListViewModel = expensesListViewModel,
                        graphViewModel = graphViewModel,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}
/*
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ExpLogTheme {
        Greeting("Android")
    }
}*/