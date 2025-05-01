package com.wtoledo.explog.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wtoledo.explog.viewModels.ExpenseViewModel
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.wtoledo.explog.models.Category
import androidx.navigation.NavController
import com.wtoledo.explog.navigation.NavRoutes
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseView(expenseViewModel: ExpenseViewModel, navController: NavController) {
    val expense by expenseViewModel.expense.observeAsState()
    val categories = expenseViewModel.categories.observeAsState(initial = emptyList())

    var showDatePickerDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val isSaveSuccessful by expenseViewModel.isSaveSuccessful.observeAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = isSaveSuccessful) {
        if (isSaveSuccessful == true) {
            snackbarHostState.showSnackbar(
                message = "Gasto guardado correctamente.",
                duration = SnackbarDuration.Short
            )
            delay(5000L)
            expenseViewModel.resetIsSaveSuccessful()
        } else if (isSaveSuccessful == false) {
            snackbarHostState.showSnackbar(
                message = "Error al guardar el gasto.",
                duration = SnackbarDuration.Short
            )
            delay(5000L)
            expenseViewModel.resetIsSaveSuccessful()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de Pago") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    OutlinedTextField(
                        value = expense?.description ?: "",
                        onValueChange = {
                            expenseViewModel.updateDescription(it)
                        },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = expense?.amount?.toString() ?: "",
                        onValueChange = {
                            expenseViewModel.updateAmount(it.toDoubleOrNull() ?: 0.0)
                        },
                        label = { Text("Monto") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = expense?.date ?: "",
                        onValueChange = { },
                        label = { Text("Fecha (YYYY-MM-DD)") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showDatePickerDialog = true }) {
                                Icon(Icons.Filled.DateRange, contentDescription = "Select Date")
                            }
                        }
                    )
                    OutlinedTextField(
                        value = expense?.localName ?: "",
                        onValueChange = {
                            expenseViewModel.updateEstablishmentName(it)
                        },
                        label = { Text("Local") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Categoría",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories.value) { categoryItem ->
                            CategoryIcon(
                                category = categoryItem,
                                isSelected = expense?.categoryId == categoryItem.id,
                                onSelect = {
                                    expenseViewModel.updateCategory(it.name)
                                }
                            )
                        }
                    }
                }// aqui
                Button(
                    onClick = { expenseViewModel.saveExpense() },
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .fillMaxWidth()
                ) {
                    Text("Guardar")
                }

                if (showDatePickerDialog) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePickerDialog = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDatePickerDialog = false
                                    val selectedDate = datePickerState.selectedDateMillis?.let {
                                        SimpleDateFormat(
                                            "yyyy-MM-dd",
                                            Locale.getDefault()
                                        ).format(Date(it))
                                    } ?: ""
                                    expenseViewModel.updateDate(selectedDate)
                                }
                            ) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePickerDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }
            }
            //} antes
        }
    ) // Cierre del Scaffold
}

@Composable
fun CategoryIcon(category: Category, isSelected: Boolean, onSelect: (Category) -> Unit) {
    Card(
        modifier = Modifier
            .size(45.dp)
            .clickable { onSelect(category) },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = category.getImageVector(),
                contentDescription = category.name,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/*
@Preview(showBackground = true)
@Composable
fun ExpenseViewPreview() {
    val previewViewModel = ExpenseViewModel()
    val navController = NavController()

    MaterialTheme {
        Surface {
            ExpenseView(expenseViewModel = previewViewModel)
        }
    }
}*/