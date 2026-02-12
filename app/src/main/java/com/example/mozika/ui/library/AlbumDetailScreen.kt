package com.example.mozika.ui.library

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
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
import com.example.mozika.ui.common.StatChip
import com.example.mozika.ui.nav.navigateToTrack
import com.example.mozika.ui.player.PlayerVM
import com.example.mozika.ui.playlist.PlaylistVM
import com.example.mozika.ui.playlist.PlaylistWithCount
import com.example.mozika.ui.common.formatDuration as commonFormatDuration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    navController: NavHostController,
    albumId: String,
    viewModel: LibraryVM = hiltViewModel()
) {
    val albums by viewModel.albums.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val playerVM: PlayerVM = hiltViewModel()

    val album = remember(albumId, albums) {
        albums.find { it.id == albumId }
    }

    val albumTracks = remember(album, tracks) {
        album?.let { a ->
            tracks.filter { it.album == a.title && it.artist == a.artist }
                .sortedBy { it.title }
        } ?: emptyList()
    }

    val totalDuration = remember(albumTracks) {
        albumTracks.sumOf { it.duration?.toLong() ?: 0L }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = album?.title ?: "Album",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0x20FFFFFF), RoundedCornerShape(20.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundBlack
                )
            )
        },
        containerColor = BackgroundBlack
    ) { paddingValues ->
        if (album == null) {
            EmptyAlbumState(navController)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(BackgroundBlack),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    AlbumHeaderModern(
                        album = album,
                        trackCount = albumTracks.size,
                        totalDuration = totalDuration,
                        playerVM = playerVM,
                        navController = navController,
                        albumTracks = albumTracks
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pistes",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(${albumTracks.size})",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )
                        )
                    }

                    Divider(
                        color = Color(0xFF1A1A1A),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }

                // ✅ CORRIGÉ : Utiliser itemsIndexed pour avoir l'index
                itemsIndexed(
                    items = albumTracks,
                    key = { index, track -> "album_track_${track.id}_${index}" }
                ) { index, track ->
                    TrackItemAlbumModern(
                        track = track,
                        trackNumber = index + 1, // ✅ Passer le numéro de piste
                        navController = navController,
                        albumTitle = album.title,
                        playerVM = playerVM
                    )
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun AlbumHeaderModern(
    album: com.example.mozika.domain.model.Album,
    trackCount: Int,
    totalDuration: Long,
    playerVM: PlayerVM,
    navController: NavHostController,
    albumTracks: List<Track>
) {
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            CyanPrimary.copy(alpha = 0.3f),
                            CyanPrimary.copy(alpha = 0.1f),
                            Color(0xFF1E1E1E)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
                .drawBehind {
                    drawRect(
                        color = CyanPrimary.copy(alpha = 0.1f),
                        topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                        size = size.copy(
                            width = size.width - 8.dp.toPx(),
                            height = size.height - 8.dp.toPx()
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Album,
                contentDescription = null,
                tint = CyanPrimary.copy(alpha = 0.8f),
                modifier = Modifier.size(72.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = album.title,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                letterSpacing = (-0.3).sp
            ),
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = album.artist,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            ),
            color = CyanPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.clickable {
                navController.navigate("artist/${album.artist}")
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatChip(
                icon = Icons.Rounded.MusicNote,
                value = trackCount.toString(),
                label = "pistes"
            )

            Spacer(modifier = Modifier.width(8.dp))

            StatChip(
                icon = Icons.Rounded.AccessTime,
                value = commonFormatDuration(totalDuration.toInt()),
                label = "durée",
                isHighlighted = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CardBlack,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = "Album",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color(0xFF888888),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (albumTracks.isNotEmpty()) {
                        playerVM.loadAlbum(album.title)
                        navController.navigateToTrack(albumTracks.first().id)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanPrimary,
                    contentColor = BackgroundBlack
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Lire",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            OutlinedButton(
                onClick = {
                    if (albumTracks.isNotEmpty()) {
                        coroutineScope.launch {
                            val shuffled = albumTracks.shuffled()
                            playerVM.loadPlaylistAndPlay(
                                newPlaylist = shuffled,
                                trackId = shuffled.first().id,
                                context = PlayerVM.PlaylistContext.Album(album.title),
                                autoPlay = true
                            )
                            navController.navigateToTrack(shuffled.first().id)
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = CyanPrimary
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            CyanPrimary.copy(alpha = 0.5f),
                            CyanPrimary.copy(alpha = 0.5f)
                        )
                    )
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shuffle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Aléatoire",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Divider(
            color = Color(0xFF1A1A1A),
            thickness = 0.5.dp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun EmptyAlbumState(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Rounded.Album,
                contentDescription = null,
                tint = Color(0xFF404040),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Album non trouvé",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { navController.popBackStack() }) {
                Text("Retour", color = CyanPrimary)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackItemAlbumModern(
    track: Track,
    trackNumber: Int, // ✅ AJOUTÉ : Numéro de piste explicite
    navController: NavHostController,
    albumTitle: String,
    playerVM: PlayerVM
) {
    val context = LocalContext.current
    val playlistVM: PlaylistVM = hiltViewModel()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        onClick = {
            playerVM.loadAlbum(albumTitle)
            playerVM.load(track.id, autoPlay = true)
            navController.navigate("player/${track.id}")
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardBlack),
                contentAlignment = Alignment.Center
            ) {
                // ✅ CORRIGÉ : Utiliser trackNumber au lieu de albumTracks.indexOf
                Text(
                    text = trackNumber.toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    color = CyanPrimary.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = Color(0xFF666666),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    track.duration?.let { duration ->
                        if (duration > 0) {
                            Text(
                                text = commonFormatDuration(duration),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    color = Color(0xFF444444)
                                )
                            )
                        }
                    }
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
                    tint = Color(0xFF444444),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = CardBlack,
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF404040)) }
        ) {
            // Bottom sheet content...
        }
    }
}