package com.example.mozika.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.navigation.NavHostController
import com.example.mozika.ui.nav.navigateToTrack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    navController: NavHostController,
    albumId: String,
    viewModel: LibraryVM = hiltViewModel()
) {
    val albums by viewModel.albums.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val playerVM: com.example.mozika.ui.player.PlayerVM = hiltViewModel()

    val album = remember(albumId, albums) {
        albums.find { it.id == albumId }
    }

    // Filtrer les chansons de cet album
    val albumTracks = remember(album, tracks) {
        if (album != null) {
            tracks.filter { it.album == album.title && it.artist == album.artist }
        } else {
            emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = album?.title ?: "Album",
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
        if (album == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFF121212)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Album non trouvé",
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
                    // En-tête de l'album
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(12.dp))
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
                                Icons.Rounded.Album,
                                contentDescription = null,
                                tint = Color(0xFF1DB954),
                                modifier = Modifier.size(80.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = album.title,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp
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
                                fontSize = 18.sp
                            ),
                            color = Color(0xFF1DB954),
                            textAlign = TextAlign.Center
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
                                    text = "${albumTracks.size} pistes",
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
                                    text = "Album",
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

                        Button(
                            onClick = {
                                if (albumTracks.isNotEmpty()) {
                                    // Charger toute la playlist de l'album
                                    playerVM.loadAlbum(album.title)
                                    // Naviguer vers la première piste
                                    navController.navigateToTrack(albumTracks.first().id)
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
                            text = "Pistes (${albumTracks.size})",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                items(albumTracks) { track ->
                    // Utiliser une version spéciale de TrackItem pour les albums
                    TrackItemAlbum(
                        track = track,
                        navController = navController,
                        albumTitle = album.title,
                        playerVM = playerVM
                    )
                }
            }
        }
    }
}

// Version spéciale de TrackItem pour les albums
@Composable
fun TrackItemAlbum(
    track: com.example.mozika.domain.model.Track,
    navController: NavHostController,
    albumTitle: String,
    playerVM: com.example.mozika.ui.player.PlayerVM
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E1E1E),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        onClick = {
            playerVM.loadAlbum(albumTitle)
            playerVM.load(track.id, autoPlay = true)
            navController.navigate("player/${track.id}")
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
                onClick = {
                },
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
}

// Fonction pour formater la durée
private fun formatDuration(milliseconds: Int): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}