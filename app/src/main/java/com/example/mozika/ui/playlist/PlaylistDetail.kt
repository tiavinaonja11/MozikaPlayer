package com.example.mozika.ui.playlist

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.mozika.domain.model.Track
import com.example.mozika.ui.common.MenuOptionItem
import com.example.mozika.ui.player.PlayerVM
import com.example.mozika.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    navController: NavController,
    viewModel: PlaylistDetailVM = hiltViewModel()
) {
    val playlist by viewModel.playlist.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val allTracks by viewModel.allTracks.collectAsState()
    var showAddTracksDialog by remember { mutableStateOf(false) }
    var showActionMenu by remember { mutableStateOf(false) }
    var showRemoveTrackDialog by remember { mutableStateOf<Track?>(null) }

    // ✅ PlayerVM initialisé ici, en dehors du Scaffold
    val playerVM: PlayerVM = hiltViewModel()

    LaunchedEffect(playlistId) {
        viewModel.loadPlaylist(playlistId)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundBlack
    ) {
        Scaffold(
            topBar = {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundBlack)
                        .padding(top = 8.dp, bottom = 4.dp),
                    color = BackgroundBlack
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(
                                onClick = { navController.navigateUp() },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.ArrowBack,
                                    contentDescription = "Retour",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = playlist?.name ?: "Chargement...",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (tracks.isNotEmpty()) {
                                    Text(
                                        text = "${tracks.size} titres",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.sp,
                                            color = TextGrayLight
                                        )
                                    )
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { showAddTracksDialog = true },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Add,
                                    contentDescription = "Ajouter",
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            IconButton(
                                onClick = { showActionMenu = true },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.MoreVert,
                                    contentDescription = "Options",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            },
            // ❌ PAS DE floatingActionButton - supprimé comme demandé
            containerColor = BackgroundBlack
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundBlack)
                    .padding(paddingValues)
            ) {
                if (tracks.isEmpty()) {
                    EmptyPlaylistDetailModern(
                        playlistName = playlist?.name ?: "",
                        onAddTracksClick = { showAddTracksDialog = true }
                    )
                } else {
                    // Header avec vignette et bouton Lire tout
                    PlaylistDetailHeaderModern(
                        playlistName = playlist?.name ?: "",
                        trackCount = tracks.size,
                        totalDuration = tracks.sumOf { it.duration },
                        onPlayAll = {
                            viewModel.playAll(playerVM)  // ✅ Utilise playerVM initialisé plus haut
                        }
                    )

                    Divider(
                        color = Color(0xFF1A1A1A),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    // Liste des pistes
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        items(
                            items = tracks,
                            key = { "detail_${it.id}" }
                        ) { track ->
                            PlaylistTrackItemModern(
                                track = track,
                                position = tracks.indexOf(track) + 1,
                                onRemove = { showRemoveTrackDialog = track },
                                onPlay = {
                                    viewModel.playTrack(playerVM, track.id)  // ✅ Même ici
                                    navController.navigate("player/${track.id}")
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }

    if (showAddTracksDialog) {
        AddTracksDialogModern(
            allTracks = allTracks,
            currentTracks = tracks,
            onDismiss = { showAddTracksDialog = false },
            onAddTracks = { selectedTracks ->
                selectedTracks.forEach { track ->
                    viewModel.addTrack(playlistId, track.id)
                }
                showAddTracksDialog = false
            }
        )
    }

    if (showRemoveTrackDialog != null) {
        RemoveTrackDialogModern(
            track = showRemoveTrackDialog!!,
            onDismiss = { showRemoveTrackDialog = null },
            onConfirm = {
                viewModel.removeTrack(playlistId, showRemoveTrackDialog!!.id)
                showRemoveTrackDialog = null
            }
        )
    }

    if (showActionMenu && playlist != null) {
        PlaylistActionMenuModern(
            playlist = playlist!!,
            onDismiss = { showActionMenu = false },
            onDelete = {
                viewModel.deletePlaylist()
                showActionMenu = false
                navController.navigateUp()
            },
            onClear = {
                viewModel.clearPlaylist()
                showActionMenu = false
            }
        )
    }
}

@Composable
fun PlaylistDetailHeaderModern(
    playlistName: String,
    trackCount: Int,
    totalDuration: Int,
    onPlayAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CyanAlpha15.copy(alpha = 0.3f),
                        BackgroundBlack
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // Vignette centrée avec dégradé
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            CyanPrimary.copy(alpha = 0.4f),
                            CyanPrimary.copy(alpha = 0.1f),
                            Color(0xFF1A1A2E)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Grande icône playlist
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(CyanAlpha20),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = playlistName.take(1).uppercase(),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 48.sp,
                            color = CyanPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = playlistName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = Color.White
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Stats + Bouton Lire tout sur la même ligne
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Carte Nombre de titres
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = CardBlack
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyanAlpha15),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            "$trackCount",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            "titres",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextGrayLight
                        )
                    }
                }
            }

            // Carte Durée + Bouton Lire tout intégré
            Surface(
                modifier = Modifier.weight(1.5f),
                shape = RoundedCornerShape(12.dp),
                color = CardBlack
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyanAlpha15),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                formatTotalDuration(totalDuration),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                "durée",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextGrayLight
                            )
                        }
                    }

                    // 🔥 BOUTON LIRE TOUT ICI - À côté de la durée
                    Button(
                        onClick = onPlayAll,
                        modifier = Modifier
                            .height(40.dp)
                            .padding(start = 8.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanPrimary,
                            contentColor = BackgroundBlack
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Lire",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistTrackItemModern(
    track: Track,
    position: Int,
    onRemove: () -> Unit,
    onPlay: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(
                onClick = onPlay,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Numéro avec fond
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (position <= 3) CyanAlpha20 else CyanAlpha15
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$position",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                color = if (position <= 3) CyanPrimary else CyanPrimary.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Info piste
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = TextGrayLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = " • ${formatDuration(track.duration)}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = Color(0xFF444444)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Bouton supprimer subtil
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Retirer",
                tint = TextGrayLight,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun EmptyPlaylistDetailModern(
    onAddTracksClick: () -> Unit,
    playlistName: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            CyanAlpha20,
                            CyanAlpha15.copy(alpha = 0.5f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.PlaylistPlay,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = CyanPrimary
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = playlistName,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Playlist vide",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 18.sp,
                color = TextGrayLight
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ajoutez des titres depuis votre bibliothèque pour commencer à écouter votre musique.",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                color = TextGrayLight,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            ),
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onAddTracksClick,
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyanPrimary,
                contentColor = BackgroundBlack
            )
        ) {
            Icon(
                Icons.Rounded.Add,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                "Ajouter des titres",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { /* TODO: Explorer */ },
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(46.dp),
            shape = RoundedCornerShape(12.dp),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                width = 1.dp,
                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF2A2A2A))
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            )
        ) {
            Icon(
                Icons.Rounded.Explore,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                "Explorer",
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun AddTracksDialogModern(
    allTracks: List<Track>,
    currentTracks: List<Track>,
    onDismiss: () -> Unit,
    onAddTracks: (List<Track>) -> Unit
) {
    var selectedTracks by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var searchQuery by remember { mutableStateOf("") }

    val availableTracks = allTracks.filter { track ->
        !currentTracks.any { it.id == track.id }
    }

    val filteredTracks = if (searchQuery.isBlank()) {
        availableTracks
    } else {
        availableTracks.filter { track ->
            track.title.contains(searchQuery, ignoreCase = true) ||
                    track.artist.contains(searchQuery, ignoreCase = true) ||
                    track.album.contains(searchQuery, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxHeight(0.9f)
            .padding(horizontal = 16.dp),
        title = {
            Column {
                Text(
                    "Ajouter des titres",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${selectedTracks.size} sélectionné${if (selectedTracks.size > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        color = if (selectedTracks.isNotEmpty()) CyanPrimary else TextGrayLight
                    )
                )
            }
        },
        text = {
            Column {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = CardBlack
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = null,
                            tint = TextGrayLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontSize = 15.sp
                            ),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                Box {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            "Rechercher...",
                                            color = TextGrayLight,
                                            fontSize = 15.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Close,
                                    contentDescription = null,
                                    tint = TextGrayLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (filteredTracks.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Rounded.MusicOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = TextGrayLight
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isBlank()) {
                                "Tous les titres sont déjà dans la playlist"
                            } else {
                                "Aucun résultat trouvé"
                            },
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = TextGrayLight,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(filteredTracks, key = { it.id }) { track ->
                            TrackSelectionItemModern(
                                track = track,
                                isSelected = selectedTracks.contains(track.id),
                                onToggle = {
                                    selectedTracks = if (selectedTracks.contains(track.id)) {
                                        selectedTracks - track.id
                                    } else {
                                        selectedTracks + track.id
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        val tracksToAdd = allTracks.filter { selectedTracks.contains(it.id) }
                        onAddTracks(tracksToAdd)
                    },
                    enabled = selectedTracks.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanPrimary,
                        contentColor = BackgroundBlack,
                        disabledContainerColor = CyanAlpha15,
                        disabledContentColor = TextGrayLight
                    )
                ) {
                    Text(
                        "Ajouter ${if (selectedTracks.isNotEmpty()) "(${selectedTracks.size})" else ""}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp,
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF2A2A2A))
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text("Annuler", fontSize = 14.sp)
                }
            }
        },
        containerColor = CardBlack,
        titleContentColor = Color.White,
        textContentColor = Color.White,
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun TrackSelectionItemModern(
    track: Track,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        color = if (isSelected) CyanAlpha15 else Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isSelected) CyanPrimary else Color.Transparent
                    )
                    .then(
                        if (!isSelected) {
                            Modifier.border(
                                width = 2.dp,
                                color = TextGrayLight,
                                shape = RoundedCornerShape(6.dp)
                            )
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = null,
                        tint = BackgroundBlack,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    ),
                    color = if (isSelected) CyanPrimary else Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${track.artist} • ${track.album}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = TextGrayLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = formatDuration(track.duration),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = if (isSelected) CyanPrimary else TextGrayLight,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            )
        }
    }
}

@Composable
fun RemoveTrackDialogModern(
    track: Track,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFCF6679).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = Color(0xFFCF6679),
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                "Retirer le titre",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = Color.White
            )
        },
        text = {
            Text(
                "Voulez-vous retirer \"${track.title}\" de la playlist ?",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    color = TextGrayLight,
                    textAlign = TextAlign.Center
                )
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFCF6679),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retirer", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF2A2A2A))
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text("Annuler", fontSize = 14.sp)
            }
        },
        containerColor = CardBlack,
        titleContentColor = Color.White,
        textContentColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}

@Composable
fun PlaylistActionMenuModern(
    playlist: com.example.mozika.domain.model.Playlist,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                playlist.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        text = {
            Column {
                MenuOptionItem(
                    icon = Icons.Rounded.Edit,
                    text = "Renommer",
                    onClick = {
                        onDismiss()
                    }
                )
                MenuOptionItem(
                    icon = Icons.Rounded.ClearAll,
                    text = "Vider la playlist",
                    onClick = onClear
                )
                MenuOptionItem(
                    icon = Icons.Rounded.Delete,
                    text = "Supprimer la playlist",
                    textColor = Color(0xFFCF6679),
                    iconTint = Color(0xFFCF6679),
                    onClick = onDelete
                )
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF2A2A2A))
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextGrayLight
                )
            ) {
                Text("Fermer")
            }
        },
        containerColor = CardBlack,
        titleContentColor = Color.White,
        textContentColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}

private fun formatDuration(milliseconds: Int): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}

private fun formatTotalDuration(milliseconds: Int): String {
    val seconds = milliseconds / 1000
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) String.format("%dh %02dmin", hours, minutes) else String.format("%d min", minutes)
}