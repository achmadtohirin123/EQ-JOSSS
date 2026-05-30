package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ParametricCurve(
    bands: List<Float>, // float list representing db values -15f to +15f
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFFA855F7) // Neon Purple/Violet
) {
    Box(
        modifier = modifier
            .background(Color(0xFF0F172A))
            .padding(10.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val centerY = h / 2f

            // Draw centerline (0dB level)
            drawLine(
                color = Color.Gray.copy(alpha = 0.5f),
                start = Offset(0f, centerY),
                end = Offset(w, centerY),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            )

            // Draw horizontal dB reference gridlines (+15dB, +7.5dB, -7.5dB, -15dB)
            val dbsY = listOf(
                centerY - (h / 2f) * 0.5f to "+7.5dB",
                centerY - (h / 2f) to "+15dB",
                centerY + (h / 2f) * 0.5f to "-7.5dB",
                centerY + (h / 2f) to "-15dB"
            )

            for (pair in dbsY) {
                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(0f, pair.first),
                    end = Offset(w, pair.first),
                    strokeWidth = 1f
                )
            }

            // Draw frequency spline plot
            val step = w / (bands.size - 1)
            val path = Path()

            val points = ArrayList<Offset>()
            for (i in bands.indices) {
                val db = bands[i].coerceIn(-15f, 15f)
                val fraction = db / 15f // -1f to 1f
                // Y goes down for positive offsets
                val x = i * step
                val y = centerY - (fraction * (h / 2f) * 0.9f)
                points.add(Offset(x, y))
            }

            if (points.isNotEmpty()) {
                path.moveTo(points[0].x, points[0].y)
                
                // Spline interpolation approximation (cubic curves)
                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val controlX1 = p0.x + step / 2f
                    val controlY1 = p0.y
                    val controlX2 = p0.x + step / 2f
                    val controlY2 = p1.y

                    path.cubicTo(
                        controlX1, controlY1,
                        controlX2, controlY2,
                        p1.x, p1.y
                    )
                }

                // Fill under curve
                val filledPath = Path().apply {
                    addPath(path)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }

                drawPath(
                    path = filledPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )

                // Draw main envelope curve line
                drawPath(
                    path = path,
                    color = accentColor,
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )

                // Print circle points at actual frequency locations
                for (p in points) {
                    drawCircle(
                        color = Color.White,
                        radius = 6f,
                        center = p
                    )
                    drawCircle(
                        color = accentColor,
                        radius = 4f,
                        center = p
                    )
                }
            }
        }
    }
}
