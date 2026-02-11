package com.example.mozika.ui.library

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.mozika.domain.model.Track
import com.example.mozika.ui.nav.navigateToTrack
import com.example.mozika.ui.player.PlayerVM
import com.example.mozika.ui.playlist.PlaylistVM
import com.example.mozika.ui.playlist.PlaylistWithCount
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    navController: NavHostController,
    artistId: String,
    viewModel: LibraryVM = hiltViewModel()
) {
    val artists by viewModel.artists.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val tracks by viewModel.tracks.collectAsState()

    // Récupérer PlayerVM
    val playerVM: PlayerVM = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()

    // Trouver l'artiste correspondant
    val artist = remember(artistId, artists) {
        artists.find { it.id == artistId }
    }

    // Filtrer les albums de cet artiste
    val artistAlbums = remember(artist, albums) {
        if (artist != null) {
            albums.filter { it.artist == artist.name }
        } else {
            emptyList()
        }
    }

    // Filtrer les chansons de cet artiste
    val artistTracks = remember(artist, tracks) {
        if (artist != null) {
            tracks.filter { it.artist == artist.name }
        } else {
            emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = artist?.name ?: "Artiste",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212)
                )
            )
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        if (artist == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFF121212)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Artiste non trouvé",
                    color = Color.White
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFF121212)),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    // En-tête de l'artiste
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF1DB954).copy(alpha = 0.3f),
                                            Color(0xFF1E1E1E).copy(alpha = 0.8f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = artist.name.take(1).uppercase(),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 60.sp
                                ),
                                color = Color(0xFF1DB954)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = artist.name,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF1E1E1E),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "${artistAlbums.size} albums",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = Color(0xFFB3B3B3),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF1E1E1E),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "${artistTracks.size} titres",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = Color(0xFF1DB954),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Bouton "Lire tout" - CORRIGÉ
                        Button(
                            onClick = {
                                if (artistTracks.isNotEmpty()) {
                                    coroutineScope.launch {
                                        // 1. Récupérer toutes les chansons de l'artiste
                                        val tracks = playerVM.loadArtistTracks(artist.name)

                                        // 2. Charger la playlist avec le contexte Artist
                                        playerVM.loadPlaylistAndPlay(
                                            newPlaylist = tracks,
                                            trackId = artistTracks.first().id,
                                            context = PlayerVM.PlaylistContext.Artist(artist.name),
                                            autoPlay = true
                                        )

                                        // 3. Attendre un peu pour que la playlist soit chargée
                                        delay(50)

                                        // 4. Naviguer vers la première piste
                                        navController.navigateToTrack(artistTracks.first().id)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1DB954)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = "Lire tout",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Lire tout",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Divider(
                            color = Color(0xFF404040),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Albums (${artistAlbums.size})",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                // Afficher les albums
                items(artistAlbums) { album ->
                    AlbumItem(album = album, navController = navController)
                }

                // Section Titres populaires
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(
                        color = Color(0xFF404040),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Titres populaires",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Afficher les chansons
                items(artistTracks.take(10)) { track ->
                    TrackItemArtistModern(
                        track = track,
                        navController = navController,
                        artistName = artist.name,
                        playerVM = playerVM
                    )
                }

                if (artistTracks.size > 10) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(
                            onClick = {
                                // Afficher toutes les chansons
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Voir toutes les ${artistTracks.size} chansons",
                                color = Color(0xFF1DB954)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Version MODERNISÉE avec Bottom Sheet pour les artistes
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackItemArtistModern(
    track: Track,
    navController: NavHostController,
    artistName: String,
    playerVM: PlayerVM
) {
    val context = LocalContext.current
    val playlistVM: PlaylistVM = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E1E1E),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        onClick = {
            coroutineScope.launch {
                // 1. Récupérer toutes les chansons de l'artiste
                val artistTracks = playerVM.loadArtistTracks(artistName)

                // 2. Charger la playlist avec le contexte Artist
                playerVM.loadPlaylistAndPlay(
                    newPlaylist = artistTracks,
                    trackId = track.id,
                    context = PlayerVM.PlaylistContext.Artist(artistName),
                    autoPlay = true
                )

                // 3. Attendre un peu
                delay(50)

                // 4. Naviguer vers le player
                navController.navigate("player/${track.id}")
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1DB954).copy(alpha = 0.3f),
                                Color(0xFF1DB954).copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = Color(0xFF1DB954),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp
                        ),
                        color = Color(0xFFB3B3B3),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = formatDuration(track.duration),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF808080),
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { showBottomSheet = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Menu",
                    tint = Color(0xFF808080),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    // Bottom Sheet Menu
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFF282828),
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF404040)) }
        ) {
            TrackOptionsBottomSheetContent(
                track = track,
                onDismiss = { showBottomSheet = false },
                onPlayNext = {
                    playerVM.playNext(track)
                    Toast.makeText(context, "Sera lu ensuite", Toast.LENGTH_SHORT).show()
                    showBottomSheet = false
                },
                onAddToQueue = {
                    playerVM.addToQueue(track)
                    Toast.makeText(context, "Ajouté à la file d'attente", Toast.LENGTH_SHORT).show()
                    showBottomSheet = false
                },
                onAddToPlaylist = {
                    showBottomSheet = false
                    showPlaylistDialog = true
                },
                onShare = {
                    shareTrack(context, track)
                    showBottomSheet = false
                },
                onInfo = {
                    showBottomSheet = false
                    showInfoDialog = true
                },
                onSetAsRingtone = {
                    setAsRingtone(context, track)
                    showBottomSheet = false
                },
                onHide = {
                    // TODO: Masquer la chanson
                    showBottomSheet = false
                }
            )
        }
    }

    // Dialog pour ajouter à une playlist
    if (showPlaylistDialog) {
        val playlists by playlistVM.playlistsWithCount.collectAsState()

        AddToPlaylistDialog(
            track = track,
            playlists = playlists,
            onDismiss = { showPlaylistDialog = false },
            onPlaylistSelected = { playlist ->
                coroutineScope.launch {
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
                showPlaylistDialog = false
            }
        )
    }

    // Dialog d'informations
    if (showInfoDialog) {
        TrackInfoDialog(
            track = track,
            onDismiss = { showInfoDialog = false }
        )
    }
}

