package com.example.mozika.ui.playlist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mozika.data.repo.PlaylistRepo
import com.example.mozika.domain.model.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SpecialPlaylistUiState(
    val isLoading: Boolean = true,
    val tracks: List<Track> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class SpecialPlaylistViewModel @Inject constructor(
    private val playlistRepo: PlaylistRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpecialPlaylistUiState())
    val uiState: StateFlow<SpecialPlaylistUiState> = _uiState

    fun loadSpecialPlaylist(type: String) {
        viewModelScope.launch {
            val flow = when (type) {
                "favorites" -> playlistRepo.getFavoriteTracks()
                "recently_played" -> playlistRepo.getRecentlyPlayedTracks()
                "most_played" -> playlistRepo.getTopPlayedTracks()
                    .map { pairs -> pairs.map { it.first } }
                else -> kotlinx.coroutines.flow.flowOf(emptyList())
            }

            flow
                .onStart { _uiState.value = SpecialPlaylistUiState(isLoading = true) }
                .catch { e ->
                    _uiState.value = SpecialPlaylistUiState(
                        isLoading = false,
                        error = e.message ?: "Erreur inconnue"
                    )
                }
                .collect { tracks ->
                    _uiState.value = SpecialPlaylistUiState(
                        isLoading = false,
                        tracks = tracks
                    )
                }
        }
    }
}