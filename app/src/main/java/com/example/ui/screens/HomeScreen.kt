package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FFTVisualizer
import com.example.ui.components.StudioKnob
import com.example.ui.components.VUMeter
import com.example.viewmodel.AudioProcessingViewModel

@Composable
fun HomeScreen(
    viewModel: AudioProcessingViewModel,
    modifier: Modifier = Modifier
) {
    val isEngineActive by viewModel.isEngineActive.collectAsState()
    val currentDevice by viewModel.currentDevice.collectAsState()
    val activePresetName by viewModel.currentPresetName.collectAsState()

    // VU meter states
    val peakL by viewModel.vuLeftPeak.collectAsState()
    val peakR by viewModel.vuRightPeak.collectAsState()
    val rmsL by viewModel.vuLeftRms.collectAsState()
    val rmsR by viewModel.vuRightRms.collectAsState()
    val clipL by viewModel.isClippingLeft.collectAsState()
    val clipR by viewModel.isClippingRight.collectAsState()

    // Faders
    val leftFader by viewModel.faderLeft.collectAsState()
    val rightFader by viewModel.faderRight.collectAsState()
    val stereoWidth by viewModel.stereoWidth.collectAsState()
    val stereoMode by viewModel.stereoMode.collectAsState()

    // Spectrum
    val fftData by viewModel.fftData.collectAsState()
    val waveData by viewModel.waveData.collectAsState()
    val visualizerMode by viewModel.visualizerMode.collectAsState()

    // Neon Glow Pulse Animation for active Power Button
    val infiniteTransition = rememberInfiniteTransition(label = "power_pulse")
    val alphaPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_pulse"
    )

    val powerColor by animateColorAsState(
        targetValue = if (isEngineActive) Color(0xFF00FFCC) else Color(0xFFEF4444), // neon cyan vs red
        label = "power_color"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP HEADER: Status Deck & Power Panel
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (isEngineActive) "AUDIO ENGINE: ACTIVE" else "AUDIO ENGINE: BYPASSED",
                        fontWeight = FontWeight.Bold,
                        color = powerColor,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Device Output: $currentDevice",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "DSP Profile: $activePresetName",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                }

                // Gigantic ON/OFF Power Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = if (isEngineActive) {
                                    listOf(Color(0xFF00FFCC).copy(alpha = 0.2f * alphaPulse), Color(0xFF0F172A))
                                } else {
                                    listOf(Color(0xFFEF4444).copy(alpha = 0.1f), Color(0xFF0F172A))
                                }
                            )
                        )
                        .border(
                            2.dp,
                            powerColor.copy(alpha = if (isEngineActive) alphaPulse else 0.5f),
                            CircleShape
                        )
                        .clickable { viewModel.togglePower() }
                        .testTag("power_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Power DSP Engine",
                        tint = powerColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // SPECTRUM ANALYZER block
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
        ) {
            FFTVisualizer(
                fftData = fftData,
                waveData = waveData,
                mode = visualizerMode,
                modifier = Modifier.fillMaxSize(),
                neonColor = powerColor
            )
        }

        // VU METERS & FADERS block (Side-by-side)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stereo VU Meter Block
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1.2f)
                    .height(230.dp)
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            ) {
                VUMeter(
                    peakL = peakL,
                    peakR = peakR,
                    rmsL = rmsL,
                    rmsR = rmsR,
                    clipL = clipL,
                    clipR = clipR,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Dual Output Fader & Stereo Width Controllers
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.8f)),
                modifier = Modifier
                    .weight(1.8f)
                    .height(230.dp)
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "OUTPUT CONTROLS",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    // Left & Right volume output sliders
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Output
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Slider(
                                value = leftFader,
                                onValueChange = { viewModel.setFaderLeft(it) },
                                valueRange = -60f..12f,
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .testTag("left_fader_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF00FFCC),
                                    activeTrackColor = Color(0xFF00FFCC)
                                )
                            )
                            Text("Left: ${String.format("%.1f", leftFader)} dB", fontSize = 10.sp, color = Color(0xFFF1F5F9))
                        }

                        // Right Output
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Slider(
                                value = rightFader,
                                onValueChange = { viewModel.setFaderRight(it) },
                                valueRange = -60f..12f,
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .testTag("right_fader_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFFFF3366),
                                    activeTrackColor = Color(0xFFFF3366)
                                )
                            )
                            Text("Right: ${String.format("%.1f", rightFader)} dB", fontSize = 10.sp, color = Color(0xFFF1F5F9))
                        }
                    }

                    Divider(color = Color(0xFF1E293B), modifier = Modifier.padding(vertical = 4.dp))

                    // Stereo Width Rotary dial
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StudioKnob(
                            value = stereoWidth,
                            onValueChange = { viewModel.setStereo(it, stereoMode) },
                            range = 0f..300f,
                            label = "STEREO WIDTH",
                            unit = "%",
                            accentColor = Color(0xFFFF9900)
                        )

                        Column(
                            modifier = Modifier.padding(start = 8.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            val modes = listOf("Mono", "Stereo", "Wide", "Ultra Wide")
                            for (m in modes) {
                                Button(
                                    onClick = { viewModel.setStereo(stereoWidth, m) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (stereoMode == m) Color(0xFFFF9900) else Color(0xFF1E293B)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .height(24.dp)
                                        .padding(bottom = 2.dp)
                                ) {
                                    Text(m, fontSize = 9.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
