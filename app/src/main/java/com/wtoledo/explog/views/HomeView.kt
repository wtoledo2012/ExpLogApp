package com.wtoledo.explog.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wtoledo.explog.viewModels.HomeViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.wtoledo.explog.navigation.NavRoutes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.wtoledo.explog.ui.theme.ExpLogTheme

@Composable
fun HomeView(
    homeViewModel: HomeViewModel = viewModel(),
    navController: NavController
) {
    val currentMonthExpensesSum by homeViewModel.currentMonthExpensesSum.observeAsState(initial = 0.0)
    val previousMonthExpensesSum by homeViewModel.previousMonthExpensesSum.observeAsState(initial = 0.0)
    val lastFiveExpenses by homeViewModel.lastFiveExpenses.observeAsState(initial = emptyList())
    val isLoading by homeViewModel.isLoading.observeAsState(false)
    val errorMessage by homeViewModel.errorMessage.observeAsState(null)

    val scrollState = rememberScrollState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(NavRoutes.EXPENSES.route) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Expense")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Bienvenido a ExpLog",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                // Comparación de Gastos Mes Actual Vs Anterior
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Comparativa de Gastos:",
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Composable de las Barras de Comparación
                        ExpenseComparisonBarChart(
                            currentMonthExpenses = currentMonthExpensesSum.toFloat(),
                            previousMonthExpenses = previousMonthExpensesSum.toFloat()
                        )
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))

                // Gastos del Mes Actual
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Gastos del Mes Actual:",
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else if (errorMessage != null) {
                            Text(
                                text = "Error: $errorMessage",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp
                            )
                        } else {
                            Text(
                                text = "$${String.format("%.2f", currentMonthExpensesSum)}",
                                textAlign = TextAlign.Center,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Gastos del Mes Anterior
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Gastos del Mes Anterior:",
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$${String.format("%.2f", previousMonthExpensesSum)}",
                            textAlign = TextAlign.Center,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ultimos 5 Gastos
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Los últimos 5 gastos:",
                            textAlign = TextAlign.Start,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 8.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (lastFiveExpenses.isEmpty()) {
                            Text(
                                "No hay gastos recientes.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        } else {
                            lastFiveExpenses.forEach { expense ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${expense.description} (${expense.date})",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "$${String.format("%.2f", expense.amount)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun ExpenseComparisonBarChart(
    currentMonthExpenses: Float,
    previousMonthExpenses: Float,
    barColorCurrentMonth: Color = MaterialTheme.colorScheme.primary,
    barColorPreviousMonth: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    barWidth: Dp = 40.dp,
    chartHeight: Dp = 150.dp, //200
    minBarHeight: Dp = 8.dp,
    labelHeight: Dp = 20.dp
) {
    val maxValue = maxOf(currentMonthExpenses, previousMonthExpenses, 1f)
    val currentMonthHeightRatio = if (currentMonthExpenses > 0) {
        (currentMonthExpenses / maxValue).coerceAtLeast(minBarHeight.value / chartHeight.value)
    } else {
        0f
    }
    val previousMonthHeightRatio = if (previousMonthExpenses > 0) {
        (previousMonthExpenses / maxValue).coerceAtLeast(minBarHeight.value / chartHeight.value)
    } else {
        0f
    }

    val animatedCurrentMonthHeightRatio by animateFloatAsState(
        targetValue = currentMonthHeightRatio,
        animationSpec = tween(durationMillis = 600), label = "CurrentMonthBarAnimation"
    )
    val animatedPreviousMonthHeightRatio by animateFloatAsState(
        targetValue = previousMonthHeightRatio,
        animationSpec = tween(durationMillis = 600), label = "PreviousMonthBarAnimation"
    )

    val currentMonthExpensesDouble = currentMonthExpenses.toDouble()
    val previousMonthExpensesDouble = previousMonthExpenses.toDouble()

    // Calcular la diferencia y el porcentaje para la leyenda
    val difference = currentMonthExpensesDouble - previousMonthExpensesDouble
    val percentageDifference = if (previousMonthExpensesDouble != 0.0) {
        (difference / previousMonthExpensesDouble) * 100
    } else if (currentMonthExpensesDouble > 0) {
        100.0
    } else {
        0.0 // Si ambos son 0
    }

    val differenceText = String.format("%.2f", difference)
    val percentageText = String.format("%.1f", percentageDifference)

    // Determinar el color del texto de la diferencia
    val differenceColor = when {
        difference > 0 -> Color.Red // Rojo si los gastos actuales son mayores
        difference < 0 -> Color.Green // Verde si los gastos actuales son menores
        else -> MaterialTheme.colorScheme.onSurface // Color por defecto si son iguales
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            //.height(chartHeight + 30.dp)
            .height(chartHeight + 4.dp + labelHeight + 8.dp + 30.dp)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                //.height(chartHeight - 20.dp),
                .height(chartHeight),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // Barra del Mes Anterior
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .fillMaxHeight(animatedPreviousMonthHeightRatio)
                        .background(barColorPreviousMonth)
                        //.weight(animatedPreviousMonthHeightRatio, fill = true)
                )
                /*Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ant.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )*/
            }

            // Barra del Mes Actual
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .fillMaxHeight(animatedCurrentMonthHeightRatio)
                        .background(barColorCurrentMonth)
                        //.weight(animatedCurrentMonthHeightRatio, fill = true)
                )
                /*Spacer(modifier = Modifier.height(4.dp)) // Espacio entre la barra y el texto
                Text(
                    text = "Actual",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )*/
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(labelHeight),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "Ant.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(barWidth)
            )
            Text(
                text = "Actual",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(barWidth)
            )
        }

        Spacer(modifier = Modifier.height(8.dp)) // Espacio entre las barras y la leyenda

        // Leyenda de Diferencia y Porcentaje
        Text(
            text = "$${differenceText} (${percentageText}%) vs Mes Anterior: $${String.format("%.2f", previousMonthExpensesDouble)}",
            color = differenceColor,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// Puedes añadir previews para visualizar el Composable del gráfico
@Preview(showBackground = true)
@Composable
fun PreviewExpenseComparisonBarChart() {
    ExpLogTheme { // Asegúrate de usar el tema de tu aplicación si tienes uno
        ExpenseComparisonBarChart(
            currentMonthExpenses = 1500.50f,
            previousMonthExpenses = 1200.75f
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewExpenseComparisonBarChartEqual() {
    ExpLogTheme {
        ExpenseComparisonBarChart(
            currentMonthExpenses = 1000f,
            previousMonthExpenses = 1000f
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewExpenseComparisonBarChartCurrentHigher() {
    ExpLogTheme {
        ExpenseComparisonBarChart(
            currentMonthExpenses = 2000f,
            previousMonthExpenses = 500f
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewExpenseComparisonBarChartPreviousHigher() {
    ExpLogTheme {
        ExpenseComparisonBarChart(
            currentMonthExpenses = 300f,
            previousMonthExpenses = 1800f
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewExpenseComparisonBarChartZeroPrevious() {
    ExpLogTheme {
        ExpenseComparisonBarChart(
            currentMonthExpenses = 200f,
            previousMonthExpenses = 0f
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewExpenseComparisonBarChartZeroCurrent() {
    ExpLogTheme {
        ExpenseComparisonBarChart(
            currentMonthExpenses = 0f,
            previousMonthExpenses = 500f
        )
    }
}


// Asegúrate de tener tu tema definido, por ejemplo:
// En ui.theme/Theme.kt
/*
package com.wtoledo.explog.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun ExpLogTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}
*/