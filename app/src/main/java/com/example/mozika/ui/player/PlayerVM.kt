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
import com.example.mozika.ui.player.AudioWaveformAnalyzer
import com.example.mozika.data.repo.TrackRepo
import com.example.mozika.domain.model.Track as DomainTrack
import com.example.mozika.domain.usecase.GenWaveform
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(UnstableApi::class)
@HiltViewModel
class PlayerVM @Inject constructor(
    private val exoPlayer: ExoPlayer,
    private val genWaveform: GenWaveform,
    private val trackRepo: TrackRepo
) : ViewModel() {

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

    private var updateJob: Job? = null
    private var originalPlaylist: List<DomainTrack> = emptyList()

    private var analyzer: AudioWaveformAnalyzer? = null

    init {
        setupPlayerListeners()
        startPlayerUpdates()
        loadAllTracks()
    }

    private fun loadAllTracks() {
        viewModelScope.launch {
            trackRepo.tracks().collect { tracks ->
                playlist = tracks
                originalPlaylist = tracks
                playlistContext = PlaylistContext.AllTracks
                if (currentTrack == null && tracks.isNotEmpty()) {
                    load(tracks.first().id)
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
                        // tu peux gérer READY si tu veux (buffer fini)
                    }
                    Player.STATE_BUFFERING -> {
                        // buffering (afficher un loader si besoin)
                    }
                    Player.STATE_IDLE -> {
                        // player à l'arrêt sans média
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
            analyzer = AudioWaveformAnalyzer(sessionId)
        }
        analyzer?.start { amps ->
            waveform = downSampleAndSmooth(amps)
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

    fun load(trackId: Long) {
        viewModelScope.launch {
            try {
                val track = playlist.find { it.id == trackId }
                    ?: trackRepo.tracks().firstOrNull()?.find { it.id == trackId }
                    ?: throw Exception("Track not found")

                currentTrack = track

                val mediaItem = MediaItem.fromUri(track.data)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.play()

                // valeur initiale (fallback si Visualizer ne marche pas)
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

    fun loadAlbum(albumId: String) {
        viewModelScope.launch {
            try {
                val albumTracks = getAlbumTracks(albumId)
                if (albumTracks.isNotEmpty()) {
                    playlist = albumTracks
                    originalPlaylist = albumTracks
                    playlistContext = PlaylistContext.Album(albumId)
                    load(albumTracks.first().id)
                } else {
                    // Retour à la bibliothèque complète si album vide
                    trackRepo.tracks().collect { tracks ->
                        playlist = tracks
                        originalPlaylist = tracks
                        playlistContext = PlaylistContext.AllTracks
                        if (tracks.isNotEmpty()) {
                            load(tracks.first().id)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadArtist(artistId: String) {
        viewModelScope.launch {
            try {
                val artistTracks = getArtistTracks(artistId)
                if (artistTracks.isNotEmpty()) {
                    playlist = artistTracks
                    originalPlaylist = artistTracks
                    playlistContext = PlaylistContext.Artist(artistId)
                    load(artistTracks.first().id)
                } else {
                    trackRepo.tracks().collect { tracks ->
                        playlist = tracks
                        originalPlaylist = tracks
                        playlistContext = PlaylistContext.AllTracks
                        if (tracks.isNotEmpty()) {
                            load(tracks.first().id)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

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

    private suspend fun getAlbumTracks(albumTitle: String): List<DomainTrack> {
        return trackRepo.tracks().firstOrNull()?.filter { track ->
            track.album.equals(albumTitle, ignoreCase = true)
        } ?: emptyList()
    }

    private suspend fun getArtistTracks(artistName: String): List<DomainTrack> {
        return trackRepo.tracks().firstOrNull()?.filter { track ->
            track.artist.equals(artistName, ignoreCase = true)
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
            title = "Lecture on Covid",
            artist = "Crosstalk",
            album = "Unknown Album",
            duration = 180000,
            dateAdded = System.currentTimeMillis(),
            data = ""
        )
    }

    fun nextTrack() {
        if (playlist.isEmpty()) return
        currentTrack?.let { current ->
            val currentIndex = playlist.indexOfFirst { it.id == current.id }
            if (currentIndex != -1) {
                val nextIndex =
                    if (currentIndex < playlist.size - 1) currentIndex + 1 else 0
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
                val prevIndex =
                    if (currentIndex > 0) currentIndex - 1 else playlist.size - 1
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
                // Recharger la playlist après le rafraîchissement
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
        updateJob?.cancel()
        analyzer?.stop()
        exoPlayer.release()
    }

    enum class RepeatMode {
        OFF, ALL, ONE
    }

    // Classes scellées pour représenter le contexte de la playlist
    sealed class PlaylistContext {
        object None : PlaylistContext()
        object AllTracks : PlaylistContext()
        data class Album(val albumId: String) : PlaylistContext()
        data class Artist(val artistId: String) : PlaylistContext()
        data class Search(val query: String) : PlaylistContext()
    }
}