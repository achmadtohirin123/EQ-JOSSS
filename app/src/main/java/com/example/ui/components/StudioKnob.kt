package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StudioKnob(
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedRange<Float>,
    label: String,
    modifier: Modifier = Modifier,
    unit: String = "",
    accentColor: Color = Color(0xFF00FFCC) // Neon Cyan
) {
    var dragAccumulator by remember { mutableStateOf(0f) }

    val coercedValue = value.coerceIn(range.start, range.endInclusive)
    val fraction = (coercedValue - range.start) / (range.endInclusive - range.start)

    // Knob rotation angles (from 135deg (min) to 405deg (max))
    val minAngle = 135f
    val maxAngle = 405f
    val sweepAngle = maxAngle - minAngle
    val currentAngle = minAngle + fraction * sweepAngle

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(70.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { dragAccumulator = 0f },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            // Drag up reduces, drag down increases (or vice versa; moving up usually increases values, so dragAmount.y negative means up)
                            val sensitivity = 0.003f
                            val deltaInValueRange = -dragAmount.y * (range.endInclusive - range.start) * sensitivity
                            onValueChange((value + deltaInValueRange).coerceIn(range.start, range.endInclusive))
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = (size.width / 2) - 8f

                // Underlay grey track circle
                drawArc(
                    color = Color(0xFF1E293B),
                    startAngle = minAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 8f, cap = StrokeCap.Round)
                )

                // Active glowing sweep
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.3f),
                            accentColor,
                            accentColor.copy(alpha = 0.3f)
                        ),
                        center = center
                    ),
                    startAngle = minAngle,
                    sweepAngle = fraction * sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 8f, cap = StrokeCap.Round)
                )

                // Knob Center Body
                drawCircle(
                    color = Color(0xFF0F172A),
                    radius = radius - 4f
                )

                // Inner bezel
                drawCircle(
                    color = Color(0xFF334155),
                    radius = radius - 8f,
                    style = Stroke(width = 2f)
                )

                // Pointer/Indicator Point
                val angleRad = (currentAngle * PI / 180f).toFloat()
                val indicatorRadius = radius - 14f
                val indicatorOffset = Offset(
                    x = center.x + indicatorRadius * cos(angleRad),
                    y = center.y + indicatorRadius * sin(angleRad)
                )

                drawCircle(
                    color = accentColor,
                    radius = 5f,
                    center = indicatorOffset
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8), // slate
            maxLines = 1
        )

        val formattedValue = if (kotlin.math.abs(value) < 1f && value != 0f) {
            String.format("%.2f", value)
        } else {
            String.format("%.1f", value)
        }

        Text(
            text = "$formattedValue $unit",
            fontSize = 11.sp,
            color = accentColor,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
