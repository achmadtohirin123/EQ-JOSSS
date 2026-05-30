package com.example.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PresetEntity
import com.example.data.PresetRepository
import com.example.service.AudioProcessingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File

class AudioProcessingViewModel(application: Application) : AndroidViewModel(application) {

    private val presetRepository: PresetRepository
    private val database = AppDatabase.getDatabase(application, viewModelScope)

    // Binding state to Foreground DSP service
    private val _isServiceBound = MutableStateFlow(false)
    val isServiceBound = _isServiceBound.asStateFlow()

    private var dspService: AudioProcessingService? = null

    init {
        presetRepository = PresetRepository(database.presetDao())
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (database.presetDao().getPresetCount() == 0) {
                    AppDatabase.populateDefaultPresets(database.presetDao())
                }
            } catch (e: Throwable) {
                // Ignore fallback exceptions
            }
        }
        startAndBindService()
    }

    // Expose repository presets
    val userPresetsList = presetRepository.allPresets.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Dynamic state streams derived from Service or local fallback
    private val _isEngineActive = MutableStateFlow(true)
    val isEngineActive = _isEngineActive.asStateFlow()

    private val _currentDevice = MutableStateFlow("Internal Speaker")
    val currentDevice = _currentDevice.asStateFlow()

    private val _currentPresetName = MutableStateFlow("Flat (Default)")
    val currentPresetName = _currentPresetName.asStateFlow()

    private val _activePresetState = MutableStateFlow(PresetEntity(id = 1, name = "Flat (Default)", isSystemPreset = true))
    val activePresetState = _activePresetState.asStateFlow()

    private var dbSaveJob: kotlinx.coroutines.Job? = null

    private val _faderLeft = MutableStateFlow(0f)
    val faderLeft = _faderLeft.asStateFlow()

    private val _faderRight = MutableStateFlow(0f)
    val faderRight = _faderRight.asStateFlow()

    private val _stereoWidth = MutableStateFlow(100f)
    val stereoWidth = _stereoWidth.asStateFlow()

    private val _stereoMode = MutableStateFlow("Stereo")
    val stereoMode = _stereoMode.asStateFlow()

    private val _fftData = MutableStateFlow(FloatArray(64) { 0f })
    val fftData = _fftData.asStateFlow()

    private val _waveData = MutableStateFlow(FloatArray(128) { 0f })
    val waveData = _waveData.asStateFlow()

    private val _vuLeftPeak = MutableStateFlow(-60f)
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

    // Config options state
    private val _sampleRate = MutableStateFlow(48000)
    val sampleRate = _sampleRate.asStateFlow()

    private val _bufferSize = MutableStateFlow(256)
    val bufferSize = _bufferSize.asStateFlow()

    private val _playbackLatency = MutableStateFlow(5)
    val playbackLatency = _playbackLatency.asStateFlow()

    private val _visualizerFps = MutableStateFlow(60)
    val visualizerFps = _visualizerFps.asStateFlow()

    private val _fftSize = MutableStateFlow(2048)
    val fftSize = _fftSize.asStateFlow()

    private val _visualizerMode = MutableStateFlow("Filled Spectrum")
    val visualizerMode = _visualizerMode.asStateFlow()

    // Screen selected navigation tab (e.g. "HOME", "EQ", "ADVANCED FX", "PRESETS", "AUTO EQ", "SETTINGS")
    private val _selectedTab = MutableStateFlow("HOME")
    val selectedTab = _selectedTab.asStateFlow()

    // AutoEQ database lists containing several real profile tunings
    val autoEqProfiles = listOf(
        AutoEqProfile("Sennheiser HD 600", "4.5,3.0,1.5,-0.5,-1.5,-2.0,-1.0,0.5,1.5,2.5,3.0,4.0,4.5,5.0,4.5", "Sennheiser"),
        AutoEqProfile("Sony WH-1000XM4", "-2.0,-3.5,-4.0,-2.5,-1.0,0.5,1.5,2.0,2.5,1.0,-1.5,-2.0,-1.0,1.5,2.5", "Sony"),
        AutoEqProfile("Apple AirPods Pro", "-1.0,-1.5,0.0,0.5,1.0,1.5,2.0,1.5,1.0,-0.5,-1.5,0.0,1.5,2.0,1.5", "Apple"),
        AutoEqProfile("Beyerdynamic DT 990 Pro", "5.0,4.0,2.5,1.0,-1.5,-3.0,-4.5,-5.0,-2.5,1.0,3.5,6.0,7.5,8.0,7.0", "Beyerdynamic"),
        AutoEqProfile("Audio-Technica ATH-M50x", "-1.5,-1.0,0.5,1.5,1.0,0.0,-1.0,-1.5,-0.5,1.0,2.5,3.5,4.0,4.5,4.0", "Audio-Technica"),
        AutoEqProfile("Bose QuietComfort 45", "-3.5,-3.0,-1.5,0.0,1.0,1.5,2.0,2.5,2.0,1.0,-1.0,-2.5,-1.5,1.0,2.5", "Bose"),
        AutoEqProfile("Samsung Galaxy Buds2 Pro", "-1.0,-0.5,0.5,1.0,1.5,2.5,3.0,2.5,1.5,0.5,-1.0,-1.5,-0.5,1.0,1.5", "Samsung"),
        AutoEqProfile("Shure SE215 Professional", "5.0,4.5,3.5,2.0,0.5,-1.0,-2.0,-2.5,-3.0,-2.5,-1.5,-0.5,0.5,1.5,2.0", "Shure")
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val filteredAutoEqProfiles = _searchQuery.map { query ->
        if (query.isEmpty()) autoEqProfiles
        else autoEqProfiles.filter { it.name.contains(query, ignoreCase = true) || it.brand.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), autoEqProfiles)

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioProcessingService.LocalBinder
            val boundService = binder.getService()
            dspService = boundService
            _isServiceBound.value = true

            // Combine/hook flows
            viewModelScope.launch {
                launch { boundService.isEngineActive.collect { _isEngineActive.value = it } }
                launch { boundService.currentDevice.collect { _currentDevice.value = it } }
                launch { boundService.selectedPresetName.collect { _currentPresetName.value = it } }
                launch { boundService.faderLeft.collect { _faderLeft.value = it } }
                launch { boundService.faderRight.collect { _faderRight.value = it } }
                launch { boundService.stereoWidth.collect { _stereoWidth.value = it } }
                launch { boundService.stereoMode.collect { _stereoMode.value = it } }
                launch { boundService.fftData.collect { _fftData.value = it } }
                launch { boundService.waveData.collect { _waveData.value = it } }
                launch { boundService.vuLeftPeak.collect { _vuLeftPeak.value = it } }
                launch { boundService.vuRightPeak.collect { _vuRightPeak.value = it } }
                launch { boundService.vuLeftRms.collect { _vuLeftRms.value = it } }
                launch { boundService.vuRightRms.collect { _vuRightRms.value = it } }
                launch { boundService.isClippingLeft.collect { _isClippingLeft.value = it } }
                launch { boundService.isClippingRight.collect { _isClippingRight.value = it } }
                launch { boundService.sampleRate.collect { _sampleRate.value = it } }
                launch { boundService.bufferSize.collect { _bufferSize.value = it } }
                launch { boundService.playbackLatency.collect { _playbackLatency.value = it } }
                launch { boundService.visualizerFps.collect { _visualizerFps.value = it } }
                launch { boundService.fftSize.collect { _fftSize.value = it } }
                launch { boundService.visualizerMode.collect { _visualizerMode.value = it } }

                // Apply initial default profile once bound without blocking
                launch {
                    userPresetsList.collect { presets ->
                        if (presets.isNotEmpty() && _currentPresetName.value == "Flat (Default)" && dspService?.activePreset?.value == null) {
                            presets.firstOrNull { it.id == 1 || it.name.contains("Default") || it.name.contains("Flat") }?.let {
                                applyPreset(it)
                            }
                        }
                    }
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            _isServiceBound.value = false
            dspService = null
        }
    }

    private fun startAndBindService() {
        val app = getApplication<Application>()
        val serviceIntent = Intent(app, AudioProcessingService::class.java)
        
        // Start the service safely as a standard background service first while in foreground
        try {
            app.startService(serviceIntent)
        } catch (e: Throwable) {
            // If background execution restriction is hit, we just rely on bindService below
        }
        
        // Bind to it
        try {
            app.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        } catch (e: Throwable) {
            // Ignored - system bound failure fallback
        }
    }

    fun setSelectedTab(tab: String) {
        _selectedTab.value = tab
    }

    fun setQuery(q: String) {
        _searchQuery.value = q
    }

    fun togglePower() {
        val nextState = !_isEngineActive.value
        _isEngineActive.value = nextState
        dspService?.toggleEngine()
    }

    fun setFaderLeft(value: Float) {
        _faderLeft.value = value
        dspService?.setFaders(value, _faderRight.value)
    }

    fun setFaderRight(value: Float) {
        _faderRight.value = value
        dspService?.setFaders(_faderLeft.value, value)
    }

    fun setStereo(width: Float, mode: String) {
        _stereoWidth.value = width
        _stereoMode.value = mode
        dspService?.setStereo(width, mode)
    }

    fun applyPreset(preset: PresetEntity) {
        dbSaveJob?.cancel()
        _currentPresetName.value = preset.name
        _stereoWidth.value = preset.stereoWidth
        _stereoMode.value = preset.stereoMode
        _activePresetState.value = preset
        dspService?.applyPreset(preset)
    }

    fun updateActivePresetState(updated: PresetEntity) {
        _activePresetState.value = updated
        _currentPresetName.value = updated.name
        _stereoWidth.value = updated.stereoWidth
        _stereoMode.value = updated.stereoMode
        dspService?.applyPreset(updated)

        // Debounce saving to Room database to avoid hammering SQLite during drags
        dbSaveJob?.cancel()
        dbSaveJob = viewModelScope.launch(Dispatchers.IO) {
            kotlinx.coroutines.delay(350)
            try {
                presetRepository.insertPreset(updated)
            } catch (e: Exception) {
                // Ignore fallback exceptions
            }
        }
    }

    fun tryStartRealVisualizer() {
        dspService?.tryStartRealVisualizer()
    }

    fun updateEngineSettings(rate: Int, size: Int, latency: Int) {
        dspService?.updateEngineSettings(rate, size, latency)
    }

    fun updateVisualizerSettings(fps: Int, fft: Int, mode: String) {
        dspService?.updateVisualizerSettings(fps, fft, mode)
    }

    // Preset Actions (Room Database mapping)
    fun saveCustomPreset(preset: PresetEntity) {
        _activePresetState.value = preset
        dbSaveJob?.cancel()
        dbSaveJob = viewModelScope.launch(Dispatchers.IO) {
            kotlinx.coroutines.delay(350)
            try {
                presetRepository.insertPreset(preset)
            } catch (e: Exception) {
                // Ignore fallback exceptions
            }
        }
    }

    fun deletePreset(preset: PresetEntity) {
        viewModelScope.launch {
            presetRepository.deletePreset(preset)
        }
    }

    // CSV band parser utilities
    fun bandsToList(bandsCsv: String): List<Float> {
        return bandsCsv.split(",").mapNotNull { it.toFloatOrNull() }
    }

    fun listToBands(list: List<Float>): String {
        return list.joinToString(",")
    }

    // Export entire preset collection to JSON
    fun exportPresetsToJson(context: Context): String? {
        return try {
            val presets = userPresetsList.value
            val root = JSONObject()
            val array = org.json.JSONArray()
            for (p in presets) {
                val obj = JSONObject().apply {
                    put("name", p.name)
                    put("eqMode", p.eqMode)
                    put("eqBandsCsv", p.eqBandsCsv)
                    put("parametricBandsCsv", p.parametricBandsCsv)
                    put("compressorEnabled", p.compressorEnabled)
                    put("compThreshold", p.compThreshold.toDouble())
                    put("compRatio", p.compRatio.toDouble())
                    put("compAttack", p.compAttack.toDouble())
                    put("compRelease", p.compRelease.toDouble())
                    put("compKnee", p.compKnee.toDouble())
                    put("compMakeupGain", p.compMakeupGain.toDouble())
                    put("limiterEnabled", p.limiterEnabled)
                    put("limCeiling", p.limCeiling.toDouble())
                    put("limRelease", p.limRelease.toDouble())
                    put("limLookAhead", p.limLookAhead.toDouble())
                    put("bassBoost", p.bassBoost.toDouble())
                    put("deepBass", p.deepBass.toDouble())
                    put("subHarmonic", p.subHarmonic.toDouble())
                    put("airGain", p.airGain.toDouble())
                    put("presenceGain", p.presenceGain.toDouble())
                    put("sparkleGain", p.sparkleGain.toDouble())
                    put("reverbEnabled", p.reverbEnabled)
                    put("revPreset", p.revPreset)
                    put("revDecay", p.revDecay.toDouble())
                    put("revSize", p.revSize.toDouble())
                    put("revWet", p.revWet.toDouble())
                    put("revDry", p.revDry.toDouble())
                    put("delayEnabled", p.delayEnabled)
                    put("delTime", p.delTime.toDouble())
                    put("delFeedback", p.delFeedback.toDouble())
                    put("delMix", p.delMix.toDouble())
                    put("audio3dMode", p.audio3dMode)
                    put("audio3dWidth", p.audio3dWidth.toDouble())
                    put("audio3dDepth", p.audio3dDepth.toDouble())
                    put("audio3dDistance", p.audio3dDistance.toDouble())
                    put("surroundMode", p.surroundMode)
                    put("vocalEnhancer", p.vocalEnhancer.toDouble())
                    put("deEsser", p.deEsser.toDouble())
                    put("harmonicExciter", p.harmonicExciter.toDouble())
                    put("stereoWidth", p.stereoWidth.toDouble())
                    put("stereoMode", p.stereoMode)
                    put("loudnessMaximizerMode", p.loudnessMaximizerMode)
                    put("crossfeedEnabled", p.crossfeedEnabled)
                }
                array.put(obj)
            }
            root.put("presets", array)
            val jsonString = root.toString(2)
            
            // Save to internal storage
            val file = File(context.filesDir, "bro_eq_presets_backup.json")
            file.writeText(jsonString)
            jsonString
        } catch (e: Exception) {
            null
        }
    }

    // Import backups from JSON config
    fun importPresetsFromJson(jsonContent: String): Boolean {
        return try {
            val root = JSONObject(jsonContent)
            val array = root.getJSONArray("presets")
            viewModelScope.launch {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val preset = PresetEntity(
                        name = obj.getString("name"),
                        eqMode = obj.optString("eqMode", "15 Band"),
                        eqBandsCsv = obj.optString("eqBandsCsv", List(15) { "0.0" }.joinToString(",")),
                        parametricBandsCsv = obj.optString("parametricBandsCsv", ""),
                        compressorEnabled = obj.optBoolean("compressorEnabled", false),
                        compThreshold = obj.optDouble("compThreshold", -20.0).toFloat(),
                        compRatio = obj.optDouble("compRatio", 4.0).toFloat(),
                        compAttack = obj.optDouble("compAttack", 20.0).toFloat(),
                        compRelease = obj.optDouble("compRelease", 250.0).toFloat(),
                        compKnee = obj.optDouble("compKnee", 5.0).toFloat(),
                        compMakeupGain = obj.optDouble("compMakeupGain", 0.0).toFloat(),
                        limiterEnabled = obj.optBoolean("limiterEnabled", false),
                        limCeiling = obj.optDouble("limCeiling", -0.1).toFloat(),
                        limRelease = obj.optDouble("limRelease", 100.0).toFloat(),
                        limLookAhead = obj.optDouble("limLookAhead", 2.0).toFloat(),
                        bassBoost = obj.optDouble("bassBoost", 0.0).toFloat(),
                        deepBass = obj.optDouble("deepBass", 0.0).toFloat(),
                        subHarmonic = obj.optDouble("subHarmonic", 0.0).toFloat(),
                        airGain = obj.optDouble("airGain", 0.0).toFloat(),
                        presenceGain = obj.optDouble("presenceGain", 0.0).toFloat(),
                        sparkleGain = obj.optDouble("sparkleGain", 0.0).toFloat(),
                        reverbEnabled = obj.optBoolean("reverbEnabled", false),
                        revPreset = obj.optString("revPreset", "Room"),
                        revDecay = obj.optDouble("revDecay", 1.5).toFloat(),
                        revSize = obj.optDouble("revSize", 50.0).toFloat(),
                        revWet = obj.optDouble("revWet", 30.0).toFloat(),
                        revDry = obj.optDouble("revDry", 100.0).toFloat(),
                        delayEnabled = obj.optBoolean("delayEnabled", false),
                        delTime = obj.optDouble("delTime", 300.0).toFloat(),
                        delFeedback = obj.optDouble("delFeedback", 40.0).toFloat(),
                        delMix = obj.optDouble("delMix", 20.0).toFloat(),
                        audio3dMode = obj.optString("audio3dMode", "Studio"),
                        audio3dWidth = obj.optDouble("audio3dWidth", 100.0).toFloat(),
                        audio3dDepth = obj.optDouble("audio3dDepth", 50.0).toFloat(),
                        audio3dDistance = obj.optDouble("audio3dDistance", 30.0).toFloat(),
                        surroundMode = obj.optString("surroundMode", "5.1"),
                        vocalEnhancer = obj.optDouble("vocalEnhancer", 0.0).toFloat(),
                        deEsser = obj.optDouble("deEsser", 0.0).toFloat(),
                        harmonicExciter = obj.optDouble("harmonicExciter", 0.0).toFloat(),
                        stereoWidth = obj.optDouble("stereoWidth", 100.0).toFloat(),
                        stereoMode = obj.optString("stereoMode", "Stereo"),
                        loudnessMaximizerMode = obj.optString("loudnessMaximizerMode", "Safe"),
                        crossfeedEnabled = obj.optBoolean("crossfeedEnabled", false),
                        isSystemPreset = false
                    )
                    presetRepository.insertPreset(preset)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun onCleared() {
        val app = getApplication<Application>()
        try {
            app.unbindService(serviceConnection)
        } catch (e: Exception) {
            // Service already unbound
        }
        super.onCleared()
    }
}

data class AutoEqProfile(
    val name: String,
    val eqBandsCsv: String,
    val brand: String
)
