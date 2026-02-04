package com.example.mozika.ui.player

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*

// Classe pour gérer l'état global du lecteur
class PlayerStateManager {
    // Les StateFlow pour observer les changements
    private val _currentTrack = MutableStateFlow<com.example.mozika.domain.model.Track?>(null)
    private val _isPlaying = MutableStateFlow(false)
    private val _playlist = MutableStateFlow<List<com.example.mozika.domain.model.Track>>(emptyList())
    private val _playlistContext = MutableStateFlow<PlayerVM.PlaylistContext>(PlayerVM.PlaylistContext.None)

    val currentTrack: StateFlow<com.example.mozika.domain.model.Track?> = _currentTrack.asStateFlow()
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    val playlist: StateFlow<List<com.example.mozika.domain.model.Track>> = _playlist.asStateFlow()
    val playlistContext: StateFlow<PlayerVM.PlaylistContext> = _playlistContext.asStateFlow()

    fun updateFromPlayerVM(playerVM: PlayerVM) {
        _currentTrack.value = playerVM.currentTrack
        _isPlaying.value = playerVM.isPlaying
        _playlist.value = playerVM.playlist
        _playlistContext.value = playerVM.playlistContext
    }
}

// Composable pour obtenir l'état du player
@Composable
fun rememberPlayerState(playerVM: PlayerVM = hiltViewModel()): PlayerStateManager {
    val playerState = remember { PlayerStateManager() }

    // Observer les changements du PlayerVM
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