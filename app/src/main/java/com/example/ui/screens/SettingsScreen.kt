package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.AudioProcessingViewModel

@Composable
fun SettingsScreen(
    viewModel: AudioProcessingViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Observe service configuration settings
    val sampleRate by viewModel.sampleRate.collectAsState()
    val bufferSize by viewModel.bufferSize.collectAsState()
    val playbackLatency by viewModel.playbackLatency.collectAsState()
    val visualizerFps by viewModel.visualizerFps.collectAsState()
    val fftSize by viewModel.fftSize.collectAsState()
    val visualizerMode by viewModel.visualizerMode.collectAsState()

    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                "AUDIO PROCESSING CONFIGURATION",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                "Fine-tune low-level Android driver sizes, latencies, and analyzer components.",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
        }

        // AUDIO ENGINE SETTINGS CARD
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "AUDIO DRIVER OPTIONS",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00FFCC),
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )

                // Sample Rate Selector (44.1, 48, 96, 192)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Sample Rate", fontSize = 11.sp, color = Color(0xFFE2E8F0))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(44100, 48000, 96000, 192000).forEach { rate ->
                            Button(
                                onClick = { viewModel.updateEngineSettings(rate, bufferSize, playbackLatency) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (sampleRate == rate) Color(0xFF00FFCC) else Color(0xFF334155)
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(26.dp)
                            ) {
                                Text(
                                    text = if (rate >= 1000) "${rate / 1000} kHz" else "$rate Hz",
                                    fontSize = 9.sp,
                                    color = if (sampleRate == rate) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }

                // Buffer Size Selector (128, 256, 512, 1024)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Buffer Size (Samples / Segment)", fontSize = 11.sp, color = Color(0xFFE2E8F0))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(128, 256, 512, 1024).forEach { size ->
                            Button(
                                onClick = { viewModel.updateEngineSettings(sampleRate, size, playbackLatency) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (bufferSize == size) Color(0xFF00FFCC) else Color(0xFF334155)
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(26.dp)
                            ) {
                                Text(
                                    text = "$size frames",
                                    fontSize = 9.sp,
                                    color = if (bufferSize == size) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }

                // Playback Driver Latency Selector (High, Normal, Low, Minimal)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Buffer Queue Latency Mode", fontSize = 11.sp, color = Color(0xFFE2E8F0))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(2 to "Ultra-Low", 5 to "Low-Latency", 10 to "Balanced", 20 to "Stable").forEach { pair ->
                            Button(
                                onClick = { viewModel.updateEngineSettings(sampleRate, bufferSize, pair.first) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (playbackLatency == pair.first) Color(0xFF00FFCC) else Color(0xFF334155)
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(26.dp)
                            ) {
                                Text(
                                    text = pair.second,
                                    fontSize = 9.sp,
                                    color = if (playbackLatency == pair.first) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // SPECTRAL ANALYZER CONFIGURATION
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "REALTIME SPECTRUM OPTIONS",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF3366),
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )

                // Visualizer Draw Modes
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("FFT Rendering Mode", fontSize = 11.sp, color = Color(0xFFE2E8F0))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Filled Spectrum", "Bars", "Wave", "Line").forEach { mode ->
                            Button(
                                onClick = { viewModel.updateVisualizerSettings(visualizerFps, fftSize, mode) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (visualizerMode == mode) Color(0xFFFF3366) else Color(0xFF334155)
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(26.dp)
                            ) {
                                Text(
                                    text = mode,
                                    fontSize = 9.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // FFT Window size option (2048, 4096, 8192)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("FFT Analyzer Sampling Window Size", fontSize = 11.sp, color = Color(0xFFE2E8F0))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(2048, 4096, 8192).forEach { size ->
                            Button(
                                onClick = { viewModel.updateVisualizerSettings(visualizerFps, size, visualizerMode) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (fftSize == size) Color(0xFFFF3366) else Color(0xFF334155)
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(26.dp)
                            ) {
                                Text(
                                    text = "$size points",
                                    fontSize = 9.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Render FPS (30 vs 60 FPS)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Oscilloscope Visualizer Refresh Rate (FPS)", fontSize = 11.sp, color = Color(0xFFE2E8F0))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(30, 60, 90, 120).forEach { fps ->
                            Button(
                                onClick = { viewModel.updateVisualizerSettings(fps, fftSize, visualizerMode) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (visualizerFps == fps) Color(0xFFFF3366) else Color(0xFF334155)
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(26.dp)
                            ) {
                                Text(
                                    text = "$fps FPS",
                                    fontSize = 9.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // BACKUP & RESTORE DATA
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "DATABASE BACKUP & RESTORE",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFA855F7), // violet
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Export JSON file
                    Button(
                        onClick = {
                            val json = viewModel.exportPresetsToJson(context)
                            if (json != null) {
                                Toast.makeText(context, "Presets database exported to filesDir/bro_eq_presets_backup.json", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Export error", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("export_backup_button")
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = "Export backup", tint = Color(0xFFA855F7))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export backup JSON", fontSize = 11.sp, color = Color.White)
                    }

                    // Restore JSON file
                    Button(
                        onClick = { showRestoreDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("restore_backup_button")
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = "Restore backup", tint = Color(0xFFA855F7))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restore JSON", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Restore database presets backup") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Paste your exported JSON parameters here to restore definitions:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    OutlinedTextField(
                        value = restoreJsonText,
                        onValueChange = { restoreJsonText = it },
                        modifier = Modifier.fillMaxWidth().height(150.dp).testTag("restore_json_field"),
                        placeholder = { Text("Paste JSON here...") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (restoreJsonText.isNotEmpty()) {
                            val success = viewModel.importPresetsFromJson(restoreJsonText)
                            if (success) {
                                Toast.makeText(context, "Presets database successfully restored!", Toast.LENGTH_SHORT).show()
                                showRestoreDialog = false
                                restoreJsonText = ""
                            } else {
                                Toast.makeText(context, "Invalid JSON structure. Please check structure and try again.", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7))
                ) {
                    Text("Process Restore", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
