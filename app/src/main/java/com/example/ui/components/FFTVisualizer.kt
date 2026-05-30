package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun FFTVisualizer(
    fftData: FloatArray,  // 64 bands representing frequency levels
    waveData: FloatArray, // 128 elements representing waveform
    mode: String,         // "Bars", "Wave", "Line", "Filled Spectrum"
    modifier: Modifier = Modifier,
    neonColor: Color = Color(0xFF00FFCC) // Neon Cyan
) {
    Box(
        modifier = modifier
            .background(Color(0xFF050B14)) // Pitch dark space Blue
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Background Grid Lines
            val gridColor = Color(0xFF1E293B).copy(alpha = 0.4f)
            val rows = 5
            val cols = 8
            for (r in 1 until rows) {
                val y = r * h / rows
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
            }
            for (c in 1 until cols) {
                val x = c * w / cols
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1f
                )
            }

            // Draw visualizer depending on mode selection
            when (mode) {
                "Bars" -> {
                    // FFT size
                    val barWidth = w / fftData.size
                    for (i in fftData.indices) {
                        val valNormalized = fftData[i].coerceIn(0f, 1.2f)
                        val barHeight = valNormalized * h * 0.85f
                        val left = i * barWidth
                        val top = h - barHeight

                        // Glowing brush top-down
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFF3366), // Pink active highs
                                    neonColor,
                                    neonColor.copy(alpha = 0.2f)
                                )
                            ),
                            topLeft = Offset(left + 2f, top),
                            size = Size(barWidth - 4f, barHeight)
                        )
                    }
                }
                "Wave" -> {
                    // Oscilloscope drawing
                    val step = w / waveData.size
                    val points = ArrayList<Offset>()
                    for (i in waveData.indices) {
                        val x = i * step
                        val y = (h / 2f) + (waveData[i] * h * 1.5f)
                        points.add(Offset(x, y.coerceIn(0f, h)))
                    }

                    val path = Path()
                    if (points.isNotEmpty()) {
                        path.moveTo(points[0].x, points[0].y)
                        for (i in 1 until points.size) {
                            path.lineTo(points[i].x, points[i].y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = Color(0xFFFF9900), // Rich gold/orange analog trace
                        style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                    )
                }
                "Line" -> {
                    // FFT outline spline line
                    val step = w / fftData.size
                    val path = Path()
                    for (i in fftData.indices) {
                        val norm = fftData[i].coerceIn(0f, 1.2f)
                        val y = h - (norm * h * 0.85f)
                        val x = i * step
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }

                    drawPath(
                        path = path,
                        color = neonColor,
                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                    )
                }
                "Filled Spectrum" -> {
                    // FFT curve filled with glowing brush shading
                    val step = w / fftData.size
                    val path = Path()
                    path.moveTo(0f, h)
                    
                    for (i in fftData.indices) {
                        val norm = fftData[i].coerceIn(0f, 1.2f)
                        val y = h - (norm * h * 0.85f)
                        val x = i * step
                        path.lineTo(x, y)
                    }
                    path.lineTo(w, h)
                    path.close()

                    // Gradient Fill shading
                    drawPath(
                        path = path,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                neonColor.copy(alpha = 0.6f),
                                Color(0xFF0033FF).copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )

                    // Draw bright envelope contour on top
                    val topCurvePath = Path()
                    for (i in fftData.indices) {
                        val norm = fftData[i].coerceIn(0f, 1.2f)
                        val y = h - (norm * h * 0.85f)
                        val x = i * step
                        if (i == 0) topCurvePath.moveTo(x, y) else topCurvePath.lineTo(x, y)
                    }

                    // Stroke
                    drawPath(
                        path = topCurvePath,
                        color = neonColor,
                        style = Stroke(width = 3f)
                    )
                }
            }
        }
    }
}
