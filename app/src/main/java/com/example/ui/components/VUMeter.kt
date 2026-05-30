package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VUMeter(
    peakL: Float, // value in dB e.g. -60 to 12
    peakR: Float,
    rmsL: Float,
    rmsR: Float,
    clipL: Boolean,
    clipR: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color(0xFF0F172A)) // very dark slate
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Individual Meter for L (Left Channel)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text("L", fontSize = 11.sp, color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (clipL) Color.Red else Color(0xFF3F0000))
            )
            Spacer(modifier = Modifier.height(6.dp))
            VerticalMeterBar(peak = peakL, rms = rmsL, isPrimary = true)
        }

        // DB scale annotations
        Column(
            modifier = Modifier
                .width(28.dp)
                .fillMaxHeight()
                .padding(top = 18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val dbs = listOf("12", "6", "0", "-6", "-12", "-24", "-36", "-60")
            for (db in dbs) {
                Text(
                    text = db,
                    fontSize = 9.sp,
                    color = when (db) {
                        "12", "6" -> Color(0xFFEF4444) // Red
                        "0" -> Color(0xFFEAB308) // Yellow
                        else -> Color(0xFF64748B) // Slate
                    }
                )
            }
        }

        // Individual Meter for R (Right Channel)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text("R", fontSize = 11.sp, color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (clipR) Color.Red else Color(0xFF3F0000))
            )
            Spacer(modifier = Modifier.height(6.dp))
            VerticalMeterBar(peak = peakR, rms = rmsR, isPrimary = false)
        }
    }
}

@Composable
fun VerticalMeterBar(
    peak: Float, // -60 to +12 dB
    rms: Float,
    isPrimary: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .width(18.dp)
            .height(180.dp)
    ) {
        val w = size.width
        val h = size.height

        // Background groove
        drawRect(color = Color(0xFF1E293B))

        // Segmented LED steps definition (height steps)
        val numSegments = 30
        val dbMin = -60f
        val dbMax = 12f
        val dbRange = dbMax - dbMin

        // Fractions for Active peaks
        val peakFrac = ((peak - dbMin) / dbRange).coerceIn(0f, 1f)
        val rmsFrac = ((rms - dbMin) / dbRange).coerceIn(0f, 1f)

        for (i in 0 until numSegments) {
            val stepFrac = i.toFloat() / numSegments
            // Position starts from bottom (y is h to 0)
            val segmentY = h - (stepFrac * h) - (h / numSegments)
            val segmentHeight = (h / numSegments) - 2f

            // Map stepFrac back to dB to select appropriate colour segment
            val currentDbValue = dbMin + stepFrac * dbRange

            val baseColor = when {
                currentDbValue >= 0f -> Color(0xFFEF4444)      // Solid Red
                currentDbValue >= -15f -> Color(0xFFF59E0B)    // Solid Orange/Amber
                else -> Color(0xFF10B981)                      // Solid Neon Emerald Green
            }

            // Decide light states
            val isActive = stepFrac <= peakFrac
            val isRms = stepFrac <= rmsFrac

            val finalColor = when {
                isActive -> baseColor
                isRms -> baseColor.copy(alpha = 0.5f)
                else -> baseColor.copy(alpha = 0.08f) // extremely dim if off
            }

            drawRect(
                color = finalColor,
                topLeft = Offset(1f, segmentY),
                size = Size(w - 2f, segmentHeight)
            )
        }

        // Draw Peak hold line at top
        if (peak > dbMin) {
            val peakY = h - (peakFrac * h)
            drawLine(
                color = Color.White,
                start = Offset(0f, peakY),
                end = Offset(w, peakY),
                strokeWidth = 3f
            )
        }
    }
}
