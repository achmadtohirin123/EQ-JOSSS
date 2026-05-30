package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "presets")
data class PresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = "",
    
    // Equalizer bands (comma-separated float list: e.g. "0.0,0.0,0.0...")
    val eqMode: String = "15 Band", // "7 Band", "15 Band", "31 Band", "Parametric"
    val eqBandsCsv: String = "0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0", // default 15 bands
    
    // Parametric 10 bands - Frequency, Q, Gain (flat array csv e.g. "freq1,q1,gain1,freq2,q2,gain2...")
    val parametricBandsCsv: String = "31.0,1.0,0.0;62.0,1.0,0.0;125.0,1.0,0.0;250.0,1.0,0.0;500.0,1.0,0.0;1000.0,1.0,0.0;2000.0,1.0,0.0;4000.0,1.0,0.0;8000.0,1.0,0.0;16000.0,1.0,0.0",
    
    // Compressor settings
    val compressorEnabled: Boolean = false,
    val compThreshold: Float = -20f, // dB
    val compRatio: Float = 4f,       // x:1
    val compAttack: Float = 20f,     // ms
    val compRelease: Float = 250f,   // ms
    val compKnee: Float = 5f,        // dB
    val compMakeupGain: Float = 0f,  // dB

    // Limiter settings
    val limiterEnabled: Boolean = false,
    val limCeiling: Float = -0.1f,    // dB
    val limRelease: Float = 100f,    // ms
    val limLookAhead: Float = 2f,     // ms

    // Bass & Treble Enhancement
    val bassBoost: Float = 0f,       // 0% - 100%
    val deepBass: Float = 0f,        // 0% - 100%
    val subHarmonic: Float = 0f,     // 0% - 100%
    val airGain: Float = 0f,         // 0% - 100%
    val presenceGain: Float = 0f,    // 0% - 100%
    val sparkleGain: Float = 0f,     // 0% - 100%

    // Reverb Engine
    val reverbEnabled: Boolean = false,
    val revPreset: String = "Room",  // "Room", "Hall", "Plate", "Church", "Arena"
    val revDecay: Float = 1.5f,      // seconds
    val revSize: Float = 50f,        // 0% - 100%
    val revWet: Float = 30f,         // 0% - 100%
    val revDry: Float = 100f,        // 0% - 100%

    // Delay Engine
    val delayEnabled: Boolean = false,
    val delTime: Float = 300f,       // ms
    val delFeedback: Float = 40f,    // 0% - 100%
    val delMix: Float = 20f,         // 0% - 100%

    // 3D Audio Engine
    val audio3dMode: String = "Studio", // "Studio", "Cinema", "Live Concert", "Hall", "Arena"
    val audio3dWidth: Float = 100f,     // 0% - 300%
    val audio3dDepth: Float = 50f,      // 0% - 100%
    val audio3dDistance: Float = 30f,   // 0% - 100%

    // Surround
    val surroundMode: String = "5.1",   // "Stereo", "5.1", "7.1"

    // Vocal Processor
    val vocalEnhancer: Float = 0f,   // 0% - 100%
    val deEsser: Float = 0f,         // 0% - 100%
    val harmonicExciter: Float = 0f, // 0% - 100%

    // Stereo Width
    val stereoWidth: Float = 100f,   // 0% - 300%
    val stereoMode: String = "Stereo", // "Mono", "Stereo", "Wide", "Ultra Wide"

    // Loudness Maximizer
    val loudnessMaximizerMode: String = "Safe", // "Safe", "Strong", "Extreme"
    
    // Crossfeed
    val crossfeedEnabled: Boolean = false,

    // Metadata
    val isSystemPreset: Boolean = false,
    val targetDeviceType: String = "All" // "All", "Internal Speaker", "Wired Headset", "Bluetooth", "USB DAC", "HDMI"
)
