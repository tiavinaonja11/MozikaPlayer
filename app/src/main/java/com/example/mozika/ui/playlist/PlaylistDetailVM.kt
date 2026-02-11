package com.example.mozika.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mozika.data.repo.PlaylistRepo
import com.example.mozika.domain.model.Playlist
import com.example.mozika.domain.model.Track
import com.example.mozika.domain.usecase.GetTracks
import com.example.mozika.ui.player.PlayerVM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailVM @Inject constructor(
    private val playlistRepo: PlaylistRepo,
    private val getTracks: GetTracks
) : ViewModel() {

    private val _playlistId = MutableStateFlow<Long?>(null)
    private val _playlist = MutableStateFlow<Playlist?>(null)
    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    private val _allTracks = MutableStateFlow<List<Track>>(emptyList())

    val playlist: StateFlow<Playlist?> = _playlist.asStateFlow()
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()
    val allTracks: StateFlow<List<Track>> = _allTracks.asStateFlow()

    init {
        // Charger toutes les pistes pour le dialogue d'ajout
        viewModelScope.launch {
            getTracks().collect { tracks ->
                _allTracks.value = tracks
            }
        }

        // Observer les pistes de la playlist quand l'ID change
        viewModelScope.launch {
            _playlistId.collect { playlistId ->
                playlistId?.let { id ->
                    playlistRepo.tracksFor(id).collect { tracks ->
                        _tracks.value = tracks
                    }
                }
            }
        }
    }

    fun loadPlaylist(playlistId: Long) {
        _playlistId.value = playlistId
        viewModelScope.launch {
            playlistRepo.playlists().collect { playlists ->
                val foundPlaylist = playlists.find { it.id == playlistId }
                _playlist.value = foundPlaylist
            }
        }
    }

    fun addTrack(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            playlistRepo.addTrack(playlistId, trackId)
        }
    }

    fun removeTrack(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            playlistRepo.removeTrack(playlistId, trackId)
        }
    }

    fun clearPlaylist() {
        viewModelScope.launch {
            val playlistId = _playlistId.value ?: return@launch
            _tracks.value.forEach { track ->
                playlistRepo.removeTrack(playlistId, track.id)
            }
        }
    }

    fun deletePlaylist() {
        viewModelScope.launch {
            _playlist.value?.let { playlist ->
                val entity = com.example.mozika.data.db.entity.Playlist(
                    id = playlist.id,
                    name = playlist.name,
                    createdAt = playlist.createdAt
                )
                playlistRepo.delete(entity)
            }
        }
    }

    /**
     * Jouer toute la playlist à partir du début
     */
    fun playAll(playerVM: PlayerVM) {
        viewModelScope.launch {
            val tracksList = _tracks.value
            if (tracksList.isNotEmpty()) {
                playerVM.loadPlaylist(tracksList)
                playerVM.load(tracksList.first().id, autoPlay = true)
            }
        }
    }

    /**
     * Jouer une chanson spécifique de la playlist
     */
    fun playTrack(playerVM: PlayerVM, trackId: Long) {
        viewModelScope.launch {
            playerVM.loadPlaylist(_tracks.value)
            playerVM.load(trackId, autoPlay = true)
        }
    }

    /**
     * Jouer la playlist à partir d'une position spécifique
     */
    fun playFromPosition(playerVM: PlayerVM, position: Int) {
        viewModelScope.launch {
            val tracksList = _tracks.value
            if (position >= 0 && position < tracksList.size) {
                playerVM.loadPlaylist(tracksList)
                playerVM.load(tracksList[position].id, autoPlay = true)
            }
        }
    }
}