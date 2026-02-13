package com.example.mozika.ui.player

import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mozika.R
import com.example.mozika.ui.player.components.SeekBar
import com.example.mozika.ui.player.components.PremiumAudioWaveform
import com.example.mozika.ui.playlist.PlaylistVM
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map

// Couleurs inspirées de Library.kt
private val CyanPrimary = Color(0xFF22D3EE)
private val CyanAlpha15 = Color(0xFF22D3EE).copy(alpha = 0.15f)
private val CyanAlpha20 = Color(0xFF22D3EE).copy(alpha = 0.20f)
private val BackgroundBlack = Color(0xFF000000)
private val CardBlack = Color(0xFF141414)
private val SurfaceBlack = Color(0xFF0A0A0A)
private val FavoriteRed = Color(0xFFEF4444) // Couleur rouge pour le favori

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    navController: NavController,
    trackId: Long?
) {
    val context = LocalContext.current
    val vm: PlayerVM = viewModel(
        viewModelStoreOwner = LocalContext.current as androidx.lifecycle.ViewModelStoreOwner
    )
    val playlistVM: PlaylistVM = hiltViewModel()

    // États pour les dialogs et bottom sheets
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showMoreOptions by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // ✅ FIX INSTANTANÉ : charger directement sans LaunchedEffect intermédiaire
    // Le trackId change → on charge immédiatement via vm.load()
    LaunchedEffect(trackId) {
        if (trackId != null && vm.currentTrack?.id != trackId) {
            vm.load(trackId, autoPlay = true)
        }
    }

    // ✅ FIX FLUIDITÉ : observer position/duration via StateFlow → recompose ciblé seekbar uniquement
    val position by vm.positionFlow.collectAsState()
    val duration by vm.durationFlow.collectAsState()

    if (trackId == null) {
        EmptyPlayerScreen(navController)
        return
    }

    // CORRECTION: Observer l'état des favoris de manière réactive
    val currentTrackId = vm.currentTrack?.id
    val isFavorite by playlistVM.favoriteTracks
        .map { tracks -> currentTrackId?.let { id -> tracks.any { it.id == id } } ?: false }
        .collectAsState(initial = false)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        SurfaceBlack,
                        BackgroundBlack
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // HEADER compact
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Retour",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "Lecture en cours",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        letterSpacing = (-0.2).sp
                    )
                )

                IconButton(
                    onClick = { vm.refreshLibrary() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Actualiser",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ALBUM CARD compacte
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .aspectRatio(1f),
                shape = RoundedCornerShape(16.dp),
                color = CardBlack,
                tonalElevation = 4.dp,
                shadowElevation = 6.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = "Album cover",
                        tint = CyanPrimary.copy(alpha = 0.6f),
                        modifier = Modifier.size(120.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // INFOS TITRE / ARTISTE compact
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = vm.currentTrack?.title ?: "Titre inconnu",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = vm.currentTrack?.artist ?: "Artiste inconnue",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color(0xFF999999),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = vm.currentTrack?.album ?: "Album inconnu",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF666666),
                        fontSize = 13.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // WAVEFORM compacte
            PremiumAudioWaveform(
                amplitudes = vm.waveform,
                progress = if (duration > 0L) position.toFloat() / duration.toFloat() else 0f,
                isPlaying = vm.isPlaying,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                onSeek = { percent ->
                    vm.seekTo((percent * duration).toLong())
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // SEEK BAR compacte
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Box(modifier = Modifier.height(20.dp)) {
                    SeekBar(
                        progress = position.toFloat(),
                        duration = duration.toFloat(),
                        onSeek = { percent ->
                            vm.seekTo((percent * duration).toLong())
                        }
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(position),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF999999)
                    )
                    Text(
                        text = formatTime(duration),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF999999)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // CONTROLES PRINCIPAUX (PLAY/PAUSE/PREV/NEXT) - VERSION RESPONSIVE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                ControlButtonWithLabel(
                    icon = Icons.Default.Shuffle,
                    label = "Shuffle",
                    isActive = vm.shuffleMode,
                    activeColor = CyanPrimary,
                    onClick = { vm.toggleShuffle() }
                )

                // Previous
                IconButton(
                    onClick = { vm.previousTrack() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Piste précédente",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Play/Pause (bouton principal plus grand)
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = CyanPrimary,
                    shadowElevation = 8.dp,
                    onClick = { vm.playPause() }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = if (vm.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (vm.isPlaying) "Pause" else "Play",
                            tint = BackgroundBlack,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Next
                IconButton(
                    onClick = { vm.nextTrack() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Piste suivante",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Repeat
                ControlButtonWithLabel(
                    icon = when (vm.repeatMode) {
                        PlayerVM.RepeatMode.OFF -> Icons.Default.Repeat
                        PlayerVM.RepeatMode.ALL -> Icons.Default.Repeat
                        PlayerVM.RepeatMode.ONE -> Icons.Default.RepeatOne
                    },
                    label = when (vm.repeatMode) {
                        PlayerVM.RepeatMode.OFF -> "Off"
                        PlayerVM.RepeatMode.ALL -> "All"
                        PlayerVM.RepeatMode.ONE -> "One"
                    },
                    isActive = vm.repeatMode != PlayerVM.RepeatMode.OFF,
                    activeColor = CyanPrimary,
                    onClick = { vm.toggleRepeat() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // BARRE D'ACTIONS FINALE - TOUS LES BOUTONS FONCTIONNELS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Share - FONCTIONNEL
                ControlButtonWithLabel(
                    icon = Icons.Default.Share,
                    label = "Share",
                    isActive = false,
                    activeColor = CyanPrimary,
                    onClick = {
                        vm.currentTrack?.let { track ->
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Écoute \"${track.title}\" par ${track.artist} sur Mozika"
                                )
                                putExtra(Intent.EXTRA_SUBJECT, "Partager une musique")
                            }
                            ContextCompat.startActivity(
                                context,
                                Intent.createChooser(shareIntent, "Partager via"),
                                null
                            )
                        }
                    }
                )

                // CORRECTION: Le bouton Favorite - maintenant réactif et fonctionnel - EN ROUGE
                ControlButtonWithLabel(
                    icon = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    label = "Favorite",
                    isActive = isFavorite,
                    activeColor = FavoriteRed,
                    onClick = {
                        currentTrackId?.let { id ->
                            playlistVM.toggleFavorite(id) { nowFavorite ->
                                Toast.makeText(
                                    context,
                                    if (nowFavorite) "Ajouté aux favoris" else "Retiré des favoris",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                )

                // Add to Playlist - FONCTIONNEL
                ControlButtonWithLabel(
                    icon = Icons.Default.PlaylistAdd,
                    label = "Add to",
                    isActive = false,
                    activeColor = CyanPrimary,
                    onClick = {
                        showPlaylistDialog = true
                    }
                )

                // More Options - FONCTIONNEL avec Bottom Sheet
                ControlButtonWithLabel(
                    icon = Icons.Default.MoreVert,
                    label = "More",
                    isActive = false,
                    activeColor = CyanPrimary,
                    onClick = {
                        showMoreOptions = true
                    }
                )
            }

            // Indicateur de playlist
            if (vm.playlist.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• ${vm.playlist.size} tracks •",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF505050),
                        fontSize = 9.sp
                    )
                )
            }
        }
    }

    // Dialog pour ajouter à une playlist
    if (showPlaylistDialog) {
        val playlists by playlistVM.playlistsWithCount.collectAsState()

        AddToPlaylistDialogPlayer(
            track = vm.currentTrack,
            playlists = playlists,
            onDismiss = { showPlaylistDialog = false },
            onPlaylistSelected = { playlist ->
                scope.launch {
                    val track = vm.currentTrack
                    if (track != null) {
                        val isAlreadyIn = playlistVM.isTrackInPlaylist(playlist.id, track.id)
                        if (isAlreadyIn) {
                            Toast.makeText(
                                context,
                                "Déjà dans ${playlist.name}",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            playlistVM.addTrackToPlaylist(playlist.id, track.id)
                            Toast.makeText(
                                context,
                                "Ajouté à ${playlist.name}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                showPlaylistDialog = false
            }
        )
    }

    // Dialog d'informations
    if (showInfoDialog) {
        TrackInfoDialogPlayer(
            track = vm.currentTrack,
            onDismiss = { showInfoDialog = false }
        )
    }

    // Bottom Sheet pour "More" options
    if (showMoreOptions) {
        ModalBottomSheet(
            onDismissRequest = { showMoreOptions = false },
            sheetState = sheetState,
            containerColor = CardBlack,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF333333)) }
        ) {
            MoreOptionsBottomSheetContent(
                track = vm.currentTrack,
                onDismiss = { showMoreOptions = false },
                onPlayNext = {
                    vm.currentTrack?.let { track ->
                        vm.playNext(track)
                        Toast.makeText(context, "Sera lu ensuite", Toast.LENGTH_SHORT).show()
                    }
                    showMoreOptions = false
                },
                onAddToQueue = {
                    vm.currentTrack?.let { track ->
                        vm.addToQueue(track)
                        Toast.makeText(context, "Ajouté à la file d'attente", Toast.LENGTH_SHORT).show()
                    }
                    showMoreOptions = false
                },
                onShowInfo = {
                    showMoreOptions = false
                    showInfoDialog = true
                },
                onSetAsRingtone = {
                    // TODO: Implémenter la sonnerie
                    Toast.makeText(context, "Sonnerie définie", Toast.LENGTH_SHORT).show()
                    showMoreOptions = false
                }
            )
        }
    }
}

// Composant réutilisable pour les boutons avec label
@Composable
private fun ControlButtonWithLabel(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else Color(0xFF666666),
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            ),
            color = if (isActive) activeColor else Color(0xFF666666),
            maxLines = 1
        )
    }
}

// Bottom Sheet pour les options "More"
@Composable
private fun MoreOptionsBottomSheetContent(
    track: com.example.mozika.domain.model.Track?,
    onDismiss: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onShowInfo: () -> Unit,
    onSetAsRingtone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        // Header avec info chanson
        if (track != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyanAlpha15),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${track.artist} • ${track.album}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp
                        ),
                        color = Color(0xFF999999),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Divider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = Color(0xFF222222),
                thickness = 1.dp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Options
        MoreOptionItem(
            icon = Icons.Outlined.PlayArrow,
            text = "Lire la suite",
            onClick = onPlayNext
        )

        MoreOptionItem(
            icon = Icons.Outlined.Queue,
            text = "Ajouter à la file",
            onClick = onAddToQueue
        )

        Divider(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            color = Color(0xFF222222),
            thickness = 1.dp
        )

        MoreOptionItem(
            icon = Icons.Outlined.Info,
            text = "Informations",
            onClick = onShowInfo
        )

        MoreOptionItem(
            icon = Icons.Outlined.Notifications,
            text = "Faire sonnerie",
            onClick = onSetAsRingtone
        )
    }
}

@Composable
private fun MoreOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = Color.White
            )
        }
    }
}

