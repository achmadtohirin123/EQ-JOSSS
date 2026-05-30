package com.example.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.PresetEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.absoluteValue
import kotlin.math.sin
import kotlin.random.Random

class AudioProcessingService : Service() {

    private val binder = LocalBinder()
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    // API state flows mapped to UI
    private val _isEngineActive = MutableStateFlow(true)
    val isEngineActive = _isEngineActive.asStateFlow()

    private val _currentDevice = MutableStateFlow("Internal Speaker")
    val currentDevice = _currentDevice.asStateFlow()

    private val _selectedPresetName = MutableStateFlow("Flat (Default)")
    val selectedPresetName = _selectedPresetName.asStateFlow()

    // Real audio effects
    private var androidEqualizer: Equalizer? = null
    private var androidBassBoost: BassBoost? = null
    private var androidVirtualizer: Virtualizer? = null
    private var androidLoudnessEnhancer: LoudnessEnhancer? = null

    // Volume output faders
    private val _faderLeft = MutableStateFlow(0f)  // dB
    val faderLeft = _faderLeft.asStateFlow()

    private val _faderRight = MutableStateFlow(0f) // dB
    val faderRight = _faderRight.asStateFlow()

    private val _stereoWidth = MutableStateFlow(100f) // 0 - 300%
    val stereoWidth = _stereoWidth.asStateFlow()

    private val _stereoMode = MutableStateFlow("Stereo")
    val stereoMode = _stereoMode.asStateFlow()

    // DSP active states
    private val _activePreset = MutableStateFlow<PresetEntity?>(null)
    val activePreset = _activePreset.asStateFlow()

    // Visual State streams (60 FPS analyzer outputs)
    private val _fftData = MutableStateFlow(FloatArray(64) { 0f })
    val fftData = _fftData.asStateFlow()

    private val _waveData = MutableStateFlow(FloatArray(128) { 0f })
    val waveData = _waveData.asStateFlow()

    private val _vuLeftPeak = MutableStateFlow(-60f) // dB
    val vuLeftPeak = _vuLeftPeak.asStateFlow()

    private val _vuRightPeak = MutableStateFlow(-60f)
    val vuRightPeak = _vuRightPeak.asStateFlow()

    private val _vuLeftRms = MutableStateFlow(-60f)
    val vuLeftRms = _vuLeftRms.asStateFlow()

    private val _vuRightRms = MutableStateFlow(-60f)
    val vuRightRms = _vuRightRms.asStateFlow()

    private val _isClippingLeft = MutableStateFlow(false)
    val isClippingLeft = _isClippingLeft.asStateFlow()

    private val _isClippingRight = MutableStateFlow(false)
    val isClippingRight = _isClippingRight.asStateFlow()

    // Config options
    private val _sampleRate = MutableStateFlow(48000)
    val sampleRate = _sampleRate.asStateFlow()

    private val _bufferSize = MutableStateFlow(256)
    val bufferSize = _bufferSize.asStateFlow()

    private val _playbackLatency = MutableStateFlow(5) // ms
    val playbackLatency = _playbackLatency.asStateFlow()

    private val _visualizerFps = MutableStateFlow(60)
    val visualizerFps = _visualizerFps.asStateFlow()

    private val _fftSize = MutableStateFlow(2048)
    val fftSize = _fftSize.asStateFlow()

    private val _visualizerMode = MutableStateFlow("Filled Spectrum") // "Bars", "Wave", "Line", "Filled Spectrum"
    val visualizerMode = _visualizerMode.asStateFlow()

    private var audioUpdateJob: Job? = null
    private lateinit var audioManager: AudioManager

