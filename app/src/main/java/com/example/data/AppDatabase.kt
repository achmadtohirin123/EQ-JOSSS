package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [PresetEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun presetDao(): PresetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bro_eq_josss_db"
                )
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDefaultPresets(database.presetDao())
                }
            }
        }

        private suspend fun populateDefaultPresets(presetDao: PresetDao) {
            // "Rock", "Pop", "Dangdut", "EDM", "Jazz", "Metal", "Hip Hop", "Acoustic", "Gaming", "Movie"
            val defaultPresets = listOf(
                PresetEntity(
                    id = 1,
                    name = "Flat (Default)",
                    isSystemPreset = true,
                    eqMode = "15 Band",
                    eqBandsCsv = List(15) { "0.0" }.joinToString(","),
                    targetDeviceType = "All"
                ),
                PresetEntity(
                    id = 2,
                    name = "Rock Mode",
                    isSystemPreset = true,
                    eqMode = "15 Band",
                    // Enhancing Bass and Treble for impactful rock
                    eqBandsCsv = "4.5,4.0,3.0,1.5,0.0,-1.0,0.0,1.0,2.0,3.5,4.0,4.5,4.5,4.5,4.0",
                    bassBoost = 40f,
                    deepBass = 30f,
                    airGain = 35f,
                    targetDeviceType = "All"
                ),
                PresetEntity(
                    id = 3,
                    name = "Pop Glow",
                    isSystemPreset = true,
                    eqMode = "15 Band",
                    // Mid and high vocal emphasis
                    eqBandsCsv = "-1.5,-1.0,0.0,1.5,2.0,2.5,3.0,2.5,2.0,1.5,1.0,1.5,2.0,2.5,2.5",
                    presenceGain = 45f,
                    sparkleGain = 20f,
                    targetDeviceType = "All"
                ),
                PresetEntity(
                    id = 4,
                    name = "Dangdut Koplo",
                    isSystemPreset = true,
                    eqMode = "15 Band",
                    // Rich deep bass (kendang) and crisp treble (suling/cengkok)
                    eqBandsCsv = "7.5,6.5,5.0,2.0,0.0,-1.5,-2.5,-1.5,0.0,2.5,4.5,6.0,7.0,7.5,6.5",
                    bassBoost = 75f,
                    deepBass = 60f,
                    subHarmonic = 50f,
                    sparkleGain = 55f,
                    targetDeviceType = "All"
                ),
                PresetEntity(
                    id = 5,
                    name = "EDM Dynamic",
                    isSystemPreset = true,
                    eqMode = "15 Band",
                    // Deep energetic U curve with compressor turned on
                    eqBandsCsv = "6.0,5.5,4.5,2.5,0.5,-1.5,-2.0,-1.5,0.5,2.5,4.5,5.5,6.0,6.5,7.0",
                    compressorEnabled = true,
                    compThreshold = -15f,
                    compRatio = 4f,
                    compAttack = 30f,
                    compRelease = 150f,
                    bassBoost = 65f,
                    deepBass = 50f,
                    stereoWidth = 150f,
                    stereoMode = "Wide",
                    targetDeviceType = "All"
                ),
                PresetEntity(
                    id = 6,
                    name = "Jazz Club",
                    isSystemPreset = true,
                    eqMode = "15 Band",
                    // Low-mid warmth with soft highs
                    eqBandsCsv = "3.0,3.0,2.5,2.0,1.5,1.0,0.5,0.5,0.0,-0.5,-1.0,-1.0,0.0,0.5,1.0",
                    reverbEnabled = true,
                    revPreset = "Plate",
                    revDecay = 1.2f,
                    revWet = 25f,
                    targetDeviceType = "All"
                ),
                PresetEntity(
                    id = 7,
                    name = "Metal Blast",
                    isSystemPreset = true,
                    eqMode = "15 Band",
                    // V-curve scoop
                    eqBandsCsv = "5.5,5.0,4.0,1.0,-2.0,-3.5,-4.0,-3.5,-2.0,1.0,4.0,5.0,5.5,6.0,6.0",
                    bassBoost = 50f,
                    subHarmonic = 30f,
                    airGain = 40f,
                    targetDeviceType = "All"
                ),
                PresetEntity(
                    id = 8,
                    name = "Hip Hop Sub",
                    isSystemPreset = true,
                    eqMode = "15 Band",
                    // Extreme low frequencies and bass
                    eqBandsCsv = "8.0,7.0,5.5,3.0,1.0,-1.0,-1.5,-1.0,0.5,1.5,2.5,3.5,4.5,5.0,5.0",
                    bassBoost = 85f,
                    deepBass = 80f,
                    subHarmonic = 70f,
                    targetDeviceType = "All"
                ),
                PresetEntity(
                    id = 9,
                    name = "Acoustic Vocal",
                    isSystemPreset = true,
                    eqMode = "15 Band",
                    // Warm mids, beautiful airy high ends
                    eqBandsCsv = "0.0,0.5,1.0,1.5,2.0,2.5,3.0,3.5,3.0,2.5,3.0,4.0,4.5,5.0,5.0",
                    presenceGain = 60f,
                    airGain = 50f,
                    targetDeviceType = "All"
                ),
                PresetEntity(
                    id = 10,
                    name = "Gaming Spatial",
                    isSystemPreset = true,
                    eqMode = "15 Band",
                    // Bass for explosions, elevated highs for footsteps, wide spatial mode
                    eqBandsCsv = "4.5,4.0,2.5,1.0,0.0,1.0,2.0,2.5,3.0,3.5,4.0,4.5,4.5,5.0,5.5",
                    audio3dMode = "Arena",
                    audio3dWidth = 180f,
                    audio3dDepth = 80f,
                    audio3dDistance = 60f,
                    stereoWidth = 180f,
                    stereoMode = "Ultra Wide",
                    targetDeviceType = "All"
                ),
                PresetEntity(
                    id = 11,
                    name = "Movie Theater",
                    isSystemPreset = true,
                    eqMode = "15 Band",
                    // Heavy rumble and spatial depth
                    eqBandsCsv = "5.5,5.0,4.0,2.0,0.5,-1.0,-1.5,-1.0,0.5,1.5,3.0,4.5,5.0,5.5,5.5",
                    audio3dMode = "Cinema",
                    audio3dWidth = 200f,
                    audio3dDepth = 90f,
                    stereoMode = "Wide",
                    reverbEnabled = true,
                    revPreset = "Hall",
                    revDecay = 2.5f,
                    revWet = 35f,
                    targetDeviceType = "All"
                )
            )

            for (preset in defaultPresets) {
                presetDao.insertPreset(preset)
            }
        }
    }
}
