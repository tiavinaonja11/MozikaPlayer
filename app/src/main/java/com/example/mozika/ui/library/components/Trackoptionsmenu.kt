package com.example.mozika.ui.components

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
import com.example.mozika.domain.model.Track
import com.example.mozika.ui.playlist.PlaylistVM
import com.example.mozika.ui.playlist.PlaylistWithCount
import com.example.mozika.ui.player.PlayerVM
import kotlinx.coroutines.launch
import java.io.File

/**
 * Menu d'options pour une chanson - VERSION BOTTOM SHEET
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackOptionsMenu(
    track: Track,
    expanded: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onTrackRemoved: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val playlistVM: PlaylistVM = hiltViewModel()
    val playerVM: PlayerVM = hiltViewModel()
    val playlists by playlistVM.playlistsWithCount.collectAsState()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (expanded) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = Color(0xFF1E1E1E),
            dragHandle = {
                BottomSheetDefaults.DragHandle(color = Color(0xFF555555))
            }
        ) {
            // Header : infos de la chanson
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
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
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = Color(0xFFB3B3B3),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(color = Color(0xFF333333), thickness = 0.5.dp)

            Spacer(modifier = Modifier.height(8.dp))

            // Options
            TrackMenuItem(
                icon = Icons.Outlined.PlayArrow,
                text = "Lire la suite",
                onClick = {
                    onDismiss()
                    playerVM.playNext(track)
                    Toast.makeText(context, "Sera lu ensuite", Toast.LENGTH_SHORT).show()
                }
            )

            TrackMenuItem(
                icon = Icons.Outlined.PlaylistAdd,
                text = "Ajouter à la Liste",
                onClick = {
                    onDismiss()
                    playerVM.addToQueue(track)
                    Toast.makeText(context, "Ajouté à la file d'attente", Toast.LENGTH_SHORT).show()
                }
            )

            TrackMenuItem(
                icon = Icons.Outlined.LibraryAdd,
                text = "Ajouter à la Playlist",
                onClick = {
                    showPlaylistDialog = true
                    onDismiss()
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                thickness = 0.5.dp,
                color = Color(0xFF333333)
            )

            TrackMenuItem(
                icon = Icons.Outlined.Share,
                text = "Partager",
                onClick = {
                    onDismiss()
                    shareTrack(context, track)
                }
            )

            TrackMenuItem(
                icon = Icons.Outlined.Edit,
                text = "Modifier",
                onClick = {
                    onDismiss()
                    showEditDialog = true
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                thickness = 0.5.dp,
                color = Color(0xFF333333)
            )

            TrackMenuItem(
                icon = Icons.Outlined.Info,
                text = "Information",
                onClick = {
                    onDismiss()
                    showInfoDialog = true
                }
            )

            TrackMenuItem(
                icon = Icons.Outlined.Notifications,
                text = "Faire comme sonnerie",
                onClick = {
                    onDismiss()
                    setAsRingtone(context, track)
                }
            )

            TrackMenuItem(
                icon = Icons.Outlined.VisibilityOff,
                text = "Masquer",
                onClick = {
                    onDismiss()
                    showDeleteConfirm = true
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Dialog pour sélectionner/créer une playlist
    if (showPlaylistDialog) {
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

    // Dialog d'édition
    if (showEditDialog) {
        TrackEditDialog(
            track = track,
            onDismiss = { showEditDialog = false },
            onSave = { newTitle, newArtist, newAlbum ->
                // TODO: Implémenter la sauvegarde dans la base de données
                Toast.makeText(
                    context,
                    "Modifications enregistrées",
                    Toast.LENGTH_SHORT
                ).show()
                showEditDialog = false
            }
        )
    }

    // Confirmation de masquage
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    text = "Masquer la chanson",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Cette chanson n'apparaîtra plus dans votre bibliothèque.",
                    color = Color(0xFFB3B3B3)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // TODO: Implémenter le masquage dans la BDD
                        Toast.makeText(
                            context,
                            "Chanson masquée",
                            Toast.LENGTH_SHORT
                        ).show()
                        showDeleteConfirm = false
                        onTrackRemoved?.invoke()
                    }
                ) {
                    Text("Masquer", color = Color(0xFFFF6B6B))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Annuler", color = Color.White)
                }
            },
            containerColor = Color(0xFF282828)
        )
    }
}

/**
 * Item de menu pour le BottomSheet
 */
@Composable
private fun TrackMenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        enabled = enabled
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
                tint = if (enabled) Color.White else Color(0xFF666666),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(18.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = if (enabled) Color.White else Color(0xFF666666)
            )
        }
    }
}

/**
 * Dialog d'édition des métadonnées
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackEditDialog(
    track: Track,
    onDismiss: () -> Unit,
    onSave: (title: String, artist: String, album: String) -> Unit
) {
    var title by remember { mutableStateOf(track.title) }
    var artist by remember { mutableStateOf(track.artist) }
    var album by remember { mutableStateOf(track.album) }

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
                    text = "Modifier la chanson",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Champ Titre
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre", color = Color(0xFFB3B3B3)) },
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

                Spacer(modifier = Modifier.height(12.dp))

                // Champ Artiste
                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artiste", color = Color(0xFFB3B3B3)) },
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

                Spacer(modifier = Modifier.height(12.dp))

                // Champ Album
                OutlinedTextField(
                    value = album,
                    onValueChange = { album = it },
                    label = { Text("Album", color = Color(0xFFB3B3B3)) },
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

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler", color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { onSave(title, artist, album) },
                        enabled = title.isNotBlank()
                    ) {
                        Text(
                            "Enregistrer",
                            color = if (title.isNotBlank()) Color(0xFF1DB954) else Color(0xFF808080)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog d'informations
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

private fun formatDuration(milliseconds: Int): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}