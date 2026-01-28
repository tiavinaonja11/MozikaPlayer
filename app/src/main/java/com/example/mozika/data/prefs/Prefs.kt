package com.example.mozika.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("mozika")

@Singleton
class Prefs @Inject constructor(private val ctx: Context) {

    private val currentTrack = longPreferencesKey("current_track")
    private val currentPosition = longPreferencesKey("current_position")

    val trackFlow: Flow<Long> = ctx.dataStore.data.map { it[currentTrack] ?: -1L }
    val positionFlow: Flow<Long> = ctx.dataStore.data.map { it[currentPosition] ?: 0L }

    suspend fun save(trackId: Long, pos: Long) {
        ctx.dataStore.edit {
            it[currentTrack] = trackId
            it[currentPosition] = pos
        }
    }
}