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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryVM @Inject constructor(
    private val getTracks: GetTracks,
    private val getAlbums: GetAlbums,
    private val getArtists: GetArtists,
    private val refreshTracks: RefreshTracks
) : ViewModel() {

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanResult = MutableStateFlow<String?>(null)
    val scanResult: StateFlow<String?> = _scanResult.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _sortOrder = MutableStateFlow<SortOrder>(SortOrder.NONE)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val rawTracks: Flow<List<Track>> = getTracks()
    private val rawAlbums: Flow<List<Album>> = getAlbums()
    private val rawArtists: Flow<List<Artist>> = getArtists()

    val tracks: StateFlow<List<Track>> = combine(
        rawTracks,
        _query,
        _sortOrder
    ) { list, q, order ->
        list
            .filter { q.isBlank() ||
                    it.title.contains(q, ignoreCase = true) ||
                    it.artist.contains(q, ignoreCase = true) }
            .let {
                when (order) {
                    SortOrder.AZ -> it.sortedBy { t -> t.title }
                    SortOrder.DATE -> it.sortedByDescending { t -> t.dateAdded }
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
        list.filter { q.isBlank() ||
                it.title.contains(q, ignoreCase = true) ||
                it.artist.contains(q, ignoreCase = true) }
            .sortedBy { it.title }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val artists: StateFlow<List<Artist>> = combine(
        rawArtists,
        _query
    ) { list, q ->
        list.filter { q.isBlank() ||
                it.name.contains(q, ignoreCase = true) }
            .sortedBy { it.name }
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
                val trackCount = tracks.value.size
                val albumCount = albums.value.size
                val artistCount = artists.value.size
                _scanResult.value = "Scan terminé ! $trackCount pistes, $albumCount albums, $artistCount artistes"
            } catch (e: Exception) {
                _scanResult.value = "Erreur: ${e.message}"
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun onQueryChange(new: String) { _query.value = new }
    fun sortByTitle() { _sortOrder.value = SortOrder.AZ }
    fun sortByDateAdded() { _sortOrder.value = SortOrder.DATE }
    fun clearScanResult() { _scanResult.value = null }

    enum class SortOrder { NONE, AZ, DATE }
}