package com.wtoledo.explog.navigation

enum class NavRoutes(val route: String) {
    EXPENSES("expenses"),
    EXPENSES_LIST("expenses_list"),
    HOME("home"),
    GRAPH("graph"),
    SCAN_EXP("scan_exp");

    object ExpensesList {
        val route = EXPENSES_LIST.route
    }
    object Expense {
        val route = EXPENSES.route
    }
    object Home {
        val route = HOME.route
    }
    object Graph {
        val route = GRAPH.route
    }
    object ScanExp {
        val route = SCAN_EXP.route
    }
}