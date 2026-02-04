package com.example.mozika.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mozika.domain.model.Track
import com.example.mozika.ui.nav.navigateToTrack

@Composable
fun MiniPlayerBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    playerVM: PlayerVM = hiltViewModel()
) {
    // Utiliser le PlayerStateManager pour observer les changements
    val playerState = rememberPlayerState(playerVM)

    // Observer les StateFlow
    val currentTrack by playerState.currentTrack.collectAsState()
    val isPlaying by playerState.isPlaying.collectAsState()
    val playlist by playerState.playlist.collectAsState()
    val playlistContext by playerState.playlistContext.collectAsState()

    // Observer la route actuelle
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val isInPlayerScreen = remember(currentRoute) {
        currentRoute?.startsWith("player/") == true
    }

    // Afficher seulement si on a une piste et qu'on n'est pas sur l'écran Player
    val shouldShow = currentTrack != null && !isInPlayerScreen

    // Debug: Afficher les informations
    LaunchedEffect(currentTrack, playlistContext, playlist.size) {
        println("MiniPlayerBar - Track: ${currentTrack?.title}")
        println("MiniPlayerBar - Context: $playlistContext")
        println("MiniPlayerBar - Playlist size: ${playlist.size}")
    }

    if (shouldShow) {
        MiniPlayerContent(
            track = currentTrack!!,
            isPlaying = isPlaying,
            playlistContext = playlistContext,
            playlist = playlist,
            onTrackClick = {
                // Stocker le contexte dans une variable locale pour l'utiliser dans le when
                val context = playlistContext

                // Navigation contextuelle selon le type de playlist
                when {
                    context is PlayerVM.PlaylistContext.Album -> {
                        // Naviguer vers l'album avec la piste actuelle
                        navController.navigate("album/${context.albumId}")
                    }
                    context is PlayerVM.PlaylistContext.Artist -> {
                        // Naviguer vers l'artiste
                        navController.navigate("artist/${context.artistId}")
                    }
                    context is PlayerVM.PlaylistContext.Search -> {
                        // Si c'est une recherche, naviguer vers le player avec contexte
                        navController.navigateToTrack(currentTrack!!.id)
                    }
                    else -> {
                        // Pour AllTracks ou None, naviguer simplement vers la piste
                        navController.navigateToTrack(currentTrack!!.id)
                    }
                }
            },
            onPrevious = {
                // S'assurer que previousTrack() utilise le contexte correct
                if (playlist.isNotEmpty()) {
                    playerVM.previousTrack()
                }
            },
            onPlayPause = {
                playerVM.playPause()
            },
            onNext = {
                // S'assurer que nextTrack() utilise le contexte correct
                if (playlist.isNotEmpty()) {
                    playerVM.nextTrack()
                }
            },
            modifier = modifier
        )
    }
}

@Composable
private fun MiniPlayerContent(
    track: Track,
    isPlaying: Boolean,
    playlistContext: PlayerVM.PlaylistContext,
    playlist: List<Track>,
    onTrackClick: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .clickable(onClick = onTrackClick),
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Miniature avec icône contextuelle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when {
                            playlistContext is PlayerVM.PlaylistContext.Album ->
                                Color(0xFF1DB954).copy(alpha = 0.3f)
                            playlistContext is PlayerVM.PlaylistContext.Artist ->
                                Color(0xFF9C27B0).copy(alpha = 0.3f)
                            else -> Color(0xFF1DB954).copy(alpha = 0.3f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                val icon = when {
                    playlistContext is PlayerVM.PlaylistContext.Album -> Icons.Default.Album
                    playlistContext is PlayerVM.PlaylistContext.Artist -> Icons.Default.Person
                    else -> Icons.Default.MusicNote
                }
                Icon(
                    icon,
                    contentDescription = "Miniature",
                    tint = when {
                        playlistContext is PlayerVM.PlaylistContext.Album -> Color(0xFF1DB954)
                        playlistContext is PlayerVM.PlaylistContext.Artist -> Color(0xFF9C27B0)
                        else -> Color(0xFF1DB954)
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Infos de la piste avec contexte
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFB3B3B3)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Badge de contexte avec info de position dans la playlist
                    if (playlist.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = getPlaylistContextText(playlistContext, playlist, track),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = getContextColor(playlistContext),
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Boutons de contrôle - activés seulement si playlist n'est pas vide
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier.size(36.dp),
                    enabled = playlist.size > 1
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Précédent",
                        tint = if (playlist.size > 1) Color.White else Color(0xFF666666),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Lecture",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = onNext,
                    modifier = Modifier.size(36.dp),
                    enabled = playlist.size > 1
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Suivant",
                        tint = if (playlist.size > 1) Color.White else Color(0xFF666666),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// Fonction utilitaire pour obtenir le texte du contexte avec position
fun getPlaylistContextText(
    context: PlayerVM.PlaylistContext,
    playlist: List<Track>,
    currentTrack: Track
): String {
    val index = playlist.indexOfFirst { it.id == currentTrack.id } + 1
    val total = playlist.size

    return when {
        context is PlayerVM.PlaylistContext.Album -> "Album • $index/$total"
        context is PlayerVM.PlaylistContext.Artist -> "Artiste • $index/$total"
        context is PlayerVM.PlaylistContext.Search -> "Recherche • $index/$total"
        context is PlayerVM.PlaylistContext.AllTracks -> "$index/$total"
        else -> ""
    }
}

// Fonction utilitaire pour obtenir la couleur du contexte
fun getContextColor(context: PlayerVM.PlaylistContext): Color {
    return when {
        context is PlayerVM.PlaylistContext.Album -> Color(0xFF1DB954)
        context is PlayerVM.PlaylistContext.Artist -> Color(0xFF9C27B0)
        context is PlayerVM.PlaylistContext.Search -> Color(0xFF2196F3)
        else -> Color(0xFF808080)
    }
}