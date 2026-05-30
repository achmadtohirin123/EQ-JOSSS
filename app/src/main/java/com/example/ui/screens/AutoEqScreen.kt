package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Search
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

@Composable
fun AutoEqScreen(
    viewModel: AudioProcessingViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val profiles by viewModel.filteredAutoEqProfiles.collectAsState()
    val rawPresets by viewModel.userPresetsList.collectAsState()
    val currentPresetName by viewModel.currentPresetName.collectAsState()

    var showClipboardDialog by remember { mutableStateOf(false) }
    var importedText by remember { mutableStateOf("") }
    var importedHeadphoneName by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                "AUTOEQ HEADSET DATABASE",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                "Search and select headphones from our preloaded database to align output response.",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
        }

        // Import custom profile button card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Import Graphic Profile", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF00FFCC))
                    Text("Paste AutoEQ standard CSV or JSON values", fontSize = 10.sp, color = Color(0xFF64748B))
                }

                Button(
                    onClick = { showClipboardDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("import_profile_button")
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = "Import", tint = Color(0xFF00FFCC))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Import profile", color = Color(0xFF00FFCC), fontSize = 11.sp)
                }
            }
        }

        // Search Bar standard
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setQuery(it) },
            placeholder = { Text("Filter headphones e.g. Sony, Apple, DT990...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF64748B)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("autoeq_search_field"),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        // Profiles Database List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("autoeq_list"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(profiles) { item ->
                val isActive = currentPresetName == "AutoEQ (${item.name})"
                val activeProfileRepresentation = rawPresets.firstOrNull { it.name == currentPresetName }

                Card(
                    onClick = {
                        val newPreset = (activeProfileRepresentation ?: PresetEntity()).copy(
                            id = 0, // override custom profiles
                            name = "AutoEQ (${item.name})",
                            eqMode = "15 Band",
                            eqBandsCsv = item.eqBandsCsv,
                            isSystemPreset = false,
                            targetDeviceType = "Wired Headset"
                        )
                        viewModel.saveCustomPreset(newPreset)
                        viewModel.applyPreset(newPreset)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isActive) Color(0xFF00FFCC) else Color(0xFF1E293B),
                            RoundedCornerShape(10.dp)
                        ),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) Color(0xFF0F172A) else Color(0xFF1E293B).copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = "Headphones",
                                tint = if (isActive) Color(0xFF00FFCC) else Color(0xFF94A3B8)
                            )

                            Column {
                                Text(
                                    text = item.name,
                                    color = if (isActive) Color(0xFF00FFCC) else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Brand: ${item.brand}",
                                    color = Color(0xFF64748B),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (isActive) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text("Aligned", fontSize = 9.sp) },
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00FFCC))
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClipboardDialog) {
        AlertDialog(
            onDismissRequest = { showClipboardDialog = false },
            title = { Text("Import standard headset profile file") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = importedHeadphoneName,
                        onValueChange = { importedHeadphoneName = it },
                        label = { Text("Headphone model name") },
                        modifier = Modifier.fillMaxWidth().testTag("imported_headphone_name_field"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = importedText,
                        onValueChange = { importedText = it },
                        placeholder = { Text("Paste coefficients CSV (e.g. 1.2,0.5,-3.4,2.1,3.4...)") },
                        label = { Text("Tuning curves CSV coefficients") },
                        modifier = Modifier.fillMaxWidth().height(100.dp).testTag("imported_headphone_csv_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importedHeadphoneName.isNotEmpty() && importedText.isNotEmpty()) {
                            val targetPreset = PresetEntity(
                                name = "AutoEQ ($importedHeadphoneName)",
                                eqMode = "15 Band",
                                eqBandsCsv = importedText,
                                targetDeviceType = "Wired Headset",
                                isSystemPreset = false
                            )
                            viewModel.saveCustomPreset(targetPreset)
                            viewModel.applyPreset(targetPreset)
                            showClipboardDialog = false
                            importedHeadphoneName = ""
                            importedText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC))
                ) {
                    Text("Import Profile", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClipboardDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
