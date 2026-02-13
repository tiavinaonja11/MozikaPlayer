package com.example.mozika.ui.components

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
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

// 🎨 Couleurs Cyan
private val Cyan        = Color(0xFF22D3EE)
private val CyanDim     = Color(0xFF22D3EE).copy(alpha = 0.15f)
private val CyanDim08   = Color(0xFF22D3EE).copy(alpha = 0.08f)
private val BgSheet     = Color(0xFF0D0D0D)
private val BgCard      = Color(0xFF161616)
private val BgItem      = Color(0xFF1A1A1A)
private val DividerColor = Color(0xFF252525)
private val TextPrimary  = Color.White
private val TextSecondary = Color(0xFF888888)

/**
 * Menu d'options pour une chanson — BottomSheet thème Cyan
 */
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
    var showInfoDialog     by remember { mutableStateOf(false) }
    var showEditDialog     by remember { mutableStateOf(false) }
    var showDeleteConfirm  by remember { mutableStateOf(false) }

    if (expanded) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = BgSheet,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF333333))
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {

                // ── Header chanson ──────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(BgCard)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Cyan.copy(alpha = 0.25f), Cyan.copy(alpha = 0.08f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.MusicNote, null,
                            tint = Cyan,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold, fontSize = 15.sp
                            ),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = track.artist,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyanDim)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Rounded.GraphicEq, null,
                            tint = Cyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // ── Groupe Lecture ──────────────────────────────
                MenuGroupLabel("Lecture")
                TrackMenuItem(
                    icon = Icons.Outlined.SkipNext,
                    text = "Lire la suite",
                    onClick = {
                        onDismiss()
                        playerVM.playNext(track)
                        Toast.makeText(context, "Sera lu ensuite", Toast.LENGTH_SHORT).show()
                    }
                )
                TrackMenuItem(
                    icon = Icons.Outlined.AddToQueue,
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
                    onClick = { showPlaylistDialog = true; onDismiss() }
                )

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(
                    color = DividerColor, thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // ── Groupe Partage ──────────────────────────────
                MenuGroupLabel("Partage & Édition")
                TrackMenuItem(
                    icon = Icons.Outlined.Share,
                    text = "Partager",
                    onClick = { onDismiss(); shareTrack(context, track) }
                )
                TrackMenuItem(
                    icon = Icons.Outlined.Edit,
                    text = "Modifier",
                    onClick = { onDismiss(); showEditDialog = true }
                )

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(
                    color = DividerColor, thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // ── Groupe Autres ───────────────────────────────
                MenuGroupLabel("Autres")
                TrackMenuItem(
                    icon = Icons.Outlined.Info,
                    text = "Information",
                    onClick = { onDismiss(); showInfoDialog = true }
                )
                TrackMenuItem(
                    icon = Icons.Outlined.NotificationsActive,
                    text = "Faire comme sonnerie",
                    onClick = { onDismiss(); setAsRingtone(context, track) }
                )
                TrackMenuItem(
                    icon = Icons.Outlined.VisibilityOff,
                    text = "Masquer",
                    tint = Color(0xFFFF6B6B),
                    onClick = { onDismiss(); showDeleteConfirm = true }
                )
            }
        }
    }

    // ── Dialogs ─────────────────────────────────────────────

    if (showPlaylistDialog) {
        AddToPlaylistDialog(
            track = track,
            playlists = playlists,
            onDismiss = { showPlaylistDialog = false },
            onPlaylistSelected = { playlist ->
                scope.launch {
                    val isAlreadyIn = playlistVM.isTrackInPlaylist(playlist.id, track.id)
                    if (isAlreadyIn) {
                        Toast.makeText(context, "Déjà dans ${playlist.name}", Toast.LENGTH_SHORT).show()
                    } else {
                        playlistVM.addTrackToPlaylist(playlist.id, track.id)
                        Toast.makeText(context, "Ajouté à ${playlist.name}", Toast.LENGTH_SHORT).show()
                    }
                }
                showPlaylistDialog = false
            }
        )
    }

    if (showInfoDialog) {
        TrackInfoDialog(track = track, onDismiss = { showInfoDialog = false })
    }

    if (showEditDialog) {
        TrackEditDialog(
            track = track,
            onDismiss = { showEditDialog = false },
            onSave = { _, _, _ ->
                Toast.makeText(context, "Modifications enregistrées", Toast.LENGTH_SHORT).show()
                showEditDialog = false
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Masquer la chanson", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Cette chanson n'apparaîtra plus dans votre bibliothèque.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    Toast.makeText(context, "Chanson masquée", Toast.LENGTH_SHORT).show()
                    showDeleteConfirm = false
                    onTrackRemoved?.invoke()
                }) { Text("Masquer", color = Color(0xFFFF6B6B)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Annuler", color = Cyan) }
            },
            containerColor = BgCard
        )
    }
}

// ── Composants privés ────────────────────────────────────────────────────

@Composable
private fun MenuGroupLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        ),
        color = Cyan.copy(alpha = 0.6f),
        modifier = Modifier.padding(start = 20.dp, bottom = 4.dp)
    )
}

@Composable
private fun TrackMenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    tint: Color = TextPrimary,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (tint == TextPrimary) CyanDim08 else tint.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = if (enabled) {
                        if (tint == TextPrimary) Cyan.copy(alpha = 0.85f) else tint
                    } else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = if (enabled) tint else TextSecondary
            )
        }
    }
}

