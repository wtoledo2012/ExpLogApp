package com.wtoledo.explog.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wtoledo.explog.models.Category
import com.wtoledo.explog.models.Expense
import com.wtoledo.explog.viewModels.ExpensesListViewModel
import com.wtoledo.explog.viewModels.ExpenseViewModel
import androidx.navigation.NavController
import com.wtoledo.explog.navigation.NavRoutes

@Composable
fun ExpensesListView(
    expensesListViewModel: ExpensesListViewModel,
    navController: NavController,
    expenseViewModel: ExpenseViewModel) {
    val expenses by expensesListViewModel.expenses.observeAsState(emptyList())
    val isLoading by expensesListViewModel.isLoading.observeAsState(false)
    val errorMessage by expensesListViewModel.errorMessage.observeAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(NavRoutes.EXPENSES) }) {
                Icon(Icons.Filled.Add, "Agregar Gasto")
            }
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Lista de Gastos",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (errorMessage != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = errorMessage ?: "Unknown error")
                    }
                } else if (expenses.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No hay gastos registrados.")
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(expenses) { expense ->
                            ExpenseItem(
                                expense = expense,
                                onDelete = { expensesListViewModel.deleteExpense(expense) },
                                getCategoryById = expenseViewModel::getCategoryById)
                        }
                    }
                }
            }
        }
    )
}

//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseItem(
    expense: Expense,
    onDelete: () -> Unit,
    getCategoryById: (Int) -> Category?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val category = getCategoryById(expense.categoryId)
            if (category != null) {
                Box(
                    modifier = Modifier
                        .size(48.dp) // Increase the size of the box
                        .wrapContentSize(Alignment.Center) // Center the icon inside the box
                ) {
                    Icon(
                        imageVector = category.getImageVector(),
                        contentDescription = category.name,
                        modifier = Modifier.size(24.dp) // Icon size inside the box
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f) // Take up available space
            ) {
                Text(text = expense.date, style = MaterialTheme.typography.bodyMedium)
                Text(text = expense.description, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(48.dp) // Increase the size of the box
                    .wrapContentSize(Alignment.Center) // Center the icon inside the box
            ) {
                Text(text = "$${expense.amount}", style = MaterialTheme.typography.bodyMedium)
            }
            Button(
                onClick = { onDelete() },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(imageVector = Icons.Filled.Delete, contentDescription = "Eliminar")
            }
        }
    }
}