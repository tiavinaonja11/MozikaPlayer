package com.example.mozika.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mozika.data.repo.PlaylistRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistVM @Inject constructor(
    private val playlistRepo: PlaylistRepo
) : ViewModel() {

    // Flow pour les playlists avec leur nombre de chansons
    val playlistsWithCount: StateFlow<List<PlaylistWithCount>> = playlistRepo.playlists()
        .flatMapLatest { playlists ->
            // Pour chaque playlist, obtenir son nombre de chansons
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
                    }.sortedByDescending { it.createdAt } // Trier par date de création
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
        val totalDuration = totalSongs * 180 // Estimation moyenne de 3 minutes par chanson
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

    // Créer une nouvelle playlist
    fun create(name: String) {
        if (name.isNotBlank()) {
            viewModelScope.launch {
                playlistRepo.create(name.trim())
            }
        }
    }

    // Supprimer une playlist
    fun delete(playlistId: Long) {
        viewModelScope.launch {
            playlistRepo.delete(playlistId)
        }
    }

    // Renommer une playlist
    fun rename(playlistId: Long, newName: String) {
        if (newName.isNotBlank()) {
            viewModelScope.launch {
                playlistRepo.rename(playlistId, newName.trim())
            }
        }
    }

    // Ajouter une chanson à une playlist
    fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            playlistRepo.addTrack(playlistId, trackId)
        }
    }

    // Supprimer une chanson d'une playlist
    fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            playlistRepo.removeTrack(playlistId, trackId)
        }
    }

    // Vérifier si une chanson est déjà dans une playlist
    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean {
        return playlistRepo.isTrackInPlaylist(playlistId, trackId)
    }

    // Rechercher des playlists
    fun searchPlaylists(query: String): Flow<List<PlaylistWithCount>> {
        return if (query.isBlank()) {
            playlistsWithCount
        } else {
            playlistRepo.searchPlaylists(query).flatMapLatest { playlists ->
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
    }

    // Obtenir le nombre de chansons d'une playlist spécifique
    suspend fun getSongCount(playlistId: Long): Int {
        return playlistRepo.getSongCount(playlistId)
    }

    // États pour gérer les opérations en cours
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Fonction pour nettoyer les messages d'erreur
    fun clearError() {
        _errorMessage.value = null
    }

    // Fonction wrapper avec gestion d'erreur
    private suspend fun <T> safeCall(block: suspend () -> T): T? {
        return try {
            _isLoading.value = true
            _errorMessage.value = null
            block()
        } catch (e: Exception) {
            _errorMessage.value = "Erreur: ${e.message}"
            null
        } finally {
            _isLoading.value = false
        }
    }
}

// Modèle de données pour les statistiques
data class PlaylistStats(
    val totalPlaylists: Int = 0,
    val totalSongs: Int = 0,
    val totalDuration: Int = 0
)