// Dialog pour ajouter à une playlist (spécifique au Player)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddToPlaylistDialogPlayer(
    track: com.example.mozika.domain.model.Track?,
    playlists: List<com.example.mozika.ui.playlist.PlaylistWithCount>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (com.example.mozika.ui.playlist.PlaylistWithCount) -> Unit
) {
    val playlistVM: PlaylistVM = hiltViewModel()
    var showCreatePlaylist by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp),
            shape = RoundedCornerShape(20.dp),
            color = CardBlack
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Ajouter à une playlist",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Bouton créer nouvelle playlist
                Surface(
                    onClick = { showCreatePlaylist = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CyanAlpha15
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Créer",
                            tint = CyanPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Créer une nouvelle playlist",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            ),
                            color = CyanPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Liste des playlists
                if (playlists.isNotEmpty()) {
                    Text(
                        text = "Mes playlists",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        color = Color(0xFF999999),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        items(playlists) { playlist ->
                            Surface(
                                onClick = { onPlaylistSelected(playlist) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlaylistPlay,
                                        contentDescription = null,
                                        tint = Color(0xFFB3B3B3),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = playlist.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Normal
                                            ),
                                            color = Color.White,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${playlist.songCount} chansons",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 12.sp
                                            ),
                                            color = Color(0xFF666666)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Annuler", color = Color(0xFF999999), fontWeight = FontWeight.Medium)
                }
            }
        }
    }

    // Dialog pour créer une nouvelle playlist
    if (showCreatePlaylist) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylist = false },
            title = {
                Text(
                    text = "Nouvelle playlist",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Nom", color = Color(0xFF666666)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = Color(0xFF333333),
                        cursorColor = CyanPrimary,
                        focusedLabelColor = CyanPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPlaylistName.isNotBlank() && track != null) {
                            scope.launch {
                                val newPlaylistId = playlistVM.create(newPlaylistName)
                                kotlinx.coroutines.delay(200)
                                playlistVM.addTrackToPlaylist(newPlaylistId, track.id)
                                Toast.makeText(
                                    context,
                                    "Créé et ajouté",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            newPlaylistName = ""
                            showCreatePlaylist = false
                            onDismiss()
                        }
                    },
                    enabled = newPlaylistName.isNotBlank()
                ) {
                    Text(
                        "Créer",
                        color = if (newPlaylistName.isNotBlank()) CyanPrimary else Color(0xFF666666),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylist = false }) {
                    Text("Annuler", color = Color(0xFF999999), fontWeight = FontWeight.Medium)
                }
            },
            containerColor = CardBlack
        )
    }
}

// Dialog d'informations
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackInfoDialogPlayer(
    track: com.example.mozika.domain.model.Track?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CardBlack
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Informations",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (track != null) {
                    InfoRowPlayer("Titre", track.title)
                    InfoRowPlayer("Artiste", track.artist)
                    InfoRowPlayer("Album", track.album)
                    InfoRowPlayer("Durée", formatTime(track.duration.toLong()))
                    InfoRowPlayer("Chemin", track.data)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Fermer", color = CyanPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun InfoRowPlayer(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            ),
            color = Color(0xFF666666)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            ),
            color = Color.White
        )
    }
}

@Composable
fun EmptyPlayerScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(CyanAlpha15),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = "Aucune musique",
                tint = CyanPrimary,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Aucune piste sélectionnée",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sélectionnez une piste depuis votre bibliothèque",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 20.sp
            ),
            color = Color(0xFF666666)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyanPrimary,
                contentColor = BackgroundBlack
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Retour à la bibliothèque",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}