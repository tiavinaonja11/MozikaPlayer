package com.example.mozika.ui.player

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerStateManager @Inject constructor() {
    private val _currentTrackId = MutableStateFlow<String?>(null)
    val currentTrackId: StateFlow<String?> = _currentTrackId.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // Garder vos anciennes propriétés pour compatibilité
    private val _currentTrack = MutableStateFlow<com.example.mozika.domain.model.Track?>(null)
    private val _playlist = MutableStateFlow<List<com.example.mozika.domain.model.Track>>(emptyList())
    @RequiresApi(Build.VERSION_CODES.O)
    private val _playlistContext = MutableStateFlow<PlayerVM.PlaylistContext>(PlayerVM.PlaylistContext.None)

    val currentTrack: StateFlow<com.example.mozika.domain.model.Track?> = _currentTrack.asStateFlow()
    val playlist: StateFlow<List<com.example.mozika.domain.model.Track>> = _playlist.asStateFlow()
    @RequiresApi(Build.VERSION_CODES.O)
    val playlistContext: StateFlow<PlayerVM.PlaylistContext> = _playlistContext.asStateFlow()

    // NOUVELLE MÉTHODE pour mettre à jour l'état
    fun updateState(trackId: String?, playing: Boolean) {
        _currentTrackId.value = trackId
        _isPlaying.value = playing
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateFromPlayerVM(playerVM: PlayerVM) {
        _currentTrack.value = playerVM.currentTrack
        _isPlaying.value = playerVM.isPlaying
        _playlist.value = playerVM.playlist
        _playlistContext.value = playerVM.playlistContext
        // Mettre à jour aussi l'ID pour l'equalizer
        _currentTrackId.value = playerVM.currentTrack?.id?.toString()
    }
}

// Garder votre composable existant
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun rememberPlayerState(playerVM: PlayerVM = hiltViewModel()): PlayerStateManager {
    val playerState = remember { PlayerStateManager() }

    LaunchedEffect(playerVM) {
        snapshotFlow { playerVM.currentTrack }.collect { track ->
            playerState.updateFromPlayerVM(playerVM)
        }
        snapshotFlow { playerVM.isPlaying }.collect {
            playerState.updateFromPlayerVM(playerVM)
        }
        snapshotFlow { playerVM.playlist }.collect {
            playerState.updateFromPlayerVM(playerVM)
        }
        snapshotFlow { playerVM.playlistContext }.collect {
            playerState.updateFromPlayerVM(playerVM)
        }
    }

    return playerState
}