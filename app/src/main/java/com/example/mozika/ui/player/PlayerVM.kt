package com.example.mozika.ui.player

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.mozika.data.repo.TrackRepo
import com.example.mozika.domain.model.Track as DomainTrack
import com.example.mozika.domain.usecase.GenWaveform
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerVM @Inject constructor(
    private val exoPlayer: ExoPlayer,
    private val genWaveform: GenWaveform,
    private val trackRepo: TrackRepo
) : ViewModel() {

    var position by mutableStateOf(0L)
        private set

    var duration by mutableStateOf(0L)
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

    private var updateJob: Job? = null
    private var originalPlaylist = emptyList<DomainTrack>()

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

                // Si aucune piste n'est en cours de lecture et qu'il y a des pistes, charger la première
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
                delay(200)
            }
        }
    }

    private fun updatePlayerState() {
        position = exoPlayer.currentPosition
        duration = if (exoPlayer.duration > 0) exoPlayer.duration else 1L
        isPlaying = exoPlayer.isPlaying
    }

    fun load(trackId: Long) {
        viewModelScope.launch {
            try {
                // Chercher la piste dans la playlist existante
                val track = playlist.find { it.id == trackId }
                    ?: // Si pas trouvé, chercher dans le repository
                    trackRepo.tracks().firstOrNull()?.find { it.id == trackId }
                    ?: throw Exception("Track not found")

                currentTrack = track

                // Charger dans ExoPlayer
                val mediaItem = MediaItem.fromUri(track.data)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.play()

                // Générer le waveform
                try {
                    waveform = genWaveform(trackId)
                } catch (e: Exception) {
                    // Fallback: waveform factice basé sur la durée
                    waveform = createFallbackWaveform(track.duration)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback: utiliser une piste factice
                currentTrack = createFallbackTrack(trackId)
                waveform = createFallbackWaveform(180000)
            }
        }
    }

    private fun createFallbackWaveform(duration: Int): IntArray {
        val durationInSeconds = duration / 1000
        val barCount = 200 // Nombre de barres dans le waveform
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
            // Sauvegarder la piste courante avant de mélanger
            val currentTrackId = currentTrack?.id

            // Mélanger la playlist
            playlist = playlist.shuffled()

            // Recharger la piste courante à sa nouvelle position
            currentTrackId?.let { trackId ->
                playlist.find { it.id == trackId }?.let { track ->
                    currentTrack = track
                }
            }
        } else {
            // Restaurer l'ordre original
            playlist = originalPlaylist

            // Recharger la piste courante à sa position originale
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

        // Appliquer le mode répétition à ExoPlayer
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
                // TODO: Implémenter avec ShareCompat
                println("Partage de la piste: ${track.title} - ${track.artist}")
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            currentTrack?.let { track ->
                // TODO: Implémenter la logique de favoris dans TrackRepo
                // Pour l'instant, juste un log
                println("Favori basculé pour: ${track.title}")
            }
        }
    }

    fun addToPlaylist() {
        viewModelScope.launch {
            currentTrack?.let { track ->
                // TODO: Implémenter l'ajout à une playlist utilisateur
                println("Ajout à la playlist: ${track.title}")
            }
        }
    }

    fun refreshLibrary() {
        viewModelScope.launch {
            try {
                trackRepo.refreshTracks()
                // La playlist sera automatiquement mise à jour via le Flow
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun searchTracks(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                // Si la recherche est vide, revenir à toutes les pistes
                trackRepo.tracks().collect { tracks ->
                    playlist = tracks
                    originalPlaylist = tracks
                }
            } else {
                trackRepo.searchTracks(query).collect { tracks ->
                    playlist = tracks
                    originalPlaylist = tracks
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        updateJob?.cancel()
        exoPlayer.release()
    }

    enum class RepeatMode {
        OFF, ALL, ONE
    }
}