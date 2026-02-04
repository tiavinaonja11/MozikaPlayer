package com.example.mozika.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val LAST_PLAYED_TRACK_ID = longPreferencesKey("last_played_track_id")
        private val IS_PLAYING = booleanPreferencesKey("is_playing")
        private val LAST_POSITION = longPreferencesKey("last_position")
        private val PLAYLIST_CONTEXT = stringPreferencesKey("playlist_context")
        private val PLAYLIST_CONTEXT_ID = stringPreferencesKey("playlist_context_id")
    }

    suspend fun savePlayerState(
        trackId: Long,
        isPlaying: Boolean,
        position: Long,
        playlistContext: String,
        contextId: String = ""
    ) {
        dataStore.edit { preferences ->
            preferences[LAST_PLAYED_TRACK_ID] = trackId
            preferences[IS_PLAYING] = isPlaying
            preferences[LAST_POSITION] = position
            preferences[PLAYLIST_CONTEXT] = playlistContext
            preferences[PLAYLIST_CONTEXT_ID] = contextId
        }
    }

    val playerState: Flow<PlayerState> = dataStore.data.map { preferences ->
        PlayerState(
            trackId = preferences[LAST_PLAYED_TRACK_ID] ?: 0L,
            isPlaying = preferences[IS_PLAYING] ?: false,
            position = preferences[LAST_POSITION] ?: 0L,
            playlistContext = preferences[PLAYLIST_CONTEXT] ?: "none",
            contextId = preferences[PLAYLIST_CONTEXT_ID] ?: ""
        )
    }
}

data class PlayerState(
    val trackId: Long,
    val isPlaying: Boolean,
    val position: Long,
    val playlistContext: String,
    val contextId: String = ""
) {
    companion object {
        fun empty() = PlayerState(
            trackId = 0L,
            isPlaying = false,
            position = 0L,
            playlistContext = "none",
            contextId = ""
        )
    }
}