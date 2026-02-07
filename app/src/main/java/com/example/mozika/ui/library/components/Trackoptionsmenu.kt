package com.example.mozika.ui.library.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.mozika.domain.model.Track
import com.example.mozika.ui.playlist.PlaylistVM
import com.example.mozika.ui.playlist.PlaylistWithCount
import kotlinx.coroutines.launch

/**
 * Menu d'options pour une chanson
 * Affiche les actions disponibles : Lire, Ajouter à la file, Ajouter à une playlist, etc.
 */
@Composable
fun TrackOptionsMenu(
    track: Track,
    expanded: Boolean,
    onDismiss: () -> Unit,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val playlistVM: PlaylistVM = hiltViewModel()
    val playlists by playlistVM.playlistsWithCount.collectAsState()

    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        offset = DpOffset(x = (-16).dp, y = 0.dp),
        modifier = modifier
            .width(240.dp)
            .background(
                color = Color(0xFF282828),
                shape = RoundedCornerShape(12.dp)
            ),
        properties = PopupProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        // Lire la suite
        TrackMenuItem(
            icon = Icons.Outlined.PlayArrow,
            text = "Lire la suite",
            onClick = {
                onDismiss()
                // TODO: Implémenter la lecture de la suite
            }
        )

        // Ajouter à la file
        TrackMenuItem(
            icon = Icons.Outlined.PlaylistAdd,
            text = "Ajouter à la file",
            onClick = {
                onDismiss()
                // TODO: Implémenter l'ajout à la file
            }
        )

        // Ajouter à une Playlist
        TrackMenuItem(
            icon = Icons.Outlined.LibraryAdd,
            text = "Ajouter à une Playlist",
            onClick = {
                showPlaylistDialog = true
                onDismiss()
            }
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            thickness = 0.5.dp,
            color = Color(0xFF404040)
        )

        // Partager
        TrackMenuItem(
            icon = Icons.Outlined.Share,
            text = "Partager",
            onClick = {
                onDismiss()
                shareTrack(context, track)
            }
        )

        // Modifier
        TrackMenuItem(
            icon = Icons.Outlined.Edit,
            text = "Modifier",
            onClick = {
                onDismiss()
                // TODO: Naviguer vers l'écran de modification
            }
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            thickness = 0.5.dp,
            color = Color(0xFF404040)
        )

        // Informations
        TrackMenuItem(
            icon = Icons.Outlined.Info,
            text = "Informations",
            onClick = {
                onDismiss()
                // TODO: Afficher les informations de la chanson
            }
        )

        // Faire sonner
        TrackMenuItem(
            icon = Icons.Outlined.Notifications,
            text = "Faire sonner",
            onClick = {
                onDismiss()
                // TODO: Définir comme sonnerie
            }
        )

        // Marquer
        TrackMenuItem(
            icon = Icons.Outlined.Star,
            text = "Marquer",
            onClick = {
                onDismiss()
                // TODO: Marquer la chanson
            }
        )
    }

    // Dialog pour sélectionner une playlist
    if (showPlaylistDialog) {
        AddToPlaylistDialog(
            track = track,
            playlists = playlists,
            onDismiss = { showPlaylistDialog = false },
            onPlaylistSelected = { playlist ->
                playlistVM.addTrackToPlaylist(playlist.id, track.id)
                showPlaylistDialog = false
            }
        )
    }
}

/**
 * Item de menu personnalisé pour les options de chanson
 */
@Composable
private fun TrackMenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    DropdownMenuItem(
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = if (enabled) Color.White else Color(0xFF808080),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = if (enabled) Color.White else Color(0xFF808080)
                )
            }
        },
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
        colors = MenuDefaults.itemColors(
            textColor = Color.White,
            disabledTextColor = Color(0xFF808080)
        )
    )
}

/**
 * Dialog pour ajouter une chanson à une playlist
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

    AlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF282828)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Titre
                Text(
                    text = "Ajouter à une playlist",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Créer une nouvelle playlist
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
                            contentDescription = "Créer une playlist",
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

                // Liste des playlists existantes
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

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        playlists.take(5).forEach { playlist ->
                            Surface(
                                onClick = {
                                    onPlaylistSelected(playlist)
                                },
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

                // Bouton Annuler
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "Annuler",
                        color = Color(0xFF1DB954),
                        style = MaterialTheme.typography.labelLarge
                    )
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
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Nom de la playlist", color = Color(0xFFB3B3B3)) },
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
                                playlistVM.create(newPlaylistName)
                                // Attendre un peu que la playlist soit créée
                                kotlinx.coroutines.delay(200)
                                // Récupérer la nouvelle playlist et y ajouter la chanson
                                val newPlaylist = playlists.firstOrNull { it.name == newPlaylistName }
                                newPlaylist?.let {
                                    playlistVM.addTrackToPlaylist(it.id, track.id)
                                }
                            }
                            newPlaylistName = ""
                            showCreatePlaylist = false
                            onDismiss()
                        }
                    },
                    enabled = newPlaylistName.isNotBlank()
                ) {
                    Text(
                        text = "Créer",
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
 * Fonction pour partager une chanson
 */
private fun shareTrack(context: android.content.Context, track: Track) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Écoute cette chanson !")
        putExtra(Intent.EXTRA_TEXT, "${track.title} - ${track.artist}")
    }
    context.startActivity(Intent.createChooser(shareIntent, "Partager via"))
}