package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.components.ParametricCurve
import com.example.ui.components.StudioKnob
import com.example.viewmodel.AudioProcessingViewModel

@Composable
fun EqualizerScreen(
    viewModel: AudioProcessingViewModel,
    modifier: Modifier = Modifier
) {
    var primaryEqTab by remember { mutableStateOf("GRAPHIC") } // "GRAPHIC" or "PARAMETRIC"

    // Retrieve active preset settings from cache
    val activePresetRaw by viewModel.activePresetState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP TAB TRACK: Graphic EQ vs Parametric Pro
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("GRAPHIC", "PARAMETRIC EQ (FAB PRO)").forEach { tabName ->
                val keyword = if (tabName.startsWith("GRAPHIC")) "GRAPHIC" else "PARAMETRIC"
                Button(
                    onClick = { primaryEqTab = keyword },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (primaryEqTab == keyword) Color(0xFF00FFCC) else Color.Transparent,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("eq_tab_${keyword.lowercase()}"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = tabName,
                        color = if (primaryEqTab == keyword) Color(0xFF0F172A) else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (primaryEqTab == "GRAPHIC") {
            // GRAPHIC EQUALIZER DECK
            var graphicBandMode by remember { mutableStateOf("15 Band") } // "7 Band", "15 Band", "31 Band"

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.8f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Equalizer subtabs: 7, 15, 31 band selector
                    Row(
                        modifier = Modifier
                            .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("7 Band", "15 Band", "31 Band").forEach { mode ->
                            Button(
                                onClick = { graphicBandMode = mode },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (graphicBandMode == mode) Color(0xFFFF3366) else Color.Transparent
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(mode, fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }

                    // Generate appropriate bands and frequencies based on configuration
                    val frequencies = when (graphicBandMode) {
                        "7 Band" -> listOf("62 Hz", "125 Hz", "500 Hz", "1 kHz", "2 kHz", "8 kHz", "16 kHz")
                        "15 Band" -> listOf("31", "62", "125", "250", "350", "500", "750", "1K", "2K", "4K", "6K", "8K", "12K", "16K", "20K")
                        else -> listOf(
                            "20", "25", "31.5", "40", "50", "63", "80", "100", "125", "160", "200", "250", "315", "400", "500", "630", "800",
                            "1K", "1.25K", "1.6K", "2K", "2.5K", "3.15K", "4K", "5K", "6.3K", "8K", "10K", "12.5K", "16K", "20K"
                        )
                    }

                    // Parse bands values
                    val bandsList = viewModel.bandsToList(activePresetRaw.eqBandsCsv)
                    val displayBandsList = remember(bandsList, graphicBandMode) {
                        // Truncate or expand bands dynamically to match modes
                        val targetLen = frequencies.size
                        if (bandsList.size >= targetLen) bandsList.take(targetLen)
                        else bandsList + List(targetLen - bandsList.size) { 0f }
                    }

                    // Vertical sliders inside horizontal scroll state
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        displayBandsList.forEachIndexed { idx, gain ->
                            val currentFreq = frequencies[idx]
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .width(44.dp)
                                    .fillMaxHeight()
                                    .background(Color(0xFF1E293B).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = String.format("%+.1f", gain),
                                    fontSize = 11.sp,
                                    color = if (gain != 0f) Color(0xFFFF3366) else Color(0xFF64748B),
                                    fontWeight = FontWeight.Bold
                                )

                                // Vertical Slider
                                Slider(
                                    value = gain,
                                    onValueChange = { newGain ->
                                        val mutableBands = displayBandsList.toMutableList()
                                        mutableBands[idx] = newGain
                                        
                                        // Save back in-memory instantly
                                        val updatedPreset = activePresetRaw.copy(
                                            eqMode = graphicBandMode,
                                            eqBandsCsv = viewModel.listToBands(mutableBands)
                                        )
                                        viewModel.updateActivePresetState(updatedPreset)
                                    },
                                    valueRange = -15f..15f,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("eq_band_slider_$idx")
                                )

                                Text(
                                    text = currentFreq,
                                    fontSize = 9.sp,
                                    color = Color(0xFFE2E8F0),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // PARAMETRIC EQ SCREEN
            // Retrive parametric bands
            val parametricBands = remember(activePresetRaw.parametricBandsCsv) {
                // Split bands e.g. "freq1,q1,gain1;freq2,q2,gain2..."
                activePresetRaw.parametricBandsCsv.split(";").mapNotNull { pBand ->
                    val parts = pBand.split(",")
                    if (parts.size >= 3) {
                        ParametricBandItem(
                            freq = parts[0].toFloatOrNull() ?: 1000f,
                            q = parts[1].toFloatOrNull() ?: 1f,
                            gain = parts[2].toFloatOrNull() ?: 0f
                        )
                    } else null
                }.toMutableStateList()
            }

            var selectedBandIndex by remember { mutableStateOf(5) } // Select band 5 (1000 Hz) as default filter

            // Curve Visualizer
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            ) {
                ParametricCurve(
                    bands = parametricBands.map { it.gain },
                    modifier = Modifier.fillMaxSize(),
                    accentColor = Color(0xFFA855F7)
                )
            }

            // Band Selectors deck
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                parametricBands.forEachIndexed { index, pBand ->
                    Button(
                        onClick = { selectedBandIndex = index },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedBandIndex == index) Color(0xFFA855F7) else Color(0xFF1E293B)
                        ),
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("parametric_band_select_$index"),
                        contentPadding = PaddingValues(0.dp),
                        shape = CircleShape
                    ) {
                        Text("${index + 1}", fontSize = 11.sp, color = Color.White)
                    }
                }
            }

            // Controllers Panel for selected Parametric Filter (Surgical Knobs!)
            if (selectedBandIndex in parametricBands.indices) {
                val currentFilter = parametricBands[selectedBandIndex]
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
                            text = "BAND ${selectedBandIndex + 1} PARAMETERS - ${currentFilter.freq.toInt()} Hz",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA855F7),
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Gain Knob
                            StudioKnob(
                                value = currentFilter.gain,
                                onValueChange = { newGain ->
                                    val mutated = currentFilter.copy(gain = newGain)
                                    parametricBands[selectedBandIndex] = mutated
                                    saveAndApplyParametric(viewModel, activePresetRaw, parametricBands)
                                },
                                range = -15f..15f,
                                label = "Gain",
                                unit = "dB",
                                accentColor = Color(0xFFA855F7)
                            )

                            // Q-Factor Width Knob
                            StudioKnob(
                                value = currentFilter.q,
                                onValueChange = { newQ ->
                                    val mutated = currentFilter.copy(q = newQ)
                                    parametricBands[selectedBandIndex] = mutated
                                    saveAndApplyParametric(viewModel, activePresetRaw, parametricBands)
                                },
                                range = 0.1f..10f,
                                label = "Q-Width",
                                accentColor = Color(0xFF10B981)
                            )

                            // Frequency Fine-Tune Knob
                            StudioKnob(
                                value = currentFilter.freq,
                                onValueChange = { newFreq ->
                                    val mutated = currentFilter.copy(freq = newFreq)
                                    parametricBands[selectedBandIndex] = mutated
                                    saveAndApplyParametric(viewModel, activePresetRaw, parametricBands)
                                },
                                range = 20f..20000f,
                                label = "Fine Freq",
                                unit = "Hz",
                                accentColor = Color(0xFFEAB308)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun saveAndApplyParametric(
    viewModel: AudioProcessingViewModel,
    preset: PresetEntity,
    bands: List<ParametricBandItem>
) {
    val csv = bands.joinToString(";") { "${it.freq},${it.q},${it.gain}" }
    val updated = preset.copy(
        eqMode = "Parametric",
        parametricBandsCsv = csv
    )
    viewModel.updateActivePresetState(updated)
}

data class ParametricBandItem(
    val freq: Float,
    val q: Float,
    val gain: Float
)
