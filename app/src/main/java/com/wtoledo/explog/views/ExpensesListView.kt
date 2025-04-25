package com.wtoledo.explog.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wtoledo.explog.models.Expense
import com.wtoledo.explog.viewModels.ExpensesListViewModel


//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesListView(expensesListViewModel: ExpensesListViewModel) {
    val expenses by expensesListViewModel.expenses.observeAsState(emptyList())
    val isLoading by expensesListViewModel.isLoading.observeAsState(false)
    val errorMessage by expensesListViewModel.errorMessage.observeAsState()

    Scaffold(
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
                            ExpenseItem(expense = expense, onDelete = { expensesListViewModel.deleteExpense(expense) })
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseItem(expense: Expense, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(text = expense.date, style = MaterialTheme.typography.bodyMedium)
                Text(text = expense.description, style = MaterialTheme.typography.bodyMedium)
                Text(text = expense.categoryId.toString(), style = MaterialTheme.typography.bodyMedium)
                Button(
                    onClick = { onDelete() }
                    //modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Eliminar")
                }
            }


            /*Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Monto: ", style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold)
                Text(text = expense.amount.toString(), style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Fecha: ", style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold)
                Text(text = expense.date, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Local: ", style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold)
                Text(text = expense.localName, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Categoría: ", style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold)
                Text(text = expense.categoryId.toString(), style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onDelete() },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Eliminar")
            }*/
        }
    }
}