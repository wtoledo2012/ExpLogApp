package com.wtoledo.explog.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wtoledo.explog.viewModels.ExpenseViewModel
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.text.style.TextAlign
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.wtoledo.explog.models.Category
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseView(expenseViewModel: ExpenseViewModel) {
    val expense by expenseViewModel.expense.observeAsState()
    val categories = expenseViewModel.categories

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
        } else if (isSaveSuccessful == false){
            snackbarHostState.showSnackbar(
                message = "Error al guardar el gasto.",
                duration = SnackbarDuration.Short
            )
            delay(5000L)
            expenseViewModel.resetIsSaveSuccessful()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Registro de Pago",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Descripción",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    OutlinedTextField(
                        value = expense?.description ?: "",
                        onValueChange = {
                            //description = it
                            expenseViewModel.updateDescription(it)
                        },
                        //label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Monto",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    OutlinedTextField(
                        value = expense?.amount?.toString() ?: "",
                        onValueChange = {
                            //amount = it
                            expenseViewModel.updateAmount(it.toDoubleOrNull() ?: 0.0)
                        },
                        //label = { Text("Amount") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Fecha (YYYY-MM-DD)",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    OutlinedTextField(
                        value = expense?.date ?: "",
                        onValueChange = {
                            //date = it
                            //expenseViewModel.updateDate(it)
                        },
                        //label = { Text("Date (YYYY-MM-DD)") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showDatePickerDialog = true }) {
                                Icon(Icons.Filled.DateRange, contentDescription = "Select Date")
                            }
                        }
                    )

                    Text(
                        text = "Local",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    OutlinedTextField(
                        value = expense?.establishmentName ?: "",
                        onValueChange = {
                            //establishmentName = it
                            expenseViewModel.updateEstablishmentName(it)
                        },
                        //label = { Text("Establishment Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Categoría",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { categoryItem ->
                            CategoryIcon(
                                category = categoryItem,
                                isSelected = expense?.categoryId == categoryItem.id,
                                onSelect = {
                                    //category = it.name
                                    expenseViewModel.updateCategory(it.name)
                                }
                            )
                        }
                    }

                    Button(
                        onClick = { expenseViewModel.saveExpense() },
                        modifier = Modifier
                            .padding(top = 24.dp)
                            .fillMaxWidth()
                    ) {
                        Text("Save")
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
                                        //date = selectedDate
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
            }
        }
    ) // Cierre del Scaffold
}

@Composable
fun CategoryIcon(category: Category, isSelected: Boolean, onSelect: (Category) -> Unit) {
    Card(
        modifier = Modifier
            .size(90.dp)
            .clickable { onSelect(category) },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
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

@Preview(showBackground = true)
@Composable
fun ExpenseViewPreview() {
    val previewViewModel = ExpenseViewModel()

    MaterialTheme {
        Surface {
            ExpenseView(expenseViewModel = previewViewModel)
        }
    }
}
