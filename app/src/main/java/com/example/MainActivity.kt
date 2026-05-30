package com.example

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AudioProcessingViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Request post notifications permission on Android 13+ inside main onCreate dynamically
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            } catch (e: Exception) {
                // permission ignored
            }
        }

        setContent {
            MyApplicationTheme {
                val viewModel: AudioProcessingViewModel = viewModel()
                val selectedTab by viewModel.selectedTab.collectAsState()

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_scaffold"),
                    bottomBar = {
                        val navigationItems = listOf(
                            NavigationItem("HOME", Icons.Default.Home, "home_tab"),
                            NavigationItem("EQ", Icons.Default.Equalizer, "eq_tab"),
                            NavigationItem("FX RACK", Icons.Default.AutoAwesome, "fx_tab"),
                            NavigationItem("PRESETS", Icons.Default.ListAlt, "presets_tab"),
                            NavigationItem("AUTO EQ", Icons.Default.Headphones, "auto_eq_tab"),
                            NavigationItem("SETTINGS", Icons.Default.Settings, "settings_tab")
                        )

                        NavigationBar(
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .testTag("bottom_nav_bar"),
                            containerColor = Color(0xFF0F172A),
                            tonalElevation = 8.dp
                        ) {
                            navigationItems.forEach { item ->
                                val isSelected = selectedTab == item.id
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { viewModel.setSelectedTab(item.id) },
                                    icon = {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.id,
                                            tint = if (isSelected) Color(0xFF00FFCC) else Color(0xFF64748B)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = item.id,
                                            fontSize = 9.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color(0xFF00FFCC) else Color(0xFF64748B)
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = Color(0xFF1E293B)
                                    ),
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    },
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF020617)) // Slate-950 Pitch Dark
                            .padding(innerPadding)
                    ) {
                        when (selectedTab) {
                            "HOME" -> HomeScreen(viewModel = viewModel)
                            "EQ" -> EqualizerScreen(viewModel = viewModel)
                            "FX RACK" -> FXRackScreen(viewModel = viewModel)
                            "PRESETS" -> PresetsScreen(viewModel = viewModel)
                            "AUTO EQ" -> AutoEqScreen(viewModel = viewModel)
                            "SETTINGS" -> SettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

data class NavigationItem(
    val id: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
)
