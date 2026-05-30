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
    private var viewModel: AudioProcessingViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        requestAudioPermissions()

        setContent {
            MyApplicationTheme {
                val vm: AudioProcessingViewModel = viewModel()
                viewModel = vm
                val selectedTab by vm.selectedTab.collectAsState()

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
                                    onClick = { vm.setSelectedTab(item.id) },
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
                            "HOME" -> HomeScreen(viewModel = vm)
                            "EQ" -> EqualizerScreen(viewModel = vm)
                            "FX RACK" -> FXRackScreen(viewModel = vm)
                            "PRESETS" -> PresetsScreen(viewModel = vm)
                            "AUTO EQ" -> AutoEqScreen(viewModel = vm)
                            "SETTINGS" -> SettingsScreen(viewModel = vm)
                        }
                    }
                }
            }
        }
    }

    private fun requestAudioPermissions() {
        val permissions = mutableListOf<String>()
        permissions.add(android.Manifest.permission.RECORD_AUDIO)
        permissions.add(android.Manifest.permission.READ_PHONE_STATE)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(android.Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(android.Manifest.permission.BLUETOOTH_SCAN)
        } else {
            @Suppress("DEPRECATION")
            permissions.add(android.Manifest.permission.BLUETOOTH)
        }
        
        try {
            requestPermissions(permissions.toTypedArray(), 101)
        } catch (e: Exception) {
            // ignore
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            viewModel?.tryStartRealVisualizer()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel?.tryStartRealVisualizer()
    }
}

data class NavigationItem(
    val id: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
)
