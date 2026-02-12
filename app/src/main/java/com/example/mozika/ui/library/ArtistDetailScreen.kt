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

// ✅ SUPPRIMÉ : Les couleurs sont importées depuis Library.kt
// Ne pas redéclarer BackgroundBlack, CardBlack, etc.

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
    val playerVM: PlayerVM = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()

    val artist = remember(artistId, artists) {
        artists.find { it.id == artistId }
    }

    val artistAlbums = remember(artist, albums) {
        artist?.let { a -> albums.filter { it.artist == a.name } } ?: emptyList()
    }

    val artistTracks = remember(artist, tracks) {
        artist?.let { a -> tracks.filter { it.artist == a.name } } ?: emptyList()
    }

    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = artist?.name ?: "Artiste",
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
                            .background(Color(0x20FFFFFF), CircleShape)
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
        if (artist == null) {
            EmptyArtistState(navController)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(BackgroundBlack),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    ArtistHeaderModern(
                        artist = artist,
                        albumCount = artistAlbums.size,
                        trackCount = artistTracks.size,
                        playerVM = playerVM,
                        navController = navController,
                        artistTracks = artistTracks
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            label = { Text("Chansons (${artistTracks.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAlpha15,
                                selectedLabelColor = CyanPrimary,
                                containerColor = CardBlack,
                                labelColor = Color(0xFF888888)
                            ),
                            border = null
                        )

                        FilterChip(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            label = { Text("Albums (${artistAlbums.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAlpha15,
                                selectedLabelColor = CyanPrimary,
                                containerColor = CardBlack,
                                labelColor = Color(0xFF888888)
                            ),
                            border = null
                        )
                    }

                    Divider(
                        color = Color(0xFF1A1A1A),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                }

                when (selectedTab) {
                    0 -> {
                        items(
                            items = artistTracks,
                            key = { track -> "artist_track_${track.id}_${track.hashCode()}" }
                        ) { track ->
                            TrackItemModern(
                                track = track,
                                isPlaying = false,
                                isCurrentTrack = false,
                                navController = navController,
                                playerVM = playerVM,
                                artistName = artist.name
                            )
                        }
                    }
                    1 -> {
                        items(
                            items = artistAlbums,
                            key = { album -> "artist_album_${album.id}_${album.hashCode()}" }
                        ) { album ->
                            AlbumItemModern(
                                album = album,
                                navController = navController
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ArtistHeaderModern(
    artist: com.example.mozika.domain.model.Artist,
    albumCount: Int,
    trackCount: Int,
    playerVM: PlayerVM,
    navController: NavHostController,
    artistTracks: List<Track>
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
                .size(160.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            CyanPrimary.copy(alpha = 0.3f),
                            CyanPrimary.copy(alpha = 0.1f),
                            Color(0xFF1E1E1E)
                        )
                    )
                )
                .drawBehind {
                    drawCircle(
                        color = CyanPrimary.copy(alpha = 0.3f),
                        radius = size.minDimension / 2,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = artist.name.take(1).uppercase(),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 56.sp
                ),
                color = CyanPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = artist.name,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                letterSpacing = (-0.5).sp
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
            StatChip(
                icon = Icons.Rounded.Album,
                value = albumCount.toString(),
                label = "albums"
            )

            Spacer(modifier = Modifier.width(8.dp))

            StatChip(
                icon = Icons.Rounded.MusicNote,
                value = trackCount.toString(),
                label = "titres",
                isHighlighted = true
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (artistTracks.isNotEmpty()) {
                    coroutineScope.launch {
                        @Suppress("DEPRECATION")
                        val tracks = playerVM.loadArtistTracks(artist.name)
                        playerVM.loadPlaylistAndPlay(
                            newPlaylist = tracks,
                            trackId = artistTracks.first().id,
                            context = PlayerVM.PlaylistContext.Artist(artist.name),
                            autoPlay = true
                        )
                        delay(50)
                        navController.navigateToTrack(artistTracks.first().id)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.85f)
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
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Lire tout",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                if (artistTracks.isNotEmpty()) {
                    coroutineScope.launch {
                        val shuffled = artistTracks.shuffled()
                        playerVM.loadPlaylistAndPlay(
                            newPlaylist = shuffled,
                            trackId = shuffled.first().id,
                            context = PlayerVM.PlaylistContext.Artist(artist.name),
                            autoPlay = true
                        )
                        navController.navigateToTrack(shuffled.first().id)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(44.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = CyanPrimary
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                width = 1.dp,
                brush = Brush.horizontalGradient(listOf(CyanPrimary.copy(alpha = 0.5f), CyanPrimary.copy(alpha = 0.5f)))
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.Shuffle,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Aléatoire",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            )
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
private fun EmptyArtistState(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Rounded.PersonOff,
                contentDescription = null,
                tint = Color(0xFF404040),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Artiste non trouvé",
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
fun TrackItemModern(
    track: Track,
    isPlaying: Boolean,
    isCurrentTrack: Boolean,
    navController: NavHostController,
    playerVM: PlayerVM,
    artistName: String
) {
    val context = LocalContext.current
    val playlistVM: PlaylistVM = hiltViewModel()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        onClick = {
            scope.launch {
                @Suppress("DEPRECATION")
                val artistTracks = playerVM.loadArtistTracks(artistName)
                playerVM.loadPlaylistAndPlay(
                    newPlaylist = artistTracks,
                    trackId = track.id,
                    context = PlayerVM.PlaylistContext.Artist(artistName),
                    autoPlay = true
                )
                delay(50)
                navController.navigate("player/${track.id}")
            }
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
                    .background(
                        if (isCurrentTrack) CyanAlpha15 else CardBlack
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isCurrentTrack && isPlaying -> PlayingBarsIndicatorSmall()
                    isCurrentTrack -> Icon(
                        imageVector = Icons.Rounded.Pause,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    else -> Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = CyanPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isCurrentTrack) FontWeight.SemiBold else FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    color = if (isCurrentTrack) CyanPrimary else Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = track.album,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = Color(0xFF666666),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    track.duration?.let { duration ->
                        if (duration > 0) {
                            Text(
                                text = " • ${commonFormatDuration(duration)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp
                                ),
                                color = Color(0xFF444444)
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

@Composable
private fun PlayingBarsIndicatorSmall(
    barCount: Int = 3,
    color: Color = CyanPrimary
) {
    Row(
        modifier = Modifier.height(12.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(barCount) { index ->
            val infiniteTransition = rememberInfiniteTransition(label = "bar$index")
            val height by infiniteTransition.animateFloat(
                initialValue = 4f,
                targetValue = 10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(300 + (index * 100), easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "height$index"
            )
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height(height.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun AlbumItemModern(
    album: com.example.mozika.domain.model.Album,
    navController: NavHostController
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(
                onClick = { navController.navigate("album/${album.id}") },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
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
            Icon(
                imageVector = Icons.Rounded.Album,
                contentDescription = null,
                tint = CyanPrimary.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = album.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${album.trackCount} pistes",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = Color(0xFF666666)
            )
        }

        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF444444),
            modifier = Modifier.size(20.dp)
        )
    }
}