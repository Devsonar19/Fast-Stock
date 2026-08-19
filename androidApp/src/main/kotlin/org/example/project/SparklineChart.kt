package org.example.project

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun SparklineChart(dataPoints: List<Float>, color: Color, modifier: Modifier = Modifier) {
    if (dataPoints.isEmpty()) return

    val max = dataPoints.maxOrNull() ?: 0f
    val min = dataPoints.minOrNull() ?: 0f
    val range = max - min

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val stepX = width / (dataPoints.size - 1).coerceAtLeast(1)
        val path = Path()

        dataPoints.forEachIndexed { index, value ->
            // Normalize the Y coordinate so the lowest point is at the bottom, highest at top
            val yOffset = if (range == 0f) height / 2 else height - ((value - min) / range * height)
            val xOffset = index * stepX

            if (index == 0) {
                path.moveTo(xOffset, yOffset)
            } else {
                // Creates straight line segments. For curved lines, you'd use quadraticBezierTo
                path.lineTo(xOffset, yOffset)
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 4f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}