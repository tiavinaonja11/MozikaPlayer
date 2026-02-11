package com.example.mozika.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mozika.data.repo.PlaylistRepo
import com.example.mozika.domain.model.Playlist
import com.example.mozika.domain.model.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistVM @Inject constructor(
    private val playlistRepo: PlaylistRepo
) : ViewModel() {

    // Flow pour les playlists avec leur nombre de chansons
    val playlistsWithCount: StateFlow<List<PlaylistWithCount>> = playlistRepo.playlists()
        .flatMapLatest { playlists ->
            if (playlists.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    playlists.map { playlist ->
                        playlistRepo.tracksFor(playlist.id).map { tracks ->
                            playlist.id to tracks.size
                        }
                    }
                ) { countsArray ->
                    playlists.map { playlist ->
                        val songCount = countsArray.find { it.first == playlist.id }?.second ?: 0
                        PlaylistWithCount(
                            id = playlist.id,
                            name = playlist.name,
                            createdAt = playlist.createdAt,
                            songCount = songCount
                        )
                    }.sortedByDescending { it.createdAt }
                }
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // Flow pour les statistiques globales
    val stats: StateFlow<PlaylistStats> = playlistsWithCount.map { playlists ->
        val totalSongs = playlists.sumOf { it.songCount }
        val totalDuration = totalSongs * 180
        PlaylistStats(
            totalPlaylists = playlists.size,
            totalSongs = totalSongs,
            totalDuration = totalDuration
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PlaylistStats()
    )

    // ===== COLLECTIONS SPÉCIALES =====

    // Favoris
    val favoriteTracks: StateFlow<List<Track>> = playlistRepo.getFavoriteTracks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Top joués
    val topPlayedTracks: StateFlow<List<Pair<Track, Int>>> = playlistRepo.getTopPlayedTracks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Récemment joués
    val recentlyPlayedTracks: StateFlow<List<Track>> = playlistRepo.getRecentlyPlayedTracks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Compteurs pour l'affichage
    val favoriteCount: StateFlow<Int> = playlistRepo.getFavoriteCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val topPlayedCount: StateFlow<Int> = playlistRepo.getTopPlayedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentlyPlayedCount: StateFlow<Int> = playlistRepo.getRecentlyPlayedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /**
     * Créer une nouvelle playlist et retourner son ID
     */
    suspend fun create(name: String): Long {
        return if (name.isNotBlank()) {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                val id = playlistRepo.create(name.trim())
                _isLoading.value = false
                id
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Erreur lors de la création: ${e.message}"
                -1L
            }
        } else {
            _errorMessage.value = "Le nom ne peut pas être vide"
            -1L
        }
    }

    /**
     * Créer une playlist (version void pour compatibilité)
     */
    fun createPlaylist(name: String) {
        if (name.isNotBlank()) {
            viewModelScope.launch {
                try {
                    _isLoading.value = true
                    playlistRepo.create(name.trim())
                    _isLoading.value = false
                } catch (e: Exception) {
                    _isLoading.value = false
                    _errorMessage.value = "Erreur lors de la création: ${e.message}"
                }
            }
        }
    }

    /**
     * Supprimer une playlist
     */
    fun delete(playlistId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                playlistRepo.delete(playlistId)
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Erreur lors de la suppression: ${e.message}"
            }
        }
    }

    /**
     * Renommer une playlist
     */
    fun rename(playlistId: Long, newName: String) {
        if (newName.isNotBlank()) {
            viewModelScope.launch {
                try {
                    _isLoading.value = true
                    playlistRepo.rename(playlistId, newName.trim())
                    _isLoading.value = false
                } catch (e: Exception) {
                    _isLoading.value = false
                    _errorMessage.value = "Erreur lors du renommage: ${e.message}"
                }
            }
        }
    }

    /**
     * Ajouter une chanson à une playlist
     */
    fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            try {
                playlistRepo.addTrack(playlistId, trackId)
            } catch (e: Exception) {
                _errorMessage.value = "Erreur lors de l'ajout: ${e.message}"
            }
        }
    }

    /**
     * Supprimer une chanson d'une playlist
     */
    fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            try {
                playlistRepo.removeTrack(playlistId, trackId)
            } catch (e: Exception) {
                _errorMessage.value = "Erreur lors de la suppression: ${e.message}"
            }
        }
    }

    /**
     * Vérifier si une chanson est dans une playlist
     */
    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean {
        return try {
            playlistRepo.isTrackInPlaylist(playlistId, trackId)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Toggle favoris
     */
    fun toggleFavorite(trackId: Long, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val isNowFavorite = playlistRepo.toggleFavorite(trackId)
                onResult(isNowFavorite)
            } catch (e: Exception) {
                _errorMessage.value = "Erreur favoris: ${e.message}"
                onResult(false)
            }
        }
    }

    /**
     * Vérifier si un track est favori
     */
    suspend fun isFavorite(trackId: Long): Boolean {
        return try {
            playlistRepo.isFavorite(trackId)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Rechercher des playlists
     */
    fun searchPlaylists(query: String): StateFlow<List<PlaylistWithCount>> {
        return if (query.isBlank()) {
            playlistsWithCount
        } else {
            playlistRepo.searchPlaylists(query).flatMapLatest { playlists ->
                if (playlists.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    combine(
                        playlists.map { playlist ->
                            playlistRepo.tracksFor(playlist.id).map { tracks ->
                                playlist.id to tracks.size
                            }
                        }
                    ) { countsArray ->
                        playlists.map { playlist ->
                            val songCount = countsArray.find { it.first == playlist.id }?.second ?: 0
                            PlaylistWithCount(
                                id = playlist.id,
                                name = playlist.name,
                                createdAt = playlist.createdAt,
                                songCount = songCount
                            )
                        }.sortedByDescending { it.createdAt }
                    }
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        }
    }

    /**
     * Obtenir le nombre de chansons d'une playlist
     */
    suspend fun getSongCount(playlistId: Long): Int {
        return try {
            playlistRepo.getSongCount(playlistId)
        } catch (e: Exception) {
            0
        }
    }

    // États pour gérer les opérations
    private val _isLoading = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun clearError() {
        _errorMessage.value = null
    }
}

// Modèle de données pour les statistiques
data class PlaylistStats(
    val totalPlaylists: Int = 0,
    val totalSongs: Int = 0,
    val totalDuration: Int = 0
)


data class Playlist(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val songCount: Int
)