package com.wtoledo.explog.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wtoledo.explog.models.CategoryExpense
import com.wtoledo.explog.viewModels.GraphViewModel
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import com.wtoledo.explog.ui.theme.pastelColors

@Composable
fun GraphView(graphViewModel: GraphViewModel) {
    val categoryExpenses by graphViewModel.categoryExpenses.observeAsState(emptyList())
    val isLoading by graphViewModel.isLoading.observeAsState(false)
    val errorMessage by graphViewModel.errorMessage.observeAsState()

    Scaffold(
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Gastos por Categoría",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                when {
                    isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    errorMessage != null -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = errorMessage ?: "Unknown error")
                        }
                    }

                    else -> {
                        PieChart(
                            data = categoryExpenses,
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        CategoryExpenseTable(categoryExpenses = categoryExpenses)
                    }
                }
            }
        }
    )
}

@Composable
fun CategoryExpenseTable(categoryExpenses: List<CategoryExpense>) {
    Column(
        modifier = Modifier
            //.fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
                .border(1.dp, Color.Gray)
        ) {
            Text(
                text = "Categoría",
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Total",
                modifier = Modifier
                    .width(100.dp)
                    .padding(8.dp),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.End,
                fontWeight = FontWeight.ExtraBold
            )
        }
        LazyColumn(modifier = Modifier.fillMaxWidth().border(1.dp, Color.Gray)) {
            items(categoryExpenses) { categoryExpense ->
                CategoryExpenseItem(categoryExpense = categoryExpense)
            }
        }
    }
}

@Composable
fun CategoryExpenseItem(categoryExpense: CategoryExpense) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = categoryExpense.categoryName,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Start
        )
        Text(
            text = "$${categoryExpense.totalAmount}",
            modifier = Modifier
                .width(100.dp)
                .padding(end = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End
        )
    }
    Divider(modifier = Modifier.fillMaxWidth())
}

@Composable
fun PieChart(data: List<CategoryExpense>, modifier: Modifier = Modifier) {
    val total = data.sumOf { it.totalAmount }
    val normalizedData = data.map { it.totalAmount / total }
    val startAngles = mutableListOf<Float>()
    var currentAngle = 0f
    normalizedData.forEach { percentage ->
        startAngles.add(currentAngle)
        currentAngle += (percentage * 360f).toFloat()
    }

    var selectedSlice by remember { mutableStateOf<Int?>(null) }
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val center = Offset((canvasWidth / 2).toFloat(), (canvasHeight / 2).toFloat())
                    val radius = min(canvasWidth, canvasHeight) / 2 * 0.8f
                    val touchAngle = atan2(offset.y - center.y, offset.x - center.x)
                    var touchPercent = (touchAngle.toDouble() / (2 * Math.PI))
                    if (touchPercent < 0) {
                        touchPercent += 1
                    }
                    touchPercent = 1 - touchPercent
                    val index = getTouchedSlice(touchPercent, normalizedData)
                    selectedSlice = if (selectedSlice == index) null else index
                }
            }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val center = Offset(canvasWidth / 2, canvasHeight / 2)
            val radius = min(canvasWidth, canvasHeight) / 2 * 0.8f
            startAngles.forEachIndexed { index, startAngle ->
                drawPieSlice(
                    color = pastelColors[index % pastelColors.size],
                    startAngle = startAngle,
                    sweepAngle = (normalizedData[index] * 360f).toFloat(),
                    center = center,
                    radius = radius
                )
            }
            selectedSlice?.let { index ->
                drawTooltip(
                    center = center,
                    radius = radius,
                    categoryName = data[index].categoryName,
                    startAngle = startAngles[index],
                    sweepAngle = (normalizedData[index] * 360f).toFloat(),
                )
            }
        }
    }
}

private fun getTouchedSlice(touchPercent: Double, normalizedData: List<Double>
) : Int {
    var currentPercent = 0.0
    normalizedData.forEachIndexed { index, percent ->
        currentPercent += percent
        if (touchPercent <= currentPercent) {
            return index
        }
    }
    return -1
}

private fun DrawScope.drawTooltip(
    center: Offset,
    radius: Float,
    categoryName: String,
    startAngle: Float,
    sweepAngle: Float
) {
    val tooltipRadius = radius * 0.6f // Adjust for tooltip position
    val midAngleRad = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
    val tooltipX = center.x + tooltipRadius * cos(midAngleRad).toFloat()
    val tooltipY = center.y - tooltipRadius * sin(midAngleRad).toFloat()
    val tooltipOffset = Offset(tooltipX, tooltipY)
    val text = categoryName
    var textSize = 10.dp.toPx()
    val textPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = this@drawTooltip.size.minDimension / textSize
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
    }
    drawContext.canvas.nativeCanvas.drawText(text, tooltipOffset.x, tooltipOffset.y, textPaint)
}

private fun DrawScope.drawPieSlice(
    color: Color,
    startAngle: Float,
    sweepAngle: Float,
    center: Offset,
    radius: Float
) {
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
    )
}