// ── Dialogs ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackEditDialog(
    track: Track,
    onDismiss: () -> Unit,
    onSave: (title: String, artist: String, album: String) -> Unit
) {
    var title  by remember { mutableStateOf(track.title) }
    var artist by remember { mutableStateOf(track.artist) }
    var album  by remember { mutableStateOf(track.album) }

    AlertDialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = BgCard) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyanDim),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Edit, null, tint = Cyan, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Modifier la chanson",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))

                val fields = listOf(
                    Triple("Titre", title) { v: String -> title = v },
                    Triple("Artiste", artist) { v: String -> artist = v },
                    Triple("Album", album) { v: String -> album = v }
                )
                fields.forEach { (label, value, onChange) ->
                    OutlinedTextField(
                        value = value,
                        onValueChange = onChange,
                        label = { Text(label, color = TextSecondary, fontSize = 13.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = Cyan,
                            unfocusedBorderColor = Color(0xFF333333),
                            cursorColor = Cyan,
                            focusedLabelColor = Cyan
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Annuler", color = TextSecondary) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(title, artist, album) },
                        enabled = title.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Enregistrer", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackInfoDialog(track: Track, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = BgCard) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyanDim),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Info, null, tint = Cyan, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Informations",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))

                val rows = listOf(
                    "Titre" to track.title,
                    "Artiste" to track.artist,
                    "Album" to track.album,
                    "Durée" to formatDuration(track.duration),
                    "Chemin" to track.data
                )
                rows.forEachIndexed { index, (label, value) ->
                    InfoRow(label, value)
                    if (index < rows.lastIndex) {
                        HorizontalDivider(
                            color = DividerColor, thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanDim, contentColor = Cyan),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Fermer", fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, letterSpacing = 0.8.sp),
            color = Cyan.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color = TextPrimary
        )
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
    var newPlaylistName    by remember { mutableStateOf("") }
    val scope   = rememberCoroutineScope()
    val context = LocalContext.current

    AlertDialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp),
            shape = RoundedCornerShape(20.dp),
            color = BgCard
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyanDim),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.LibraryAdd, null, tint = Cyan, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Ajouter à une playlist",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    onClick = { showCreatePlaylist = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CyanDim
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Add, null, tint = Cyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Créer une nouvelle playlist",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold, fontSize = 14.sp
                            ),
                            color = Cyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (playlists.isNotEmpty()) {
                    Text(
                        "MES PLAYLISTS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold
                        ),
                        color = Cyan.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(playlists) { playlist ->
                            Surface(
                                onClick = { onPlaylistSelected(playlist) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = BgItem
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(CyanDim08),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Rounded.PlaylistPlay, null,
                                            tint = Cyan.copy(alpha = 0.7f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            playlist.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 14.sp, fontWeight = FontWeight.Medium
                                            ),
                                            color = TextPrimary, maxLines = 1
                                        )
                                        Text(
                                            "${playlist.songCount} chansons",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                            color = TextSecondary
                                        )
                                    }
                                    Icon(
                                        Icons.Rounded.ChevronRight, null,
                                        tint = Cyan.copy(alpha = 0.4f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Annuler", color = TextSecondary)
                }
            }
        }
    }

    if (showCreatePlaylist) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylist = false },
            title = { Text("Nouvelle playlist", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Nom", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = Cyan,
                        unfocusedBorderColor = Color(0xFF333333),
                        cursorColor = Cyan,
                        focusedLabelColor = Cyan
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            scope.launch {
                                val id = playlistVM.create(newPlaylistName)
                                kotlinx.coroutines.delay(200)
                                playlistVM.addTrackToPlaylist(id, track.id)
                                Toast.makeText(context, "Créé et ajouté", Toast.LENGTH_SHORT).show()
                            }
                            newPlaylistName = ""
                            showCreatePlaylist = false
                            onDismiss()
                        }
                    },
                    enabled = newPlaylistName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Créer", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylist = false }) {
                    Text("Annuler", color = TextSecondary)
                }
            },
            containerColor = BgCard
        )
    }
}

// ── Fonctions utilitaires ────────────────────────────────────────────────

private fun shareTrack(context: android.content.Context, track: Track) {
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Écoute cette chanson !")
        putExtra(Intent.EXTRA_TEXT, "${track.title} - ${track.artist}\nAlbum: ${track.album}")
    }
    context.startActivity(Intent.createChooser(intent, "Partager via"))
}

private fun setAsRingtone(context: android.content.Context, track: Track) {
    try {
        val file = File(track.data)
        if (!file.exists()) {
            Toast.makeText(context, "Fichier introuvable", Toast.LENGTH_SHORT).show()
            return
        }
        if (!Settings.System.canWrite(context)) {
            Toast.makeText(context, "Permission requise", Toast.LENGTH_LONG).show()
            context.startActivity(
                Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
            )
            return
        }
        RingtoneManager.setActualDefaultRingtoneUri(
            context, RingtoneManager.TYPE_RINGTONE, Uri.fromFile(file)
        )
        Toast.makeText(context, "Sonnerie définie", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun formatDuration(milliseconds: Int): String {
    val seconds = milliseconds / 1000
    return String.format("%d:%02d", seconds / 60, seconds % 60)
}