package com.example.mozika.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mozika.domain.model.Track
import com.example.mozika.domain.model.Album
import com.example.mozika.domain.model.Artist
import com.example.mozika.domain.usecase.GetTracks
import com.example.mozika.domain.usecase.GetAlbums
import com.example.mozika.domain.usecase.GetArtists
import com.example.mozika.domain.usecase.RefreshTracks
import com.example.mozika.ui.player.PlayerStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryVM @Inject constructor(
    private val getTracks: GetTracks,
    private val getAlbums: GetAlbums,
    private val getArtists: GetArtists,
    private val refreshTracks: RefreshTracks,
    private val playerStateManager: PlayerStateManager
) : ViewModel() {

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanResult = MutableStateFlow<String?>(null)
    val scanResult: StateFlow<String?> = _scanResult.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _sortOrder = MutableStateFlow<SortOrder>(SortOrder.NONE)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    // EXPOSER les états du PlayerStateManager directement
    val currentlyPlayingTrackId: StateFlow<String?> = playerStateManager.currentTrackId
    val isPlaying: StateFlow<Boolean> = playerStateManager.isPlaying

    private val rawTracks: Flow<List<Track>> = getTracks()
    private val rawAlbums: Flow<List<Album>> = getAlbums()
    private val rawArtists: Flow<List<Artist>> = getArtists()

    val tracks: StateFlow<List<Track>> = combine(
        rawTracks,
        _query,
        _sortOrder
    ) { list, q, order ->
        list
            .filter { track ->
                q.isBlank() ||
                        track.title.contains(q, ignoreCase = true) ||
                        track.artist.contains(q, ignoreCase = true) ||
                        track.album.contains(q, ignoreCase = true)
            }
            .let {
                when (order) {
                    SortOrder.AZ -> it.sortedBy { t -> t.title.lowercase() }
                    SortOrder.ZA -> it.sortedByDescending { t -> t.title.lowercase() }
                    SortOrder.ARTIST -> it.sortedBy { t -> t.artist.lowercase() }
                    SortOrder.DATE -> it.sortedByDescending { t -> t.dateAdded }
                    SortOrder.DURATION -> it.sortedByDescending { t -> t.duration ?: 0 }
                    SortOrder.NONE -> it
                }
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val albums: StateFlow<List<Album>> = combine(
        rawAlbums,
        _query
    ) { list, q ->
        list.filter { album ->
            q.isBlank() ||
                    album.title.contains(q, ignoreCase = true) ||
                    album.artist.contains(q, ignoreCase = true)
        }.sortedBy { it.title.lowercase() }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val artists: StateFlow<List<Artist>> = combine(
        rawArtists,
        _query
    ) { list, q ->
        list.filter { artist ->
            q.isBlank() ||
                    artist.name.contains(q, ignoreCase = true)
        }.sortedBy { it.name.lowercase() }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun scanTracks() {
        viewModelScope.launch {
            _isScanning.value = true
            _scanResult.value = null

            try {
                refreshTracks()
                kotlinx.coroutines.delay(500)
                val trackCount = tracks.value.size
                _scanResult.value = "$trackCount pistes trouvées"
            } catch (e: Exception) {
                _scanResult.value = "Erreur: ${e.message}"
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun clearQuery() {
        _query.value = ""
    }

    fun sortByTitle() {
        _sortOrder.value = if (_sortOrder.value == SortOrder.AZ) SortOrder.ZA else SortOrder.AZ
    }

    fun sortByArtist() {
        _sortOrder.value = SortOrder.ARTIST
    }

    fun sortByDateAdded() {
        _sortOrder.value = SortOrder.DATE
    }

    fun sortByDuration() {
        _sortOrder.value = SortOrder.DURATION
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun clearSort() {
        _sortOrder.value = SortOrder.NONE
    }

    fun clearScanResult() {
        _scanResult.value = null
    }

    enum class SortOrder {
        NONE, AZ, ZA, ARTIST, DATE, DURATION
    }
}