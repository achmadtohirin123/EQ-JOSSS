package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Usb
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
import com.example.viewmodel.AudioProcessingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetsScreen(
    viewModel: AudioProcessingViewModel,
    modifier: Modifier = Modifier
) {
    val presets by viewModel.userPresetsList.collectAsState()
    val activePresetName by viewModel.currentPresetName.collectAsState()

    var showSaveDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }
    var targetDeviceType by remember { mutableStateOf("All") } // "All", "Internal Speaker", "Wired Headset", "Bluetooth", "USB DAC"

    val currentActivePreset = presets.firstOrNull { it.name == activePresetName }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Saving Trigger FAB Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PRESET MANAGER",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = "Active Profile: $activePresetName",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }

            // Save Active config as Profile Button
            Button(
                onClick = { showSaveDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("save_preset_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFF0F172A))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save Active", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
            }
        }

        // List Profile Presets
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("presets_list"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(presets) { p ->
                val isSelected = p.name == activePresetName
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isSelected) Color(0xFF00FFCC) else Color(0xFF1E293B),
                            RoundedCornerShape(12.dp)
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF0F172A) else Color(0xFF1E293B).copy(alpha = 0.5f)
                    ),
                    onClick = { viewModel.applyPreset(p) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Device icon type indicator
                            val icon = when (p.targetDeviceType) {
                                "Internal Speaker" -> Icons.Default.Speaker
                                "Wired Headset" -> Icons.Default.Headphones
                                "Bluetooth" -> Icons.Default.Headphones
                                "USB DAC" -> Icons.Default.Usb
                                else -> Icons.Default.DeviceUnknown
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = p.targetDeviceType,
                                tint = if (isSelected) Color(0xFF00FFCC) else Color(0xFF64748B)
                            )

                            Column {
                                Text(
                                    text = p.name,
                                    color = if (isSelected) Color(0xFF00FFCC) else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Device Alignment: ${p.targetDeviceType} | EQ: ${p.eqMode}",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Actions (Delete button) only show for custom presets (not default ones)
                        if (!p.isSystemPreset) {
                            IconButton(
                                onClick = { viewModel.deletePreset(p) },
                                modifier = Modifier.testTag("delete_preset_${p.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                            }
                        } else {
                            // Chip saying default
                            SuggestionChip(
                                onClick = {},
                                label = { Text("Standard", fontSize = 10.sp) },
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal dialog to save preset
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save contemporary DSP setting as Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newPresetName,
                        onValueChange = { newPresetName = it },
                        label = { Text("Profile Name") },
                        modifier = Modifier.fillMaxWidth().testTag("preset_name_input"),
                        singleLine = true
                    )

                    Column {
                        Text("Target Device Class Alignment:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("All", "Internal Speaker", "Wired Headset", "Bluetooth", "USB DAC").forEach { dType ->
                                Button(
                                    onClick = { targetDeviceType = dType },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (targetDeviceType == dType) Color(0xFF00FFCC) else Color(0xFF334155)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(
                                        text = if (dType == "Internal Speaker") "Speaker" else if (dType == "Wired Headset") "Wired" else dType,
                                        fontSize = 9.sp,
                                        color = if (targetDeviceType == dType) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPresetName.isNotEmpty()) {
                            // Create copy of current configuration with the name inserted
                            val freshPreset = (currentActivePreset ?: PresetEntity()).copy(
                                id = 0, // safe auto incremental
                                name = newPresetName,
                                isSystemPreset = false,
                                targetDeviceType = targetDeviceType
                            )
                            viewModel.saveCustomPreset(freshPreset)
                            viewModel.applyPreset(freshPreset)
                            showSaveDialog = false
                            newPresetName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC))
                ) {
                    Text("Save Config", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