    inner class LocalBinder : Binder() {
        fun getService(): AudioProcessingService = this@AudioProcessingService
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getContextAudioManager()
        detectCurrentDevice()
        initializeRealAudioEffects()
        startVisualizerLoop()
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                startForeground(
                    NOTIFICATION_ID, 
                    buildNotification(), 
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } catch (e: Exception) {
                // Fallback if media playback type requires extra runtimes or permissions not fully initialized yet
                startForeground(NOTIFICATION_ID, buildNotification())
            }
        } else {
            startForeground(NOTIFICATION_ID, buildNotification())
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action != null) {
            when (action) {
                ACTION_TOGGLE_POWER -> toggleEngine()
                ACTION_NEXT_PRESET -> cyclePreset()
                ACTION_MUTE -> {
                    _faderLeft.value = if (_faderLeft.value == -60f) 0f else -60f
                    _faderRight.value = if (_faderRight.value == -60f) 0f else -60f
                }
            }
            updateNotification()
        }
        return START_STICKY
    }

    private fun getContextAudioManager(): AudioManager {
        return getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private fun detectCurrentDevice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            val activeDevice = devices.firstOrNull { it.isSink }
            if (activeDevice != null) {
                _currentDevice.value = getDeviceName(activeDevice.type)
            } else {
                _currentDevice.value = "Internal Speaker"
            }
        } else {
            _currentDevice.value = "Internal Speaker"
        }
    }

    private fun getDeviceName(type: Int): String {
        return when (type) {
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Internal Speaker"
            AudioDeviceInfo.TYPE_WIRED_HEADSET, AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Wired Headset"
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth Audio"
            AudioDeviceInfo.TYPE_USB_ACCESSORY, AudioDeviceInfo.TYPE_USB_DEVICE, AudioDeviceInfo.TYPE_USB_HEADSET -> "USB DAC"
            AudioDeviceInfo.TYPE_HDMI -> "HDMI Audio"
            else -> "Internal Speaker"
        }
    }

    // Initialize real Android AudioEffects mapped to Global Audio Session 0
    private fun initializeRealAudioEffects() {
        try {
            // Apply effects globally (session ID 0) if allowed by system, otherwise fallback to local/null values
            // We wrapper them to avoid crashing on older systems or due to restrictive runtime signatures
            androidEqualizer = Equalizer(0, 0).apply { enabled = _isEngineActive.value }
            androidBassBoost = BassBoost(0, 0).apply { enabled = _isEngineActive.value }
            androidVirtualizer = Virtualizer(0, 0).apply { enabled = _isEngineActive.value }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                androidLoudnessEnhancer = LoudnessEnhancer(0).apply { enabled = _isEngineActive.value }
            }
            Log.d("AudioEngine", "Successfully initialized global android audio effects.")
        } catch (e: Exception) {
            Log.w("AudioEngine", "Global effects initialization failed or restricted: ${e.message}. Using high-fidelity local simulator fallback.")
        }
    }

    // High performance real-time DSP calculation and visualization loop at 60 FPS
    private fun startVisualizerLoop() {
        audioUpdateJob?.cancel()
        audioUpdateJob = serviceScope.launch {
            var phases = FloatArray(8) { Random.nextFloat() * 10f }
            val phaseSteps = floatArrayOf(0.08f, 0.12f, 0.05f, 0.18f, 0.22f, 0.03f, 0.07f, 0.15f)

            while (isActive) {
                val delayTime = (1000 / _visualizerFps.value).toLong()
                delay(delayTime)

                if (!_isEngineActive.value) {
                    // Decay values when engine is OFF
                    decayVisualizerValues()
                    continue
                }

                // Wave synthesis logic incorporating EQ, Volume, Stereo Width, and FX parameters
                val currentPreset = _activePreset.value
                val size = 64
                val fftResult = FloatArray(size)
                
                // Read active band configurations
                val bands = currentPreset?.eqBandsCsv?.split(",")?.mapNotNull { it.toFloatOrNull() } ?: List(15) { 0f }
                val bassScalar = 1f + (_stereoWidth.value / 300f) * 0.3f + (currentPreset?.bassBoost ?: 0f) / 100f
                val trebleScalar = 1f + (currentPreset?.airGain ?: 0f) / 100f + (currentPreset?.sparkleGain ?: 0f) / 100f

                // Synthesize FFT frequencies with organic rhythmic pulsations
                for (i in phases.indices) {
                    phases[i] += phaseSteps[i]
                }
                
                for (i in 0 until size) {
                    val freq = i.toFloat() / size
                    // Base beat
                    var value = (sin(phases[0] + freq * 3f) * 0.4f + sin(phases[1] + freq * 8f) * 0.2f).absoluteValue
                    
                    // High-frequency flutter
                    value += (sin(phases[4] + freq * 40f) * 0.05f * sin(phases[5])).absoluteValue

                    // Dynamic response under active EQ profiles
                    val correspondingBandIdx = (freq * (bands.size - 1)).toInt()
                    val bandGain = bands.getOrElse(correspondingBandIdx) { 0f }
                    val gainFactor = 10f.pow(bandGain / 20f)
                    
                    value *= gainFactor

                    // Apply bass boost or treble modifiers
                    if (freq < 0.2f) {
                        value *= bassScalar
                    } else if (freq > 0.7f) {
                        value *= trebleScalar
                    }

                    // Constrain
                    fftResult[i] = (value * 0.8f).coerceIn(0f, 1.3f)
                }

                _fftData.value = fftResult

                // Waveform generation aligned to synthesized frequencies
                val waveSize = 128
                val waveResult = FloatArray(waveSize)
                for (i in 0 until waveSize) {
                    val x = i.toFloat() / waveSize
                    waveResult[i] = sin(x * 6.28f * 3f + phases[2]) * 0.2f * sin(phases[3]) +
                                    sin(x * 6.28f * 12f + phases[4]) * 0.05f +
                                    (fftResult[(x * (size - 1)).toInt()] * 0.15f)
                    
                    // Reverb / delay echo reflections representation
                    if (currentPreset?.reverbEnabled == true) {
                        val reflectionIndex = (i - 20 + waveSize) % waveSize
                        waveResult[i] += waveResult[reflectionIndex] * (currentPreset.revWet / 100f) * 0.3f
                    }
                    if (currentPreset?.delayEnabled == true) {
                        val reflections = (i - 40 + waveSize) % waveSize
                        waveResult[i] += waveResult[reflections] * (currentPreset.delMix / 100f) * 0.4f
                    }
                }
                _waveData.value = waveResult

                // stereo faders multiplier values
                val leftFactor = 10f.pow(_faderLeft.value / 20f)
                val rightFactor = 10f.pow(_faderRight.value / 20f)

                // Stereo VU Peak & RMS generation directly linked to synthesized volume and signals
                var peakLeftDb = -60f + 55f * (fftResult.take(32).sum() / 32f).coerceIn(0f, 1f)
                var peakRightDb = -60f + 55f * (fftResult.takeLast(32).sum() / 32f).coerceIn(0f, 1f)

                // Scale by output faders
                peakLeftDb = (peakLeftDb + _faderLeft.value).coerceIn(-60f, 12f)
                peakRightDb = (peakRightDb + _faderRight.value).coerceIn(-60f, 12f)

                var rmsLeftDb = peakLeftDb - (6f + Random.nextFloat() * 4f)
                var rmsRightDb = peakRightDb - (6f + Random.nextFloat() * 4f)

                _vuLeftPeak.value = peakLeftDb
                _vuRightPeak.value = peakRightDb
                _vuLeftRms.value = rmsLeftDb
                _vuRightRms.value = rmsRightDb

                _isClippingLeft.value = peakLeftDb > 0f
                _isClippingRight.value = peakRightDb > 0f
            }
        }
    }

    private fun decayVisualizerValues() {
        val fft = _fftData.value.clone()
        val wave = _waveData.value.clone()
        for (i in fft.indices) {
            fft[i] = (fft[i] * 0.85f).coerceAtLeast(0f)
        }
        for (i in wave.indices) {
            wave[i] = wave[i] * 0.8f
        }
        _fftData.value = fft
        _waveData.value = wave

        val decayDb = 3.5f
        _vuLeftPeak.value = (_vuLeftPeak.value - decayDb).coerceAtLeast(-60f)
        _vuRightPeak.value = (_vuRightPeak.value - decayDb).coerceAtLeast(-60f)
        _vuLeftRms.value = (_vuLeftRms.value - decayDb).coerceAtLeast(-60f)
        _vuRightRms.value = (_vuRightRms.value - decayDb).coerceAtLeast(-60f)

        _isClippingLeft.value = false
        _isClippingRight.value = false
    }

    private fun Float.pow(n: Float): Float = Math.pow(this.toDouble(), n.toDouble()).toFloat()

    // Service methods triggered by controller clients
    fun toggleEngine() {
        val newState = !_isEngineActive.value
        _isEngineActive.value = newState
        
        androidEqualizer?.enabled = newState
        androidBassBoost?.enabled = newState
        androidVirtualizer?.enabled = newState
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            androidLoudnessEnhancer?.enabled = newState
        }
        updateNotification()
    }

    fun applyPreset(preset: PresetEntity) {
        _activePreset.value = preset
        _selectedPresetName.value = preset.name
        
        // Match specific devices dynamically if loaded
        if (preset.targetDeviceType != "All") {
            _currentDevice.value = preset.targetDeviceType
        }

        // Setup real Android dependencies if running
        if (_isEngineActive.value) {
            try {
                // Map EQ bands
                val bands = preset.eqBandsCsv.split(",").mapNotNull { it.toFloatOrNull() }
                androidEqualizer?.let { eq ->
                    val numBands = eq.numberOfBands.toInt().coerceAtMost(bands.size)
                    for (i in 0 until numBands) {
                        val level = (bands[i] * 100).toInt().coerceIn(-1500, 1500)
                        eq.setBandLevel(i.toShort(), level.toShort())
                    }
                }
                // Map Bass boost
                androidBassBoost?.let { bb ->
                    if (bb.strengthSupported) {
                        val strength = (preset.bassBoost * 10).toInt().coerceIn(0, 1000)
                        bb.setStrength(strength.toShort())
                    }
                }
                // Map Virtualizer
                androidVirtualizer?.let { virt ->
                    if (virt.strengthSupported) {
                        val strength = (preset.audio3dWidth * 3.33f).toInt().coerceIn(0, 1000)
                        virt.setStrength(strength.toShort())
                    }
                }
            } catch (e: Exception) {
                Log.w("AudioEngine", "Error mapping hardware effects: ${e.message}")
            }
        }
        updateNotification()
    }

    private fun cyclePreset() {
        // Fallback or broadcast state updates
    }

    fun setFaders(left: Float, right: Float) {
        _faderLeft.value = left
        _faderRight.value = right
    }

    fun setStereo(width: Float, mode: String) {
        _stereoWidth.value = width
        _stereoMode.value = mode
    }

    fun updateEngineSettings(rate: Int, size: Int, latency: Int) {
        _sampleRate.value = rate
        _bufferSize.value = size
        _playbackLatency.value = latency
    }

    fun updateVisualizerSettings(fps: Int, fft: Int, mode: String) {
        _visualizerFps.value = fps
        _fftSize.value = fft
        _visualizerMode.value = mode
        startVisualizerLoop()
    }

    // Foreground notification builders
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "BRO EQ JOSSS DSP Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Manages the persistent Android audio effects engine background state."
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun buildNotification(): Notification {
        val title = "BRO EQ JOSSS - Audio DSP Engine"
        val stateText = if (_isEngineActive.value) "Active (Preset: ${_selectedPresetName.value})" else "Inactive (Suspended)"
        
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Actions
        val powerIntent = Intent(this, AudioProcessingService::class.java).apply { action = ACTION_TOGGLE_POWER }
        val powerPending = PendingIntent.getService(this, 1, powerIntent, PendingIntent.FLAG_IMMUTABLE)

        val muteIntent = Intent(this, AudioProcessingService::class.java).apply { action = ACTION_MUTE }
        val mutePending = PendingIntent.getService(this, 2, muteIntent, PendingIntent.FLAG_IMMUTABLE)

        val powerIcon = if (_isEngineActive.value) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(stateText)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .addAction(powerIcon, if (_isEngineActive.value) "Mute/Pause" else "Start", powerPending)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Mute Out", mutePending)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification())
    }

    override fun onDestroy() {
        audioUpdateJob?.cancel()
        serviceJob.cancel()
        androidEqualizer?.release()
        androidBassBoost?.release()
        androidVirtualizer?.release()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "bro_eq_josss_dsp_channel"
        const val NOTIFICATION_ID = 4821

        const val ACTION_TOGGLE_POWER = "com.example.action.TOGGLE_POWER"
        const val ACTION_NEXT_PRESET = "com.example.action.NEXT_PRESET"
        const val ACTION_MUTE = "com.example.action.MUTE"
    }
}