private fun formatDuration(milliseconds: Int): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}

// ============== COMPOSANTS PARTAGÉS ==============

@Composable
private fun TrackOptionsBottomSheetContent(
    track: Track,
    onDismiss: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onShare: () -> Unit,
    onInfo: () -> Unit,
    onSetAsRingtone: () -> Unit,
    onHide: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        // Header avec info chanson
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pochette/Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1DB954).copy(alpha = 0.3f),
                                Color(0xFF1DB954).copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = Color(0xFF1DB954),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info texte
            Column(
                modifier = Modifier.weight(1f)
            ) {
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
                    color = Color(0xFFB3B3B3),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Divider(
            modifier = Modifier.padding(horizontal = 20.dp),
            color = Color(0xFF404040),
            thickness = 0.5.dp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Options
        TrackOptionItem(
            icon = Icons.Outlined.PlayArrow,
            text = "Lire la suite",
            onClick = onPlayNext
        )

        TrackOptionItem(
            icon = Icons.Outlined.Queue,
            text = "Ajouter à la file",
            onClick = onAddToQueue
        )

        TrackOptionItem(
            icon = Icons.Outlined.PlaylistAdd,
            text = "Ajouter à la playlist",
            onClick = onAddToPlaylist
        )

        TrackOptionItem(
            icon = Icons.Outlined.Share,
            text = "Partager",
            onClick = onShare
        )

        Divider(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            color = Color(0xFF404040),
            thickness = 0.5.dp
        )

        TrackOptionItem(
            icon = Icons.Outlined.Info,
            text = "Informations",
            onClick = onInfo
        )

        TrackOptionItem(
            icon = Icons.Outlined.Notifications,
            text = "Faire sonnerie",
            onClick = onSetAsRingtone
        )

        TrackOptionItem(
            icon = Icons.Outlined.VisibilityOff,
            text = "Masquer",
            onClick = onHide
        )
    }
}

@Composable
private fun TrackOptionItem(
    icon: ImageVector,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddToPlaylistDialog(
    track: Track,
    playlists: List<PlaylistWithCount>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (PlaylistWithCount) -> Unit
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
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF282828)
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
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1DB954).copy(alpha = 0.15f)
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
                            tint = Color(0xFF1DB954),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Créer une nouvelle playlist",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            ),
                            color = Color(0xFF1DB954)
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
                        color = Color(0xFFB3B3B3),
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
                                        imageVector = Icons.Rounded.PlaylistPlay,
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
                                            color = Color(0xFF808080)
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
                    Text("Annuler", color = Color(0xFF1DB954))
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
                    label = { Text("Nom", color = Color(0xFFB3B3B3)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF1DB954),
                        unfocusedBorderColor = Color(0xFF404040),
                        cursorColor = Color(0xFF1DB954)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            scope.launch {
                                val newPlaylistId = playlistVM.create(newPlaylistName)
                                delay(200)
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
                        color = if (newPlaylistName.isNotBlank()) Color(0xFF1DB954) else Color(0xFF808080)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylist = false }) {
                    Text("Annuler", color = Color.White)
                }
            },
            containerColor = Color(0xFF282828)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackInfoDialog(
    track: Track,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF282828)
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

                InfoRow("Titre", track.title)
                InfoRow("Artiste", track.artist)
                InfoRow("Album", track.album)
                InfoRow("Durée", formatDuration(track.duration))
                InfoRow("Chemin", track.data)

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Fermer", color = Color(0xFF1DB954))
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF808080),
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

private fun shareTrack(context: android.content.Context, track: Track) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Écoute cette chanson !")
        putExtra(Intent.EXTRA_TEXT, "${track.title} - ${track.artist}\nAlbum: ${track.album}")
    }
    context.startActivity(Intent.createChooser(shareIntent, "Partager via"))
}

private fun setAsRingtone(context: android.content.Context, track: Track) {
    try {
        val file = File(track.data)
        if (!file.exists()) {
            Toast.makeText(context, "Fichier introuvable", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Settings.System.canWrite(context)) {
            Toast.makeText(
                context,
                "Permission requise",
                Toast.LENGTH_LONG
            ).show()
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
            return
        }

        val uri = Uri.fromFile(file)
        RingtoneManager.setActualDefaultRingtoneUri(
            context,
            RingtoneManager.TYPE_RINGTONE,
            uri
        )
        Toast.makeText(context, "Sonnerie définie", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}