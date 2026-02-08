package com.example.mozika.ui.player

import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.mozika.data.datastore.PlayerPreferences
import com.example.mozika.data.db.entity.Track
import com.example.mozika.data.datastore.PlayerState as SavedPlayerState
import com.example.mozika.ui.player.AudioWaveformAnalyzer
import com.example.mozika.data.repo.TrackRepo
import com.example.mozika.domain.model.Track as DomainTrack
import com.example.mozika.domain.usecase.GenWaveform
import com.example.mozika.domain.usecase.GetTracks
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(UnstableApi::class)
@HiltViewModel
class PlayerVM @Inject constructor(
    private val exoPlayer: ExoPlayer,
    private val genWaveform: GenWaveform,
    private val trackRepo: TrackRepo,
    private val playerPreferences: PlayerPreferences,
    private val getTracks: GetTracks
) : ViewModel() {

    // ============================================
    // PROPRIÉTÉS DE FILE D'ATTENTE (NOUVELLES)
    // ============================================

    /**
     * File d'attente complète (Track Entity)
     */
    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    /**
     * Index de la chanson en cours dans la file
     */
    private val _currentQueueIndex = MutableStateFlow(0)
    val currentQueueIndex: StateFlow<Int> = _currentQueueIndex.asStateFlow()

    /**
     * Chanson actuellement en lecture (StateFlow)
     */
    private val _currentTrackFlow = MutableStateFlow<Track?>(null)
    val currentTrackFlow: StateFlow<Track?> = _currentTrackFlow.asStateFlow()

    /**
     * État de lecture (StateFlow)
     */
    private val _isPlayingFlow = MutableStateFlow(false)
    val isPlayingFlow: StateFlow<Boolean> = _isPlayingFlow.asStateFlow()

    /**
     * Mode de répétition (StateFlow)
     */
    private val _repeatModeFlow = MutableStateFlow(RepeatMode.OFF)
    val repeatModeFlow: StateFlow<RepeatMode> = _repeatModeFlow.asStateFlow()

    /**
     * Mode aléatoire (StateFlow)
     */
    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private var originalQueue: List<Track> = emptyList()

    // ============================================
    // PROPRIÉTÉS EXISTANTES (CONSERVÉES)
    // ============================================

    var position by mutableLongStateOf(0L)
        private set

    var duration by mutableLongStateOf(0L)
        private set

    var isPlaying by mutableStateOf(false)
        private set

    var waveform by mutableStateOf(intArrayOf())
        private set

    var currentTrack by mutableStateOf<DomainTrack?>(null)
        private set

    var shuffleMode by mutableStateOf(false)
        private set

    var repeatMode by mutableStateOf(RepeatMode.OFF)
        private set

    var playlist by mutableStateOf<List<DomainTrack>>(emptyList())
        private set

    var playlistContext by mutableStateOf<PlaylistContext>(PlaylistContext.None)
        private set

    // StateFlow pour observer les changements d'état
    data class PlayerState(
        val currentTrack: DomainTrack?,
        val isPlaying: Boolean,
        val playlist: List<DomainTrack>,
        val playlistContext: PlaylistContext
    )

    private val _playerState = MutableStateFlow(
        PlayerState(
            currentTrack = null,
            isPlaying = false,
            playlist = emptyList(),
            playlistContext = PlaylistContext.None
        )
    )
    val playerState = _playerState.asStateFlow()

    // Flow pour forcer la mise à jour du UI
    private val _forceUpdate = MutableStateFlow(0L)

    private var updateJob: Job? = null
    private var originalPlaylist: List<DomainTrack> = emptyList()

    private var analyzer: AudioWaveformAnalyzer? = null

    init {
        setupPlayerListeners()
        startPlayerUpdates()
        restorePlayerState()
        startAutoSave()
        loadAllTracks()
    }

    // ============================================
    // MÉTHODES DE FILE D'ATTENTE (NOUVELLES)
    // ============================================

    /**
     * Ajoute une chanson à la fin de la file d'attente
     */
    fun addToQueue(track: DomainTrack) {
        viewModelScope.launch {
            // Convertir DomainTrack en Track Entity
            val entityTrack = Track(
                id = track.id,
                title = track.title,
                artist = track.artist,
                album = track.album,
                duration = track.duration,
                dateAdded = track.dateAdded,
                path = track.data
            )

            val currentQueue = _queue.value.toMutableList()
            if (!currentQueue.any { it.id == entityTrack.id }) {
                currentQueue.add(entityTrack)
                _queue.value = currentQueue
            }
        }
    }

    /**
     * Ajoute une chanson pour qu'elle soit lue juste après la chanson actuelle
     */
    fun playNext(track: DomainTrack) {
        viewModelScope.launch {
            // Convertir DomainTrack en Track Entity
            val entityTrack = Track(
                id = track.id,
                title = track.title,
                artist = track.artist,
                album = track.album,
                duration = track.duration,
                dateAdded = track.dateAdded,
                path = track.data
            )

            val currentQueue = _queue.value.toMutableList()

            // Retirer si déjà présent
            currentQueue.removeAll { it.id == entityTrack.id }

            // Insérer après la chanson actuelle
            val insertIndex = _currentQueueIndex.value + 1
            if (insertIndex <= currentQueue.size) {
                currentQueue.add(insertIndex, entityTrack)
            } else {
                currentQueue.add(entityTrack)
            }

            _queue.value = currentQueue
        }
    }

    /**
     * Charge un album complet dans la file d'attente
     */
    fun loadAlbum(albumTitle: String) {
        viewModelScope.launch {
            getTracks().firstOrNull()?.let { allTracks ->
                val albumTracks = allTracks.filter { it.album == albumTitle }
                    .sortedBy { it.title }

                if (albumTracks.isNotEmpty()) {
                    // Convertir en Track Entity
                    val entityTracks = albumTracks.map { track ->
                        Track(
                            id = track.id,
                            title = track.title,
                            artist = track.artist,
                            album = track.album,
                            duration = track.duration,
                            dateAdded = track.dateAdded,
                            path = track.data
                        )
                    }

                    _queue.value = entityTracks
                    _currentQueueIndex.value = 0

                    // Charger aussi dans la playlist normale
                    playlist = albumTracks
                    playlistContext = PlaylistContext.Album(albumTitle) // albumTitle est déjà un String
                    updatePlayerStateFlow()
                }
            }
        }
    }

    /**
     * Charge une playlist dans la file d'attente
     */
    fun loadPlaylist(tracks: List<DomainTrack>) {
        if (tracks.isNotEmpty()) {
            viewModelScope.launch {
                // Convertir en Track Entity
                val entityTracks = tracks.map { track ->
                    Track(
                        id = track.id,
                        title = track.title,
                        artist = track.artist,
                        album = track.album,
                        duration = track.duration,
                        dateAdded = track.dateAdded,
                        path = track.data
                    )
                }

                _queue.value = entityTracks
                _currentQueueIndex.value = 0

                // Charger aussi dans la playlist normale
                playlist = tracks
                updatePlayerStateFlow()
            }
        }
    }

    /**
     * Supprime une chanson de la file d'attente
     */
    fun removeFromQueue(index: Int) {
        val currentQueue = _queue.value.toMutableList()
        if (index in currentQueue.indices) {
            currentQueue.removeAt(index)
            _queue.value = currentQueue

            // Ajuster l'index actuel si nécessaire
            if (index < _currentQueueIndex.value) {
                _currentQueueIndex.value = (_currentQueueIndex.value - 1).coerceAtLeast(0)
            } else if (index == _currentQueueIndex.value && currentQueue.isNotEmpty()) {
                val newIndex = index.coerceAtMost(currentQueue.size - 1)
                _currentQueueIndex.value = newIndex
                _currentTrackFlow.value = currentQueue.getOrNull(newIndex)
            }
        }
    }

    /**
     * Vide la file d'attente
     */
    fun clearQueue() {
        _queue.value = emptyList()
        _currentQueueIndex.value = 0
        _currentTrackFlow.value = null
        _isPlayingFlow.value = false
    }

    /**
     * Passe à la chanson suivante dans la file
     */
    fun playNextInQueue() {
        val queue = _queue.value
        val currentIndex = _currentQueueIndex.value

        when (_repeatModeFlow.value) {
            RepeatMode.ONE -> {
                // Recommencer la même chanson
                exoPlayer.seekTo(0)
                exoPlayer.play()
            }
            RepeatMode.ALL -> {
                val nextIndex = if (currentIndex + 1 < queue.size) {
                    currentIndex + 1
                } else {
                    0 // Recommencer au début
                }
                _currentQueueIndex.value = nextIndex
                queue.getOrNull(nextIndex)?.let { track ->
                    load(track.id, autoPlay = true)
                }
            }
            RepeatMode.OFF -> {
                if (currentIndex + 1 < queue.size) {
                    _currentQueueIndex.value = currentIndex + 1
                    queue[currentIndex + 1].let { track ->
                        load(track.id, autoPlay = true)
                    }
                } else {
                    // Fin de la file
                    exoPlayer.pause()
                    _isPlayingFlow.value = false
                }
            }
        }
    }

    /**
     * Revient à la chanson précédente dans la file
     */
    fun playPreviousInQueue() {
        val currentIndex = _currentQueueIndex.value
        val queue = _queue.value

        if (currentIndex > 0) {
            _currentQueueIndex.value = currentIndex - 1
            queue[currentIndex - 1].let { track ->
                load(track.id, autoPlay = true)
            }
        } else if (_repeatModeFlow.value == RepeatMode.ALL && queue.isNotEmpty()) {
            val lastIndex = queue.size - 1
            _currentQueueIndex.value = lastIndex
            queue[lastIndex].let { track ->
                load(track.id, autoPlay = true)
            }
        }
    }

    /**
     * Réorganise la file d'attente
     */
    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        val currentQueue = _queue.value.toMutableList()

        if (fromIndex in currentQueue.indices && toIndex in currentQueue.indices) {
            val item = currentQueue.removeAt(fromIndex)
            currentQueue.add(toIndex, item)
            _queue.value = currentQueue

            // Ajuster l'index actuel
            when {
                fromIndex == _currentQueueIndex.value -> {
                    _currentQueueIndex.value = toIndex
                }
                fromIndex < _currentQueueIndex.value && toIndex >= _currentQueueIndex.value -> {
                    _currentQueueIndex.value = _currentQueueIndex.value - 1
                }
                fromIndex > _currentQueueIndex.value && toIndex <= _currentQueueIndex.value -> {
                    _currentQueueIndex.value = _currentQueueIndex.value + 1
                }
            }
        }
    }

    /**
     * Active/désactive le mode aléatoire pour la file d'attente
     */
    fun toggleShuffleQueue() {
        val currentlyShuffling = _isShuffleEnabled.value

        if (!currentlyShuffling) {
            // Activer le shuffle
            originalQueue = _queue.value
            val currentTrack = _queue.value.getOrNull(_currentQueueIndex.value)

            val shuffled = _queue.value.toMutableList()
            shuffled.shuffle()

            // S'assurer que la chanson actuelle reste en première position
            currentTrack?.let { track ->
                shuffled.removeAll { it.id == track.id }
                shuffled.add(0, track)
            }

            _queue.value = shuffled
            _currentQueueIndex.value = 0
        } else {
            // Désactiver le shuffle
            _queue.value = originalQueue
            val currentTrack = _currentTrackFlow.value
            currentTrack?.let { track ->
                val newIndex = originalQueue.indexOfFirst { it.id == track.id }
                if (newIndex != -1) {
                    _currentQueueIndex.value = newIndex
                }
            }
        }

        _isShuffleEnabled.value = !currentlyShuffling
    }

    /**
     * Change le mode de répétition (OFF -> ALL -> ONE -> OFF)
     */
    fun cycleRepeatMode() {
        _repeatModeFlow.value = when (_repeatModeFlow.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }

        // Mettre à jour aussi l'ancien repeatMode pour compatibilité
        repeatMode = _repeatModeFlow.value

        exoPlayer.repeatMode = when (_repeatModeFlow.value) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
    }

    /**
     * Aller à un index spécifique de la file
     */
    fun playAtIndex(index: Int) {
        if (index in _queue.value.indices) {
            _currentQueueIndex.value = index
            _queue.value[index].let { track ->
                load(track.id, autoPlay = true)
            }
        }
    }

    // ============================================
    // MÉTHODES UTILITAIRES FILE D'ATTENTE
    // ============================================

    /**
     * Obtient la chanson suivante sans la jouer
     */
    fun getNextTrack(): Track? {
        val queue = _queue.value
        val currentIndex = _currentQueueIndex.value

        return when {
            _repeatModeFlow.value == RepeatMode.ONE -> _currentTrackFlow.value
            currentIndex + 1 < queue.size -> queue[currentIndex + 1]
            _repeatModeFlow.value == RepeatMode.ALL -> queue.firstOrNull()
            else -> null
        }
    }

    /**
     * Obtient la chanson précédente sans la jouer
     */
    fun getPreviousTrack(): Track? {
        val currentIndex = _currentQueueIndex.value
        val queue = _queue.value
        return when {
            currentIndex > 0 -> queue[currentIndex - 1]
            _repeatModeFlow.value == RepeatMode.ALL -> queue.lastOrNull()
            else -> null
        }
    }

    /**
     * Vérifie s'il y a une chanson suivante
     */
    fun hasNext(): Boolean {
        return when {
            _repeatModeFlow.value != RepeatMode.OFF -> true
            else -> _currentQueueIndex.value + 1 < _queue.value.size
        }
    }

    /**
     * Vérifie s'il y a une chanson précédente
     */
    fun hasPrevious(): Boolean {
        return when {
            _repeatModeFlow.value == RepeatMode.ALL -> true
            else -> _currentQueueIndex.value > 0
        }
    }

    // ============================================
    // MÉTHODES EXISTANTES (CONSERVÉES)
    // ============================================

    private fun restorePlayerState() {
        viewModelScope.launch {
            val savedState = playerPreferences.playerState.firstOrNull()
                ?: SavedPlayerState.empty()

            if (savedState.trackId > 0) {
                try {
                    val allTracks = trackRepo.tracks().firstOrNull() ?: emptyList()
                    val track = allTracks.find { it.id == savedState.trackId }

                    if (track != null) {
                        currentTrack = track

                        val mediaItem = MediaItem.fromUri(track.data)
                        exoPlayer.setMediaItem(mediaItem)
                        exoPlayer.prepare()

                        if (savedState.position > 0) {
                            exoPlayer.seekTo(savedState.position)
                        }

                        isPlaying = savedState.isPlaying
                        if (savedState.isPlaying) {
                            exoPlayer.play()
                        }

                        playlistContext = when (savedState.playlistContext) {
                            "album" -> PlaylistContext.Album(savedState.contextId)
                            "artist" -> PlaylistContext.Artist(savedState.contextId)
                            "search" -> PlaylistContext.Search(savedState.contextId)
                            "all" -> PlaylistContext.AllTracks
                            else -> PlaylistContext.None
                        }

                        updatePlayerStateFlow()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun savePlayerState() {
        viewModelScope.launch {
            currentTrack?.let { track ->
                val context = playlistContext
                val (contextType, contextId) = when (context) {
                    is PlaylistContext.Album -> Pair("album", context.albumId)
                    is PlaylistContext.Artist -> Pair("artist", context.artistId)
                    is PlaylistContext.Search -> Pair("search", context.query)
                    is PlaylistContext.AllTracks -> Pair("all", "")
                    is PlaylistContext.None -> Pair("none", "")
                }

                playerPreferences.savePlayerState(
                    trackId = track.id,
                    isPlaying = isPlaying,
                    position = exoPlayer.currentPosition,
                    playlistContext = contextType,
                    contextId = contextId
                )
            }
        }
    }

    fun isPlaylistValidForContext(): Boolean {
        return when (playlistContext) {
            is PlaylistContext.Album, is PlaylistContext.Artist -> playlist.size > 1
            else -> playlist.size > 1
        }
    }

    private fun startAutoSave() {
        viewModelScope.launch {
            while (true) {
                delay(5000)
                savePlayerState()
            }
        }
    }

    private fun loadAllTracks() {
        viewModelScope.launch {
            trackRepo.tracks().collect { tracks ->
                playlist = tracks
                originalPlaylist = tracks

                if (playlistContext == PlaylistContext.None) {
                    playlistContext = PlaylistContext.AllTracks
                }

                updatePlayerStateFlow()
            }
        }
    }

    private fun setupPlayerListeners() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_ENDED -> {
                        // Chanson terminée, passer à la suivante
                        if (hasNext()) {
                            playNextInQueue()
                        } else {
                            isPlaying = false
                            _isPlayingFlow.value = false
                        }
                    }
                    Player.STATE_READY -> {
                        duration = exoPlayer.duration.coerceAtLeast(0)
                    }
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                _isPlayingFlow.value = playing
                updatePlayerStateFlow()
            }

            override fun onPlayerError(error: PlaybackException) {
                error.printStackTrace()
            }
        })
    }

    private fun startPlayerUpdates() {
        updateJob = viewModelScope.launch {
            while (true) {
                if (exoPlayer.isPlaying) {
                    position = exoPlayer.currentPosition.coerceAtLeast(0)
                    duration = exoPlayer.duration.coerceAtLeast(0)
                }
                delay(100)
            }
        }
    }

    private fun updatePlayerStateFlow() {
        _playerState.value = PlayerState(
            currentTrack = currentTrack,
            isPlaying = isPlaying,
            playlist = playlist,
            playlistContext = playlistContext
        )
        _forceUpdate.value = System.currentTimeMillis()
    }

    fun load(trackId: Long, autoPlay: Boolean = false) {
        viewModelScope.launch {
            try {
                val allTracks = trackRepo.tracks().firstOrNull() ?: emptyList()
                val track = allTracks.find { it.id == trackId }

                track?.let {
                    currentTrack = it

                    // Mettre à jour aussi le flow
                    _currentTrackFlow.value = Track(
                        id = it.id,
                        title = it.title,
                        artist = it.artist,
                        album = it.album,
                        duration = it.duration,
                        dateAdded = it.dateAdded,
                        path = it.data
                    )

                    val mediaItem = MediaItem.fromUri(it.data)
                    exoPlayer.setMediaItem(mediaItem)
                    exoPlayer.prepare()

                    if (autoPlay) {
                        exoPlayer.play()
                        isPlaying = true
                        _isPlayingFlow.value = true
                    }

                    viewModelScope.launch {
                        waveform = try {
                            genWaveform(trackId)
                        } catch (e: Exception) {
                            createFallbackWaveform()
                        }
                    }

                    updatePlayerStateFlow()
                    savePlayerState()
                } ?: run {
                    currentTrack = createFallbackTrack(trackId)
                    waveform = createFallbackWaveform()
                    updatePlayerStateFlow()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                currentTrack = createFallbackTrack(trackId)
                waveform = createFallbackWaveform()
                updatePlayerStateFlow()
            }
        }
    }

    fun loadWithPlaylist(trackId: Long, newPlaylist: List<DomainTrack>, context: PlaylistContext, autoPlay: Boolean = false) {
        playlist = newPlaylist
        originalPlaylist = newPlaylist
        playlistContext = context
        updatePlayerStateFlow()
        load(trackId, autoPlay)
    }

    fun loadAlbumTracks(albumId: String): List<DomainTrack> {
        return playlist.filter {
            it.album.equals(albumId, ignoreCase = true)
        }
    }

    fun loadArtistTracks(artistName: String): List<DomainTrack> {
        return playlist.filter {
            it.artist.equals(artistName, ignoreCase = true)
        } ?: emptyList()
    }

    fun getAlbumFromCurrentTrack(): String? {
        return currentTrack?.album
    }

    fun getArtistFromCurrentTrack(): String? {
        return currentTrack?.artist
    }

    fun isCurrentTrackInAlbum(): Boolean {
        return playlistContext is PlaylistContext.Album
    }

    fun isCurrentTrackInArtist(): Boolean {
        return playlistContext is PlaylistContext.Artist
    }

    fun updateCurrentTrack(track: DomainTrack) {
        currentTrack = track
        updatePlayerStateFlow()
    }

    fun nextTrack() {
        if (playlist.isEmpty()) return
        currentTrack?.let { current ->
            val currentIndex = playlist.indexOfFirst { it.id == current.id }
            if (currentIndex != -1) {
                val nextIndex = if (currentIndex < playlist.size - 1) currentIndex + 1 else 0
                load(playlist[nextIndex].id)
            } else {
                load(playlist[0].id)
            }
        } ?: run {
            load(playlist[0].id)
        }
    }

    fun previousTrack() {
        if (playlist.isEmpty()) return
        currentTrack?.let { current ->
            val currentIndex = playlist.indexOfFirst { it.id == current.id }
            if (currentIndex != -1) {
                val prevIndex = if (currentIndex > 0) currentIndex - 1 else playlist.size - 1
                load(playlist[prevIndex].id)
            }
        }
    }

    fun playPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
        isPlaying = exoPlayer.isPlaying
        _isPlayingFlow.value = exoPlayer.isPlaying
        updatePlayerStateFlow()
    }

    fun toggleShuffle() {
        shuffleMode = !shuffleMode
        if (shuffleMode) {
            val currentTrackId = currentTrack?.id
            playlist = playlist.shuffled()
            currentTrackId?.let { id ->
                playlist.find { it.id == id }?.let { track ->
                    currentTrack = track
                }
            }
        } else {
            playlist = originalPlaylist
            currentTrack?.let { track ->
                originalPlaylist.find { it.id == track.id }?.let { originalTrack ->
                    currentTrack = originalTrack
                }
            }
        }
        updatePlayerStateFlow()
    }

    fun toggleRepeat() {
        repeatMode = when (repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }

        // Synchroniser avec le nouveau flow
        _repeatModeFlow.value = repeatMode

        exoPlayer.repeatMode = when (repeatMode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
    }

    fun seekTo(ms: Long) {
        exoPlayer.seekTo(ms)
    }

    fun shareTrack() {
        viewModelScope.launch {
            currentTrack?.let { track ->
                println("Partage de la piste: ${track.title} - ${track.artist}")
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            currentTrack?.let { track ->
                println("Favori basculé pour: ${track.title}")
            }
        }
    }

    fun addToPlaylist() {
        viewModelScope.launch {
            currentTrack?.let { track ->
                println("Ajout à la playlist: ${track.title}")
            }
        }
    }

    fun refreshLibrary() {
        viewModelScope.launch {
            try {
                trackRepo.refreshTracks()
                trackRepo.tracks().collect { tracks ->
                    playlist = tracks
                    originalPlaylist = tracks
                    playlistContext = PlaylistContext.AllTracks
                    updatePlayerStateFlow()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun searchTracks(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                trackRepo.tracks().collect { tracks ->
                    playlist = tracks
                    originalPlaylist = tracks
                    playlistContext = PlaylistContext.AllTracks
                    updatePlayerStateFlow()
                }
            } else {
                trackRepo.searchTracks(query).collect { tracks ->
                    playlist = tracks
                    originalPlaylist = tracks
                    playlistContext = PlaylistContext.Search(query)
                    updatePlayerStateFlow()
                }
            }
        }
    }

    fun clearSearch() {
        viewModelScope.launch {
            trackRepo.tracks().collect { tracks ->
                playlist = tracks
                originalPlaylist = tracks
                playlistContext = PlaylistContext.AllTracks
                updatePlayerStateFlow()
            }
        }
    }

    fun getCurrentPlaylistInfo(): String {
        return when (val context = playlistContext) {
            is PlaylistContext.Album -> "Album: ${context.albumId}"
            is PlaylistContext.Artist -> "Artiste: ${context.artistId}"
            is PlaylistContext.Search -> "Recherche: ${context.query}"
            is PlaylistContext.AllTracks -> "Toutes les pistes"
            is PlaylistContext.None -> "Aucune playlist"
        }
    }

    fun getCurrentPlaylistSize(): Int {
        return playlist.size
    }

    override fun onCleared() {
        super.onCleared()
        savePlayerState()
        updateJob?.cancel()
        analyzer?.stop()
    }

    fun forceSaveState() {
        viewModelScope.launch {
            savePlayerState()
        }
    }

    private fun createFallbackWaveform(): IntArray {
        val barCount = 200
        return IntArray(barCount) { index ->
            val position = index % 40
            when {
                position < 10 -> (position + 1) * 10
                position < 20 -> (20 - position) * 10
                position < 30 -> (position - 20) * 10
                else -> (40 - position) * 10
            }
        }
    }

    private fun createFallbackTrack(trackId: Long): DomainTrack {
        return DomainTrack(
            id = trackId,
            title = "Lecture en cours",
            artist = "Artiste inconnu",
            album = "Album inconnu",
            duration = 180000,
            dateAdded = System.currentTimeMillis(),
            data = ""
        )
    }

    enum class RepeatMode {
        OFF, ALL, ONE
    }

    sealed class PlaylistContext {
        object None : PlaylistContext()
        object AllTracks : PlaylistContext()
        data class Album(val albumId: String) : PlaylistContext()
        data class Artist(val artistId: String) : PlaylistContext()
        data class Search(val query: String) : PlaylistContext()
    }

    fun getForceUpdateFlow() = _forceUpdate.asStateFlow()
}