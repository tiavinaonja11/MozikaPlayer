package com.example.mozika.ui.library.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.mozika.domain.model.Track
import com.example.mozika.ui.player.PlayerVM
import com.example.mozika.ui.playlist.PlaylistVM
import com.example.mozika.ui.playlist.PlaylistWithCount
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

/**
 * Composant TrackItem principal utilisé dans la bibliothèque
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackItem(
    track: Track,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val playlistVM: PlaylistVM = hiltViewModel()
    val playerVM: PlayerVM = viewModel(
        viewModelStoreOwner = LocalContext.current as androidx.lifecycle.ViewModelStoreOwner
    )

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E1E1E),
        onClick = {
            playerVM.load(track.id, autoPlay = true)
            navController.navigate("player/${track.id}")
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Icône de musique
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1DB954).copy(alpha = 0.3f),
                                    Color(0xFF1DB954).copy(alpha = 0.1f)
                                )
                            )
                        )
                )
                Icon(
                    Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = Color(0xFF1DB954),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Informations de la chanson
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp
                        ),
                        color = Color(0xFFB3B3B3),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = formatDuration(track.duration),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF808080)
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Bouton menu 3 points - ouvre le bottom sheet
            IconButton(
                onClick = { showBottomSheet = true },
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Menu",
                    tint = Color(0xFF808080),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    // Bottom Sheet Menu - Style lecteur moderne
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
                scope.launch {
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

/**
 * Contenu du Bottom Sheet - Style moderne avec info chanson en haut
 */
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

/**
 * Version TrackItem utilisée dans les écrans Album
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackItemAlbum(
    track: Track,
    navController: NavHostController,
    albumTitle: String,
    playerVM: PlayerVM,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val playlistVM: PlaylistVM = hiltViewModel()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E1E1E),
        onClick = {
            playerVM.loadAlbum(albumTitle)
            playerVM.load(track.id, autoPlay = true)
            navController.navigate("player/${track.id}")
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1DB954).copy(alpha = 0.3f),
                                    Color(0xFF1DB954).copy(alpha = 0.1f)
                                )
                            )
                        )
                )
                Icon(
                    Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = Color(0xFF1DB954),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp
                        ),
                        color = Color(0xFFB3B3B3),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = formatDuration(track.duration),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 11.sp
                        ),
                        color = Color(0xFF808080)
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = { showBottomSheet = true },
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Menu",
                    tint = Color(0xFF808080),
                    modifier = Modifier.size(18.dp)
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
                scope.launch {
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

/**
 * Dialog d'informations de la chanson
 */
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

/**
 * Dialog pour ajouter à une playlist
 */
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

/**
 * Partager une chanson
 */
private fun shareTrack(context: android.content.Context, track: Track) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Écoute cette chanson !")
        putExtra(Intent.EXTRA_TEXT, "${track.title} - ${track.artist}\nAlbum: ${track.album}")
    }
    context.startActivity(Intent.createChooser(shareIntent, "Partager via"))
}

/**
 * Définir comme sonnerie
 */
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

/**
 * Fonction utilitaire pour formater la durée
 */
private fun formatDuration(milliseconds: Int): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}

/**
 * TrackItem avec indicateur de lecture en cours et menu 3 points (TrackOptionsMenu)
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackItemWithPlayingIndicator(
    track: Track,
    isPlaying: Boolean,
    isCurrentTrack: Boolean,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val playerVM: PlayerVM = viewModel(
        viewModelStoreOwner = LocalContext.current as androidx.lifecycle.ViewModelStoreOwner
    )

    // État du menu déroulant 3 points
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = if (isCurrentTrack) Color(0xFF1DB954).copy(alpha = 0.08f) else Color(0xFF1E1E1E),
        onClick = {
            playerVM.load(track.id, autoPlay = true)
            navController.navigate("player/${track.id}")
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icône musique / indicateur lecture
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1DB954).copy(alpha = if (isCurrentTrack) 0.5f else 0.3f),
                                    Color(0xFF1DB954).copy(alpha = if (isCurrentTrack) 0.2f else 0.1f)
                                )
                            )
                        )
                )
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.VolumeUp else Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = Color(0xFF1DB954),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Infos chanson
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isCurrentTrack) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isCurrentTrack) Color(0xFF1DB954) else Color.White
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = Color(0xFFB3B3B3),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatDuration(track.duration),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF808080)
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Bouton 3 points + menu déroulant
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Menu",
                        tint = if (isCurrentTrack) Color(0xFF1DB954) else Color(0xFF808080),
                        modifier = Modifier.size(18.dp)
                    )
                }

                com.example.mozika.ui.components.TrackOptionsMenu(
                    track = track,
                    expanded = menuExpanded,
                    onDismiss = { menuExpanded = false }
                )
            }
        }
    }
}