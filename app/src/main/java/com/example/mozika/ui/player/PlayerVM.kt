package com.example.mozika.ui.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.example.mozika.data.datastore.PlayerPreferences
import com.example.mozika.data.db.entity.Track
import com.example.mozika.data.repo.TrackRepo
import com.example.mozika.domain.usecase.GenWaveform
import com.example.mozika.domain.usecase.GetTracks
import com.example.mozika.service.PlaybackService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.example.mozika.domain.model.Track as DomainTrack

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(UnstableApi::class)
@HiltViewModel
class PlayerVM @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exoPlayer: ExoPlayer,
    private val genWaveform: GenWaveform,
    private val trackRepo: TrackRepo,
    private val playerPreferences: PlayerPreferences,
    private val getTracks: GetTracks,
    private val mediaSession: MediaSession  // ✅ CORRECTION: Utiliser MediaSession de androidx.media3
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
    private val _currentTrackFlow = MutableStateFlow<DomainTrack?>(null)
    val currentTrackFlow: StateFlow<DomainTrack?> = _currentTrackFlow.asStateFlow()

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
        println("✅ DEBUG - PlayerVM initialisé avec MediaSession")
        setupPlayerListeners()
        startPlayerUpdates()
        restorePlayerState()
        startAutoSave()
        loadAllTracks()
    }

    // ============================================
    // CORRECTIONS CRITIQUES POUR LES NOTIFICATIONS
    // ============================================

    @RequiresApi(Build.VERSION_CODES.O)
    fun load(trackId: Long, autoPlay: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. On cherche l'index dans la playlist (qui contient des DomainTrack)
                val trackIndex = playlist.indexOfFirst { it.id == trackId }
                if (trackIndex == -1) return@launch

                // 2. ✅ CORRECTION: Préparation des MediaItems avec métadonnées COMPLÈTES
                val mediaItems = playlist.map { track ->
                    createMediaItemWithCompleteMetadata(track)  // ✅ Utiliser cette méthode au lieu de builder simple
                }

                withContext(Dispatchers.Main) {
                    // 3. Charger la liste complète dans le player (active les boutons Suivant/Précédent)
                    mediaSession.player.setMediaItems(mediaItems)
                    mediaSession.player.seekTo(trackIndex, 0L)
                    mediaSession.player.prepare()

                    if (autoPlay) mediaSession.player.play()

                    // 4. RÉSOLUTION DES ERREURS DE TYPE
                    val selectedTrack: DomainTrack = playlist[trackIndex]

                    // On assigne le DomainTrack aux deux variables
                    currentTrack = selectedTrack
                    _currentTrackFlow.value = selectedTrack

                    // ✅ CORRECTION: Invalider la notification pour forcer la mise à jour
                    // Note: On ne peut pas appeler directement invalidateNotification() depuis ici
                    // car c'est dans le Service, mais les métadonnées sont maintenant correctement définies
                    // et le listener onMediaMetadataChanged dans le Service déclenchera invalidateNotification()

                    // Génération de la waveform
                    generateWaveformForTrack(selectedTrack.data)
                }
            } catch (e: Exception) {
                println("❌ Erreur de chargement : ${e.message}")
            }
        }
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Crée un MediaItem avec métadonnées COMPLÈTES pour les notifications
     */
    /**
     * ✅ Crée un MediaItem avec métadonnées COMPLÈTES pour les notifications
     */
    private fun createMediaItemWithCompleteMetadata(track: DomainTrack): MediaItem {
        val uri = Uri.parse(track.data)

        // ✅ EXTRAIRE LA POCHETTE AVEC GESTION D'ERREUR
        val artworkData = extractAlbumArtWithFallback(track.data)

        // ✅ CONSTRUIRE LES MÉTADONNÉES COMPLÈTES
        val metadata = MediaMetadata.Builder()
            .setTitle(track.title)                // Titre dans la notification
            .setArtist(track.artist)              // Artiste dans la notification
            .setAlbumTitle(track.album)           // Album dans la notification
            .setArtworkData(artworkData, MediaMetadata.PICTURE_TYPE_FRONT_COVER) // Pochette
            .setIsPlayable(true)
            .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
            .build()

        println("✅ DEBUG - Métadonnées créées pour: ${track.title} - ${track.artist}")

        return MediaItem.Builder()
            .setMediaId(track.id.toString())
            .setUri(uri)
            .setMediaMetadata(metadata) // ⬅️ CRITIQUE: Inclure les métadonnées
            .build()
    }

    /**
     * ✅ Extraction de pochette avec meilleure gestion d'erreur
     */
    private fun extractAlbumArtWithFallback(path: String): ByteArray {
        return try {
            val uri = Uri.parse(path)
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val art = retriever.embeddedPicture
            retriever.release()

            if (art != null) {
                println("✅ DEBUG - Pochette extraite avec succès pour: $path")
                art
            } else {
                println("⚠️ DEBUG - Pas de pochette dans le fichier, utilisation de l'image par défaut")
                createDefaultAlbumArt()
            }
        } catch (e: Exception) {
            println("❌ DEBUG - Erreur extraction pochette: ${e.message}")
            createDefaultAlbumArt()
        }
    }

    /**
     * ✅ CONSERVATION de votre méthode originale (pour compatibilité)
     */
    private fun createMediaItemFromTrack(track: DomainTrack): MediaItem {
        // Appelle la nouvelle méthode pour garantir la cohérence
        return createMediaItemWithCompleteMetadata(track)
    }

    /**
     * ✅ CONSERVATION de votre méthode originale
     */
    private fun extractAlbumArt(path: String): ByteArray? {
        return try {
            val uri = Uri.parse(path)
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val art = retriever.embeddedPicture
            retriever.release()

            if (art == null) {
                createDefaultAlbumArt()
            } else {
                art
            }
        } catch (e: Exception) {
            e.printStackTrace()
            createDefaultAlbumArt()
        }
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Génération de waveform avec gestion d'erreur
     */
    private fun generateWaveformForTrack(path: String) {
        viewModelScope.launch {
            try {
                waveform = genWaveform(path)
                println("✅ DEBUG - Waveform générée")
            } catch (e: Exception) {
                waveform = createFallbackWaveform()
                println("⚠️ DEBUG - Waveform par défaut utilisée")
            }
        }
    }

    /**
     * ✅ CONSERVATION de votre méthode originale
     */
    private fun createDefaultAlbumArt(): ByteArray {
        val bitmap = android.graphics.Bitmap.createBitmap(512, 512, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(0xFF1DB954.toInt())

        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 200f
            textAlign = android.graphics.Paint.Align.CENTER
        }

        canvas.drawText("♪", 256f, 300f, paint)

        val stream = java.io.ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    // ============================================
    // MÉTHODES DE FILE D'ATTENTE (NOUVELLES) - CONSERVÉES
    // ============================================

    /**
     * Ajoute une chanson à la fin de la file d'attente
     */
    fun addToQueue(track: DomainTrack) {
        viewModelScope.launch {
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
                println("✅ DEBUG - Piste ajoutée à la file d'attente")
            }
        }
    }

    /**
     * Ajoute une chanson pour qu'elle soit lue juste après la chanson actuelle
     */
    fun playNext(track: DomainTrack) {
        viewModelScope.launch {
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
            currentQueue.removeAll { it.id == entityTrack.id }

            val insertIndex = _currentQueueIndex.value + 1
            if (insertIndex <= currentQueue.size) {
                currentQueue.add(insertIndex, entityTrack)
            } else {
                currentQueue.add(entityTrack)
            }

            _queue.value = currentQueue
            println("✅ DEBUG - Piste programmée pour lecture suivante")
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

                    playlist = albumTracks
                    playlistContext = PlaylistContext.Album(albumTitle)
                    updatePlayerStateFlow()

                    println("✅ DEBUG - Album chargé: $albumTitle (${albumTracks.size} pistes)")
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

                playlist = tracks
                originalPlaylist = tracks
                updatePlayerStateFlow()

                println("✅ DEBUG - Playlist chargée (${tracks.size} pistes)")
            }
        }
    }

    /**
     * Supprime une chanson de la file d'attente
     */
    fun removeFromQueue(trackId: Long) {
        viewModelScope.launch {
            val currentQueue = _queue.value.toMutableList()
            val index = currentQueue.indexOfFirst { it.id == trackId }

            if (index != -1) {
                currentQueue.removeAt(index)
                _queue.value = currentQueue

                if (index < _currentQueueIndex.value) {
                    _currentQueueIndex.value = maxOf(0, _currentQueueIndex.value - 1)
                }

                println("✅ DEBUG - Piste supprimée de la file d'attente")
            }
        }
    }

    /**
     * Déplace une chanson dans la file d'attente
     */
    fun moveInQueue(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            val currentQueue = _queue.value.toMutableList()
            if (fromIndex in currentQueue.indices && toIndex in currentQueue.indices) {
                val item = currentQueue.removeAt(fromIndex)
                currentQueue.add(toIndex, item)
                _queue.value = currentQueue

                when {
                    fromIndex == _currentQueueIndex.value -> _currentQueueIndex.value = toIndex
                    fromIndex < _currentQueueIndex.value && toIndex >= _currentQueueIndex.value ->
                        _currentQueueIndex.value = maxOf(0, _currentQueueIndex.value - 1)
                    fromIndex > _currentQueueIndex.value && toIndex <= _currentQueueIndex.value ->
                        _currentQueueIndex.value = minOf(currentQueue.size - 1, _currentQueueIndex.value + 1)
                }

                println("✅ DEBUG - Piste déplacée dans la file d'attente")
            }
        }
    }

    /**
     * Vide la file d'attente
     */
    fun clearQueue() {
        viewModelScope.launch {
            _queue.value = emptyList()
            _currentQueueIndex.value = 0
            println("✅ DEBUG - File d'attente vidée")
        }
    }

    /**
     * Joue une chanson spécifique de la file d'attente
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun playFromQueue(index: Int) {
        viewModelScope.launch {
            if (index in _queue.value.indices) {
                _currentQueueIndex.value = index
                val track = _queue.value[index]

                val domainTrack = DomainTrack(
                    id = track.id,
                    title = track.title,
                    artist = track.artist,
                    album = track.album,
                    duration = track.duration,
                    dateAdded = track.dateAdded,
                    data = track.path
                )

                load(domainTrack.id, autoPlay = true)
                println("✅ DEBUG - Lecture depuis la file d'attente (index: $index)")
            }
        }
    }

    // ============================================
    // ✅ CORRECTION CRITIQUE : setupPlayerListeners()
    // ============================================

    private fun setupPlayerListeners() {
        // ✅ Utiliser mediaSession.player au lieu de exoPlayer
        mediaSession.player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                _isPlayingFlow.value = playing
                updatePlayerStateFlow()

                if (playing) {
                    println("▶️ DEBUG - Lecture en cours via MediaSession, notification active")
                } else {
                    println("⏸️ DEBUG - Lecture en pause via MediaSession")
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_ENDED -> {
                        println("⏹️ DEBUG - Lecture terminée via MediaSession")
                        when (repeatMode) {
                            RepeatMode.ONE -> {
                                mediaSession.player.seekTo(0)
                                println("🔂 DEBUG - Répétition d'une piste via MediaSession")
                            }
                            RepeatMode.ALL -> {
                                nextTrack()
                                println("🔁 DEBUG - Passage à la piste suivante (mode répétition) via MediaSession")
                            }
                            RepeatMode.OFF -> {
                                currentTrack?.let { current ->
                                    val currentIndex = playlist.indexOfFirst { it.id == current.id }
                                    if (currentIndex < playlist.size - 1) {
                                        nextTrack()
                                        println("⏭️ DEBUG - Passage à la piste suivante via MediaSession")
                                    }
                                }
                            }
                        }
                    }
                    Player.STATE_READY -> {
                        duration = mediaSession.player.duration.takeIf { it != C.TIME_UNSET } ?: 0L
                        println("✅ DEBUG - Player prêt via MediaSession, durée: ${duration}ms")
                    }
                    else -> {}
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                println("❌ DEBUG - Erreur de lecture via MediaSession: ${error.message}")
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItem?.let { item ->
                    val trackId = item.mediaId?.toLongOrNull()
                    playlist.find { it.id == trackId }?.let { track ->
                        currentTrack = track
                        // Mettre à jour l'UI
                        updatePlayerStateFlow()
                        println("🔄 Transition vers : ${track.title}")
                    }
                }
            }
        })
    }

    // ============================================
    // ✅ CORRECTION : startPlayerUpdates()
    // ============================================

    private fun startPlayerUpdates() {
        updateJob = viewModelScope.launch {
            while (true) {
                if (mediaSession.player.isPlaying) {
                    position = mediaSession.player.currentPosition.coerceAtLeast(0L)
                    duration = mediaSession.player.duration.takeIf { it != C.TIME_UNSET } ?: 0L
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

    // ============================================
    // ✅ CORRECTIONS : SAUVEGARDE ET RESTAURATION
    // ============================================

    private fun savePlayerState() {
        viewModelScope.launch {
            currentTrack?.let { track ->
                val contextType = when (playlistContext) {
                    is PlaylistContext.Album -> "album"
                    is PlaylistContext.Artist -> "artist"
                    is PlaylistContext.Search -> "search"
                    is PlaylistContext.AllTracks -> "all_tracks"
                    else -> "none"
                }

                val contextId = when (val ctx = playlistContext) {
                    is PlaylistContext.Album -> ctx.albumId
                    is PlaylistContext.Artist -> ctx.artistId
                    is PlaylistContext.Search -> ctx.query
                    else -> ""
                }

                playerPreferences.savePlayerState(
                    trackId = track.id,
                    isPlaying = mediaSession.player.isPlaying, // ✅ Utiliser mediaSession.player
                    position = mediaSession.player.currentPosition, // ✅ Utiliser mediaSession.player
                    playlistContext = contextType,
                    contextId = contextId
                )

                println("💾 DEBUG - État sauvegardé via MediaSession: ${track.title}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun restorePlayerState() {
        viewModelScope.launch {
            playerPreferences.playerState.firstOrNull()?.let { state ->
                if (state.trackId > 0) {
                    playlistContext = when (state.playlistContext) {
                        "album" -> PlaylistContext.Album(state.contextId)
                        "artist" -> PlaylistContext.Artist(state.contextId)
                        "search" -> PlaylistContext.Search(state.contextId)
                        "all_tracks" -> PlaylistContext.AllTracks
                        else -> PlaylistContext.None
                    }

                    load(state.trackId, autoPlay = false)

                    delay(200)
                    mediaSession.player.seekTo(state.position) // ✅ Utiliser mediaSession.player

                    println("↩️ DEBUG - État restauré via MediaSession: track ${state.trackId}")
                }
            }
        }
    }

    private fun startAutoSave() {
        viewModelScope.launch {
            while (true) {
                delay(5000)
                if (mediaSession.player.isPlaying) { // ✅ Utiliser mediaSession.player
                    savePlayerState()
                }
            }
        }
    }

    // ============================================
    // CHARGEMENT DES PISTES - CONSERVÉS
    // ============================================

    private fun loadAllTracks() {
        viewModelScope.launch {
            trackRepo.tracks().collect { tracks ->
                playlist = tracks
                originalPlaylist = tracks
                playlistContext = PlaylistContext.AllTracks
                updatePlayerStateFlow()
                println("📚 DEBUG - ${tracks.size} pistes chargées dans la playlist")
            }
        }
    }

    // ============================================
    // MÉTHODES EXISTANTES - CONSERVÉES AVEC CORRECTIONS
    // ============================================

    fun loadPlaylistAndPlay(
        newPlaylist: List<DomainTrack>,
        trackId: Long,
        autoPlay: Boolean = true,
        context: PlaylistContext = PlaylistContext.AllTracks
    ) {
        playlist = newPlaylist
        originalPlaylist = newPlaylist
        playlistContext = context
        updatePlayerStateFlow()
        load(trackId, autoPlay)
        println("🎵 DEBUG - Playlist chargée (${newPlaylist.size} pistes)")
    }

    fun loadAlbumTracks(albumId: String): List<DomainTrack> {
        return playlist.filter {
            it.album.equals(albumId, ignoreCase = true)
        }
    }

    fun loadArtistTracks(artistName: String): List<DomainTrack> {
        return playlist.filter {
            it.artist.equals(artistName, ignoreCase = true)
        }
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
        println("🔄 DEBUG - Piste actuelle mise à jour: ${track.title}")
    }

    // ============================================
    // ✅ CORRECTIONS : CONTRÔLES DE LECTURE
    // ============================================

    fun nextTrack() {
        if (mediaSession.player.hasNextMediaItem()) {
            mediaSession.player.seekToNextMediaItem()
            println("⏭️ DEBUG - Passage au suivant via index Media3")
        } else if (playlist.isNotEmpty()) {
            // Optionnel : Revenir au début si on est à la fin
            mediaSession.player.seekTo(0, 0L)
        }
    }

    fun previousTrack() {
        if (mediaSession.player.hasPreviousMediaItem()) {
            mediaSession.player.seekToPreviousMediaItem()
            println("⏮️ DEBUG - Passage au précédent via index Media3")
        }
    }

    fun playPause() {
        if (mediaSession.player.isPlaying) { // ✅ Utiliser mediaSession.player
            mediaSession.player.pause()
            println("⏸️ DEBUG - Lecture mise en pause via MediaSession")
        } else {
            mediaSession.player.play()
            println("▶️ DEBUG - Lecture démarrée/reprise via MediaSession")
        }
        isPlaying = mediaSession.player.isPlaying // ✅ Utiliser mediaSession.player
        _isPlayingFlow.value = mediaSession.player.isPlaying // ✅ Utiliser mediaSession.player
        updatePlayerStateFlow()
    }

    fun toggleShuffle() {
        shuffleMode = !shuffleMode
        _isShuffleEnabled.value = shuffleMode

        if (shuffleMode) {
            val currentTrackId = currentTrack?.id
            playlist = playlist.shuffled()
            currentTrackId?.let { id ->
                playlist.find { it.id == id }?.let { track ->
                    currentTrack = track
                }
            }
            println("🔀 DEBUG - Mode aléatoire activé via MediaSession")
        } else {
            playlist = originalPlaylist
            currentTrack?.let { track ->
                originalPlaylist.find { it.id == track.id }?.let { originalTrack ->
                    currentTrack = originalTrack
                }
            }
            println("➡️ DEBUG - Mode aléatoire désactivé via MediaSession")
        }
        updatePlayerStateFlow()
    }

    fun toggleRepeat() {
        repeatMode = when (repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }

        _repeatModeFlow.value = repeatMode

        mediaSession.player.repeatMode = when (repeatMode) { // ✅ Utiliser mediaSession.player
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }

        println("🔁 DEBUG - Mode répétition via MediaSession: $repeatMode")
    }

    fun seekTo(ms: Long) {
        mediaSession.player.seekTo(ms) // ✅ Utiliser mediaSession.player
        println("⏱️ DEBUG - Position via MediaSession: ${ms}ms")
    }

    // ============================================
    // ACTIONS SUR LES PISTES - CONSERVÉES
    // ============================================

    fun shareTrack() {
        viewModelScope.launch {
            currentTrack?.let { track ->
                println("📤 DEBUG - Partage de la piste: ${track.title} - ${track.artist}")
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            currentTrack?.let { track ->
                println("⭐ DEBUG - Favori basculé pour: ${track.title}")
            }
        }
    }

    fun addToPlaylist() {
        viewModelScope.launch {
            currentTrack?.let { track ->
                println("➕ DEBUG - Ajout à la playlist: ${track.title}")
            }
        }
    }

    // ============================================
    // RECHERCHE ET RAFRAÎCHISSEMENT - CONSERVÉES
    // ============================================

    fun refreshLibrary() {
        viewModelScope.launch {
            try {
                trackRepo.refreshTracks()
                trackRepo.tracks().collect { tracks ->
                    playlist = tracks
                    originalPlaylist = tracks
                    playlistContext = PlaylistContext.AllTracks
                    updatePlayerStateFlow()
                    println("🔄 DEBUG - Bibliothèque rafraîchie")
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
                    println("🔍 DEBUG - Recherche: '$query' (${tracks.size} résultats)")
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
                println("❌ DEBUG - Recherche effacée")
            }
        }
    }

    // ============================================
    // INFORMATIONS SUR LA PLAYLIST - CONSERVÉES
    // ============================================

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

    // ============================================
    // NETTOYAGE - CONSERVÉ
    // ============================================

    override fun onCleared() {
        super.onCleared()
        savePlayerState()
        updateJob?.cancel()
        analyzer?.stop()
        println("🧹 DEBUG - PlayerVM nettoyé")
    }

    fun forceSaveState() {
        viewModelScope.launch {
            savePlayerState()
            println("💾 DEBUG - Sauvegarde forcée")
        }
    }

    // ============================================
    // UTILITAIRES - CONSERVÉS
    // ============================================

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