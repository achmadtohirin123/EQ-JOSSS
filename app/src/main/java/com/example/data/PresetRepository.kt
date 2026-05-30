package com.example.data

import kotlinx.coroutines.flow.Flow

class PresetRepository(private val presetDao: PresetDao) {
    val allPresets: Flow<List<PresetEntity>> = presetDao.getAllPresetsFlow()

    fun getPresetsForDevice(deviceType: String): Flow<List<PresetEntity>> =
        presetDao.getPresetsForDeviceFlow(deviceType)

    suspend fun getPresetById(id: Int): PresetEntity? =
        presetDao.getPresetById(id)

    suspend fun insertPreset(preset: PresetEntity): Long =
        presetDao.insertPreset(preset)

    suspend fun updatePreset(preset: PresetEntity) =
        presetDao.updatePreset(preset)

    suspend fun deletePreset(preset: PresetEntity) =
        presetDao.deletePreset(preset)

    suspend fun deletePresetById(id: Int) =
        presetDao.deletePresetById(id)
}
