package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.CurrencyFormatter

data class CategorySpendItem(
    val categoryName: String,
    val amount: Double,
    val percentage: Float, // 0 to 100
    val color: Color
)

data class BarChartItem(
    val label: String,
    val amount: Double
)

data class TrendPoint(
    val label: String,
    val amount: Double
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryDonutChart(
    items: List<CategorySpendItem>,
    totalAmount: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty() || totalAmount <= 0.0) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No expenses recorded for this period",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800),
        label = "donut_anim"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 32.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val centerOffset = Offset(size.width / 2, size.height / 2)
                val arcSize = Size(radius * 2, radius * 2)
                val topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius)

                var currentStartAngle = -90f
                for (item in items) {
                    val sweepAngle = (item.amount / totalAmount * 360f).toFloat() * animProgress
                    if (sweepAngle > 0f) {
                        drawArc(
                            color = item.color,
                            startAngle = currentStartAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        currentStartAngle += sweepAngle
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Total Spent",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyFormatter.format(totalAmount, currencySymbol),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.take(8).forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(item.color, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${item.categoryName} (${String.format("%.1f", item.percentage)}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyBarChart(
    items: List<BarChartItem>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty() || items.all { it.amount <= 0.0 }) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No monthly spending data to compare",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val maxAmount = (items.maxOfOrNull { it.amount } ?: 1.0).coerceAtLeast(10.0)
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.primaryContainer

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            items.forEach { item ->
                val ratio = (item.amount / maxAmount).toFloat().coerceIn(0.04f, 1f)
                val heightProgress by animateFloatAsState(
                    targetValue = ratio,
                    animationSpec = tween(durationMillis = 600),
                    label = "bar_anim"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    if (item.amount > 0) {
                        Text(
                            text = if (item.amount >= 1000) "${(item.amount / 1000).toInt()}k" else "${item.amount.toInt()}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height((130 * heightProgress).dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    listOf(primaryColor, secondaryColor)
                                ),
                                shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                            )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun DailyTrendLineChart(
    points: List<TrendPoint>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    if (points.size < 2 || points.all { it.amount <= 0.0 }) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Add more transactions across days to see trend line",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val maxVal = (points.maxOfOrNull { it.amount } ?: 1.0).coerceAtLeast(10.0)
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            val width = size.width
            val height = size.height
            val stepX = width / (points.size - 1)

            val linePath = Path()
            val fillPath = Path()

            val coordinates = points.mapIndexed { index, point ->
                val x = index * stepX
                val yRatio = (point.amount / maxVal).toFloat()
                val y = height - (yRatio * (height - 24.dp.toPx())) - 8.dp.toPx()
                Offset(x, y)
            }

            // Grid lines
            drawLine(
                color = surfaceColor,
                start = Offset(0f, height),
                end = Offset(width, height),
                strokeWidth = 1.dp.toPx()
            )

            // Build path
            linePath.moveTo(coordinates.first().x, coordinates.first().y)
            fillPath.moveTo(coordinates.first().x, height)
            fillPath.lineTo(coordinates.first().x, coordinates.first().y)

            for (i in 1 until coordinates.size) {
                val prev = coordinates[i - 1]
                val curr = coordinates[i]
                val controlX = (prev.x + curr.x) / 2
                linePath.cubicTo(controlX, prev.y, controlX, curr.y, curr.x, curr.y)
                fillPath.cubicTo(controlX, prev.y, controlX, curr.y, curr.x, curr.y)
            }

            fillPath.lineTo(coordinates.last().x, height)
            fillPath.close()

            // Draw gradient fill
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.35f),
                        primaryColor.copy(alpha = 0.02f)
                    ),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw line
            drawPath(
                path = linePath,
                color = primaryColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw points
            coordinates.forEachIndexed { idx, coord ->
                if (points[idx].amount > 0) {
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = coord
                    )
                    drawCircle(
                        color = primaryColor,
                        radius = 2.5.dp.toPx(),
                        center = coord
                    )
                }
            }
        }

        // Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val labelCount = kotlin.math.min(5, points.size)
            val step = if (points.size > 1) (points.size - 1) / (labelCount - 1) else 1
            for (i in 0 until points.size step (if (step <= 0) 1 else step)) {
                Text(
                    text = points[i].label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
