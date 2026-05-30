package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PresetEntity
import com.example.ui.components.StudioKnob
import com.example.viewmodel.AudioProcessingViewModel

@Composable
fun FXRackScreen(
    viewModel: AudioProcessingViewModel,
    modifier: Modifier = Modifier
) {
    var fxActiveSubTab by remember { mutableStateOf("DYNAMIC") } // "DYNAMIC", "SPATIAL", "ENHANCE"
    val activePreset by viewModel.activePresetState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // FX Category tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("DYNAMIC", "SPATIAL & REVERB", "ENHANCE & VOCAL").forEach { caption ->
                val keyword = when {
                    caption.contains("DYNAMIC") -> "DYNAMIC"
                    caption.contains("SPATIAL") -> "SPATIAL"
                    else -> "ENHANCE"
                }

                Button(
                    onClick = { fxActiveSubTab = keyword },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (fxActiveSubTab == keyword) Color(0xFF00FFCC) else Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("fx_tab_${keyword.lowercase()}"),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = caption,
                        color = if (fxActiveSubTab == keyword) Color(0xFF0F172A) else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // SCROLLABLE CONTAINER
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (fxActiveSubTab) {
                "DYNAMIC" -> {
                    // COMPRESSOR CARD BLOCK
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "MASTER COMPRESSOR",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00FFCC),
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp
                                )

                                Switch(
                                    checked = activePreset.compressorEnabled,
                                    onCheckedChange = { isChecked ->
                                        val updated = activePreset.copy(compressorEnabled = isChecked)
                                        viewModel.saveCustomPreset(updated)
                                        viewModel.applyPreset(updated)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF00FFCC)
                                    ),
                                    modifier = Modifier.testTag("comp_switch")
                                )
                            }

                            // Horizontal Reduction Meter
                            if (activePreset.compressorEnabled) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("Realtime Reduction: -2.4 dB", fontSize = 10.sp, color = Color(0xFF64748B))
                                    LinearProgressIndicator(
                                        progress = 0.85f,
                                        color = Color(0xFFEF4444), // red reduction indicator
                                        trackColor = Color(0xFF334155),
                                        modifier = Modifier.fillMaxWidth().height(6.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StudioKnob(
                                    value = activePreset.compThreshold,
                                    onValueChange = {
                                        val updated = activePreset.copy(compThreshold = it)
                                        viewModel.saveCustomPreset(updated)
                                        viewModel.applyPreset(updated)
                                    },
                                    range = -60f..0f,
                                    label = "Threshold",
                                    unit = "dB",
                                    accentColor = Color(0xFF00FFCC),
                                    modifier = Modifier.weight(1f)
                                )

                                StudioKnob(
                                    value = activePreset.compRatio,
                                    onValueChange = {
                                        val updated = activePreset.copy(compRatio = it)
                                        viewModel.saveCustomPreset(updated)
                                        viewModel.applyPreset(updated)
                                    },
                                    range = 1f..20f,
                                    label = "Ratio",
                                    unit = ":1",
                                    accentColor = Color(0xFF00FFCC),
                                    modifier = Modifier.weight(1f)
                                )

                                StudioKnob(
                                    value = activePreset.compAttack,
                                    onValueChange = {
                                        val updated = activePreset.copy(compAttack = it)
                                        viewModel.saveCustomPreset(updated)
                                        viewModel.applyPreset(updated)
                                    },
                                    range = 0.1f..100f,
                                    label = "Attack",
                                    unit = "ms",
                                    accentColor = Color(0xFF00FFCC),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StudioKnob(
                                    value = activePreset.compRelease,
                                    onValueChange = {
                                        val updated = activePreset.copy(compRelease = it)
                                        viewModel.saveCustomPreset(updated)
                                        viewModel.applyPreset(updated)
                                    },
                                    range = 10f..1000f,
                                    label = "Release",
                                    unit = "ms",
                                    accentColor = Color(0xFF00FFCC),
                                    modifier = Modifier.weight(1f)
                                )

                                StudioKnob(
                                    value = activePreset.compKnee,
                                    onValueChange = {
                                        val updated = activePreset.copy(compKnee = it)
                                        viewModel.saveCustomPreset(updated)
                                        viewModel.applyPreset(updated)
                                    },
                                    range = 0f..20f,
                                    label = "Knee width",
                                    unit = "dB",
                                    accentColor = Color(0xFF00FFCC),
                                    modifier = Modifier.weight(1f)
                                )

                                StudioKnob(
                                    value = activePreset.compMakeupGain,
                                    onValueChange = {
                                        val updated = activePreset.copy(compMakeupGain = it)
                                        viewModel.saveCustomPreset(updated)
                                        viewModel.applyPreset(updated)
                                    },
                                    range = 0f..24f,
                                    label = "Makeup",
                                    unit = "dB",
                                    accentColor = Color(0xFF00FFCC),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // LIMITER & MAXIMIZER CARD BLOCK
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "LIMITER & MAXIMIZER",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF3366),
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StudioKnob(
                                    value = activePreset.limCeiling,
                                    onValueChange = {
                                        val updated = activePreset.copy(limCeiling = it)
                                        viewModel.saveCustomPreset(updated)
                                        viewModel.applyPreset(updated)
                                    },
                                    range = -10f..0f,
                                    label = "Ceiling",
                                    unit = "dB",
                                    accentColor = Color(0xFFFF3366),
                                    modifier = Modifier.weight(1f)
                                )

                                StudioKnob(
                                    value = activePreset.limRelease,
                                    onValueChange = {
                                        val updated = activePreset.copy(limRelease = it)
                                        viewModel.saveCustomPreset(updated)
                                        viewModel.applyPreset(updated)
                                    },
                                    range = 5f..500f,
                                    label = "Release",
                                    unit = "ms",
                                    accentColor = Color(0xFFFF3366),
                                    modifier = Modifier.weight(1f)
                                )

                                StudioKnob(
                                    value = activePreset.limLookAhead,
                                    onValueChange = {
                                        val updated = activePreset.copy(limLookAhead = it)
                                        viewModel.saveCustomPreset(updated)
                                        viewModel.applyPreset(updated)
                                    },
                                    range = 0.1f..10f,
                                    label = "Look ahead",
                                    unit = "ms",
                                    accentColor = Color(0xFFFF3366),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Divider(color = Color(0xFF334155))

                            // Maximizer Mode Switcher (Safe, Strong, Extreme)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Loudness Maximizer Mode", fontSize = 11.sp, color = Color(0xFFE2E8F0))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("Safe", "Strong", "Extreme").forEach { mode ->
                                        Button(
                                            onClick = {
                                                val updated = activePreset.copy(loudnessMaximizerMode = mode)
                                                viewModel.saveCustomPreset(updated)
                                                viewModel.applyPreset(updated)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (activePreset.loudnessMaximizerMode == mode) Color(0xFFFF3366) else Color(0xFF334155)
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f).height(32.dp)
                                        ) {
                                            Text(mode, fontSize = 10.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "SPATIAL" -> {
                    // 3D SPATIAL & REVERB CARD
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "3D AUDIO & SURROUND ENGINE",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3B82F6), // Blue
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )

                            // 3D preset simulation select
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Acoustic Space Mode", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    listOf("Studio", "Cinema", "Live", "Hall", "Arena").forEach { sMode ->
                                        Button(
                                            onClick = {
                                                val updated = activePreset.copy(audio3dMode = sMode)
                                                viewModel.saveCustomPreset(updated)
                                                viewModel.applyPreset(updated)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (activePreset.audio3dMode == sMode) Color(0xFF3B82F6) else Color(0xFF223555)
                                            ),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(0.dp),
                                            modifier = Modifier.weight(1f).height(24.dp)
                                        ) {
                                            Text(sMode, fontSize = 9.sp, color = Color.White)
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StudioKnob(
                                    value = activePreset.audio3dWidth,
                                    onValueChange = {
                                        val updated = activePreset.copy(audio3dWidth = it)
                                        viewModel.saveCustomPreset(updated)
                                        viewModel.applyPreset(updated)
                                    },
                                    range = 0f..300f,
                                    label = "Width",
                                    unit = "%",
                                    accentColor = Color(0xFF3B82F6),
                                    modifier = Modifier.weight(1f)
                                )

                                StudioKnob(
                                    value = activePreset.audio3dDepth,
                                    onValueChange = {
                                        val updated = activePreset.copy(audio3dDepth = it)
                                        viewModel.saveCustomPreset(updated)
                                        viewModel.applyPreset(updated)
                                    },
                                    range = 0f..100f,
                                    label = "Depth",
                                    unit = "%",
                                    accentColor = Color(0xFF3B82F6),
                                    modifier = Modifier.weight(1f)
                                )

                                StudioKnob(
                                    value = activePreset.audio3dDistance,
                                    onValueChange = {
                                        val updated = activePreset.copy(audio3dDistance = it)
                                        viewModel.saveCustomPreset(updated)
                                        viewModel.applyPreset(updated)
                                    },
                                    range = 0f..100f,
                                    label = "Distance",
                                    unit = "%",
                                    accentColor = Color(0xFF3B82F6),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Divider(color = Color(0xFF334155))

                            // Surround Simulation switcher (5.1 vs 7.1)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Audio Channel Surround Simulation", fontSize = 11.sp, color = Color(0xFFE2E8F0))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("Stereo", "5.1", "7.1").forEach { ch ->
                                        Button(
                                            onClick = {
                                                val updated = activePreset.copy(surroundMode = ch)
                                                viewModel.saveCustomPreset(updated)
                                                viewModel.applyPreset(updated)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (activePreset.surroundMode == ch) Color(0xFF3B82F6) else Color(0xFF334155)
                                            ),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp),
                                            modifier = Modifier.height(26.dp)
                                        ) {
                                            Text(ch, fontSize = 9.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // REVERB & DELAY CARD BLOCK
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "REVERBERATION ENGINE",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981),
                                    fontSize = 12.sp
                                )

                                Switch(
                                    checked = activePreset.reverbEnabled,
                                    onCheckedChange = { isChecked ->
                                        val updated = activePreset.copy(reverbEnabled = isChecked)
                                        viewModel.saveCustomPreset(updated)
                                        viewModel.applyPreset(updated)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF10B981)
                                    ),
                                    modifier = Modifier.testTag("reverb_switch")
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StudioKnob(
                                    value = activePreset.revDecay,
                                    onValueChange = {
                                        val updated = activePreset.copy(revDecay = it)
                                        viewModel.saveCustomPreset(updated)
                                        viewModel.applyPreset(updated)
                                    },
                                    range = 0.1f..10f,
                                    label = "Decay time",
                                    unit = "s",
                                    accentColor = Color(0xFF10B981),
                                    modifier = Modifier.weight(1f)
                                )

                                StudioKnob(
                                    value = activePreset.revWet,
                                    onValueChange = {
                                        val updated = activePreset.copy(revWet = it)
                                        viewModel.saveCustomPreset(updated)
                                        viewModel.applyPreset(updated)
                                    },
                                    range = 0f..100f,
                                    label = "Wet mix",
                                    unit = "%",
                                    accentColor = Color(0xFF10B981),
                                    modifier = Modifier.weight(1f)
                                )

                                StudioKnob(
                                    value = activePreset.revDry,
                                    onValueChange = {
                                        val updated = activePreset.copy(revDry = it)
                                        viewModel.saveCustomPreset(updated)
                                        viewModel.applyPreset(updated)
                                    },
                                    range = 0f..100f,
                                    label = "Dry mix",
                                    unit = "%",
                                    accentColor = Color(0xFF10B981),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                "ENHANCE" -> {
                    // BASS & TREBLE ENHANCERS
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "ANALOG SOUND GENERATORS",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEAB308),
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )

                            // BASS ENHANCEMENT
                            Text("BASS ENHANCEMENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StudioKnob(
                                    value = activePreset.bassBoost,
                                    onValueChange = {
                                        val updated = activePreset.copy(bassBoost = it)
                                        viewModel.saveCustomPreset(updated)
                                        viewModel.applyPreset(updated)
                                    },
                                    range = 0f..100f,
                                    label = "Bass Boost",
                                    unit = "%",
                                    accentColor = Color(0xFFEAB308),
                                    modifier = Modifier.weight(1f)
                                )

                                StudioKnob(
                                    value = activePreset.deepBass,
                                    onValueChange = {
                                        val updated = activePreset.copy(deepBass = it)
                                        viewModel.saveCustomPreset(updated)
                                        viewModel.applyPreset(updated)
                                    },
                                    range = 0f..100f,
                                    label = "Deep Bass",
                                    unit = "%",
                                    accentColor = Color(0xFFEAB308),
                                    modifier = Modifier.weight(1f)
                                )

                                StudioKnob(
                                    value = activePreset.subHarmonic,
                                    onValueChange = {
                                        val updated = activePreset.copy(subHarmonic = it)
                                        viewModel.saveCustomPreset(updated)
                                        viewModel.applyPreset(updated)
                                    },
                                    range = 0f..100f,
                                    label = "Sub Harmonic",
                                    unit = "%",
                                    accentColor = Color(0xFFEAB308),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Divider(color = Color(0xFF334155))

                            // TREBLE ENHANCEMENT
                            Text("TREBLE ENHANCEMENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StudioKnob(
                                    value = activePreset.airGain,
                                    onValueChange = {
                                        val updated = activePreset.copy(airGain = it)
                                        viewModel.saveCustomPreset(updated)
                                        viewModel.applyPreset(updated)
                                    },
                                    range = 0f..100f,
                                    label = "Air Flow",
                                    unit = "%",
                                    accentColor = Color(0xFF00FFCC),
                                    modifier = Modifier.weight(1f)
                                )

                                StudioKnob(
                                    value = activePreset.presenceGain,
                                    onValueChange = {
                                        val updated = activePreset.copy(presenceGain = it)
                                        viewModel.saveCustomPreset(updated)
                                        viewModel.applyPreset(updated)
                                    },
                                    range = 0f..100f,
                                    label = "Presence",
                                    unit = "%",
                                    accentColor = Color(0xFF00FFCC),
                                    modifier = Modifier.weight(1f)
                                )

                                StudioKnob(
                                    value = activePreset.sparkleGain,
                                    onValueChange = {
                                        val updated = activePreset.copy(sparkleGain = it)
                                        viewModel.saveCustomPreset(updated)
                                        viewModel.applyPreset(updated)
                                    },
                                    range = 0f..100f,
                                    label = "Sparkle FX",
                                    unit = "%",
                                    accentColor = Color(0xFF00FFCC),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // VOCAL PROCESSOR BLOCK CARD
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "VOCAL HARMONICS PROCESSOR",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA855F7), // Purple
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StudioKnob(
                                    value = activePreset.vocalEnhancer,
                                    onValueChange = {
                                        val updated = activePreset.copy(vocalEnhancer = it)
                                        viewModel.saveCustomPreset(updated)
                                        viewModel.applyPreset(updated)
                                    },
                                    range = 0f..100f,
                                    label = "Enhancer",
                                    unit = "%",
                                    accentColor = Color(0xFFA855F7),
                                    modifier = Modifier.weight(1f)
                                )

                                StudioKnob(
                                    value = activePreset.deEsser,
                                    onValueChange = {
                                        val updated = activePreset.copy(deEsser = it)
                                        viewModel.saveCustomPreset(updated)
                                        viewModel.applyPreset(updated)
                                    },
                                    range = 0f..100f,
                                    label = "De-Esser",
                                    unit = "%",
                                    accentColor = Color(0xFFA855F7),
                                    modifier = Modifier.weight(1f)
                                )

                                StudioKnob(
                                    value = activePreset.harmonicExciter,
                                    onValueChange = {
                                        val updated = activePreset.copy(harmonicExciter = it)
                                        viewModel.saveCustomPreset(updated)
                                        viewModel.applyPreset(updated)
                                    },
                                    range = 0f..100f,
                                    label = "Exciter",
                                    unit = "%",
                                    accentColor = Color(0xFFA855F7),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
