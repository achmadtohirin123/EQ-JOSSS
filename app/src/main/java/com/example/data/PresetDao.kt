package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {
    @Query("SELECT * FROM presets ORDER BY name ASC")
    fun getAllPresetsFlow(): Flow<List<PresetEntity>>

    @Query("SELECT * FROM presets WHERE id = :id")
    suspend fun getPresetById(id: Int): PresetEntity?

    @Query("SELECT * FROM presets WHERE targetDeviceType = :deviceType OR targetDeviceType = 'All'")
    fun getPresetsForDeviceFlow(deviceType: String): Flow<List<PresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: PresetEntity): Long

    @Update
    suspend fun updatePreset(preset: PresetEntity)

    @Delete
    suspend fun deletePreset(preset: PresetEntity)

    @Query("DELETE FROM presets WHERE id = :id")
    suspend fun deletePresetById(id: Int)

    // Prepopulate check
    @Query("SELECT COUNT(*) FROM presets")
    suspend fun getPresetCount(): Int
}
