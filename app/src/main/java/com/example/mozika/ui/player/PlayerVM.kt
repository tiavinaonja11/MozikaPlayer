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
import com.example.mozika.data.datastore.PlayerState
import com.example.mozika.ui.player.AudioWaveformAnalyzer
import com.example.mozika.data.repo.TrackRepo
import com.example.mozika.domain.model.Track as DomainTrack
import com.example.mozika.domain.usecase.GenWaveform
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val playerPreferences: PlayerPreferences
) : ViewModel() {

    var position by mutableLongStateOf(0L)
        private set

    var duration by mutableLongStateOf(0L)
        private set

    var isPlaying by mutableStateOf(false)

    var waveform by mutableStateOf(intArrayOf())
        private set

    var currentTrack by mutableStateOf<DomainTrack?>(null)

    var shuffleMode by mutableStateOf(false)
        private set

    var repeatMode by mutableStateOf(RepeatMode.OFF)
        private set

    var playlist by mutableStateOf<List<DomainTrack>>(emptyList())
   

    var playlistContext by mutableStateOf<PlaylistContext>(PlaylistContext.None)
        private set

    private var updateJob: Job? = null
    private var originalPlaylist: List<DomainTrack> = emptyList()

    private var analyzer: AudioWaveformAnalyzer? = null

    init {
        setupPlayerListeners()
        startPlayerUpdates()
        restorePlayerState()
        startAutoSave()
    }

    private fun restorePlayerState() {
        viewModelScope.launch {
            // Récupérer l'état sauvegardé
            val savedState = playerPreferences.playerState.firstOrNull()
                ?: PlayerState.empty()

            if (savedState.trackId > 0) {
                try {
                    // Chercher la piste sauvegardée
                    val track = trackRepo.tracks().firstOrNull()?.find {
                        it.id == savedState.trackId
                    }

                    if (track != null) {
                        // Restaurer la piste mais en pause
                        currentTrack = track

                        // Préparer le player
                        val mediaItem = MediaItem.fromUri(track.data)
                        exoPlayer.setMediaItem(mediaItem)
                        exoPlayer.prepare()

                        // Restaurer la position
                        if (savedState.position > 0) {
                            exoPlayer.seekTo(savedState.position)
                        }

                        // Restaurer l'état de lecture
                        isPlaying = savedState.isPlaying
                        if (savedState.isPlaying) {
                            exoPlayer.play()
                        }

                        // Restaurer le contexte de playlist
                        playlistContext = when (savedState.playlistContext) {
                            "album" -> PlaylistContext.Album(savedState.contextId)
                            "artist" -> PlaylistContext.Artist(savedState.contextId)
                            "search" -> PlaylistContext.Search(savedState.contextId)
                            "all" -> PlaylistContext.AllTracks
                            else -> PlaylistContext.None
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Charger toutes les pistes pour la bibliothèque
            loadAllTracks()
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

                // Si aucun contexte n'est défini, utiliser AllTracks
                if (playlistContext == PlaylistContext.None) {
                    playlistContext = PlaylistContext.AllTracks
                }
            }
        }
    }

    private fun setupPlayerListeners() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> {
                        when (repeatMode) {
                            RepeatMode.ONE -> {
                                exoPlayer.seekTo(0)
                                exoPlayer.play()
                            }
                            else -> nextTrack()
                        }
                    }
                    Player.STATE_READY -> {
                        // État prêt
                    }
                    Player.STATE_BUFFERING -> {
                        // Buffering
                    }
                    Player.STATE_IDLE -> {
                        // Idle
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                this@PlayerVM.isPlaying = isPlaying
                if (isPlaying) {
                    startWaveformAnalyzer()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                error.printStackTrace()
            }
        })
    }

    private fun startPlayerUpdates() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            while (true) {
                updatePlayerState()
                delay(80)
            }
        }
    }

    private fun updatePlayerState() {
        position = exoPlayer.currentPosition
        duration = if (exoPlayer.duration > 0) exoPlayer.duration else 1L
        isPlaying = exoPlayer.isPlaying
    }

    private fun startWaveformAnalyzer() {
        val sessionId = exoPlayer.audioSessionId
        if (sessionId == C.AUDIO_SESSION_ID_UNSET) return

        if (analyzer == null) {
            try {
                analyzer = AudioWaveformAnalyzer(sessionId)
            } catch (e: Exception) {
                e.printStackTrace()
                return
            }
        }

        try {
            analyzer?.start { amps ->
                waveform = downSampleAndSmooth(amps)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun downSampleAndSmooth(src: IntArray): IntArray {
        val target = 200
        if (src.isEmpty()) return intArrayOf()

        val step = src.size.toFloat() / target
        val out = IntArray(target)
        var pos = 0f

        for (i in 0 until target) {
            val idx = pos.toInt().coerceIn(0, src.lastIndex)
            out[i] = src[idx]
            pos += step
        }

        for (i in 1 until out.size - 1) {
            out[i] = ((out[i - 1] + out[i] + out[i + 1]) / 3f).toInt()
        }

        return out
    }

    // Fonction principale pour charger une piste
    fun load(trackId: Long, autoPlay: Boolean = true) {
        viewModelScope.launch {
            try {
                // Chercher la piste dans la playlist actuelle d'abord
                var track = playlist.find { it.id == trackId }

                // Si pas trouvée, chercher dans le repo
                if (track == null) {
                    track = trackRepo.tracks().firstOrNull()?.find { it.id == trackId }
                }

                if (track == null) {
                    throw Exception("Piste non trouvée")
                }

                currentTrack = track

                val mediaItem = MediaItem.fromUri(track.data)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()

                if (autoPlay) {
                    exoPlayer.play()
                }

                // Générer waveform
                waveform = try {
                    genWaveform(trackId)
                } catch (e: Exception) {
                    createFallbackWaveform()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                currentTrack = createFallbackTrack(trackId)
                waveform = createFallbackWaveform()
            }
        }
    }

    fun loadAlbum(albumTitle: String) {
        viewModelScope.launch {
            try {
                val albumTracks = getAlbumTracks(albumTitle)
                if (albumTracks.isNotEmpty()) {
                    // Mettre à jour la playlist avec les pistes de l'album
                    playlist = albumTracks
                    originalPlaylist = albumTracks
                    playlistContext = PlaylistContext.Album(albumTitle)

                    // DEBUG
                    println("Album chargé: $albumTitle, ${albumTracks.size} pistes")
                    println("Contexte: ${playlistContext}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadArtist(artistName: String) {
        viewModelScope.launch {
            try {
                val artistTracks = getArtistTracks(artistName)
                if (artistTracks.isNotEmpty()) {
                    // Mettre à jour la playlist avec les pistes de l'artiste
                    playlist = artistTracks
                    originalPlaylist = artistTracks
                    playlistContext = PlaylistContext.Artist(artistName)

                    // DEBUG
                    println("Artiste chargé: $artistName, ${artistTracks.size} pistes")
                    println("Contexte: ${playlistContext}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Fonction pour charger un album à partir d'une piste
    fun playAlbumFromTrack(trackId: Long) {
        viewModelScope.launch {
            val track = playlist.find { it.id == trackId }
                ?: trackRepo.tracks().firstOrNull()?.find { it.id == trackId }

            track?.let { currentTrack ->
                val albumTracks = getAlbumTracks(currentTrack.album)
                if (albumTracks.isNotEmpty()) {
                    playlist = albumTracks
                    originalPlaylist = albumTracks
                    playlistContext = PlaylistContext.Album(currentTrack.album)
                    load(trackId)
                }
            }
        }
    }

    // Récupérer les pistes d'un album
    private suspend fun getAlbumTracks(albumTitle: String): List<DomainTrack> {
        return trackRepo.tracks().firstOrNull()?.filter { track ->
            track.album.equals(albumTitle, ignoreCase = true)
        } ?: emptyList()
    }

    // Récupérer les pistes d'un artiste
    private suspend fun getArtistTracks(artistName: String): List<DomainTrack> {
        return trackRepo.tracks().firstOrNull()?.filter { track ->
            track.artist.equals(artistName, ignoreCase = true)
        } ?: emptyList()
    }

    // Méthodes utilitaires
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

    // Méthode pour forcer la mise à jour de la piste actuelle
    fun updateCurrentTrack(track: DomainTrack) {
        currentTrack = track
    }

    // Flux pour observer les changements
    fun getCurrentTrackFlow() = MutableStateFlow(currentTrack).apply {
        value = currentTrack
    }.asStateFlow()

    fun getIsPlayingFlow() = MutableStateFlow(isPlaying).apply {
        value = isPlaying
    }.asStateFlow()

    // Contrôles de lecture
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
    }

    fun toggleRepeat() {
        repeatMode = when (repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }

        exoPlayer.repeatMode = when (repeatMode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
    }

    fun seekTo(ms: Long) {
        exoPlayer.seekTo(ms)
    }

    // Méthodes pour partager et favoris
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

    // Rafraîchir la bibliothèque
    fun refreshLibrary() {
        viewModelScope.launch {
            try {
                trackRepo.refreshTracks()
                trackRepo.tracks().collect { tracks ->
                    playlist = tracks
                    originalPlaylist = tracks
                    playlistContext = PlaylistContext.AllTracks
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Recherche
    fun searchTracks(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                trackRepo.tracks().collect { tracks ->
                    playlist = tracks
                    originalPlaylist = tracks
                    playlistContext = PlaylistContext.AllTracks
                }
            } else {
                trackRepo.searchTracks(query).collect { tracks ->
                    playlist = tracks
                    originalPlaylist = tracks
                    playlistContext = PlaylistContext.Search(query)
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
            }
        }
    }

    // Informations sur la playlist
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

    // Sauvegarde et nettoyage
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

    // Waveform par défaut
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

    // Piste par défaut
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

    // Enum pour le mode répétition
    enum class RepeatMode {
        OFF, ALL, ONE
    }

    // Contextes de playlist
    sealed class PlaylistContext {
        object None : PlaylistContext()
        object AllTracks : PlaylistContext()
        data class Album(val albumId: String) : PlaylistContext()
        data class Artist(val artistId: String) : PlaylistContext()
        data class Search(val query: String) : PlaylistContext()
    }
}