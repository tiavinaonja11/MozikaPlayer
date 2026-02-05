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

@Composable
fun MiniPlayerBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val playerVM: PlayerVM = hiltViewModel()

    // ✅ Observer l'état du player via StateFlow
    val playerState by playerVM.playerState.collectAsState()

    // Observer la route actuelle
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // Vérifier si on est sur l'écran Player
    val isInPlayerScreen = remember(currentRoute) {
        currentRoute?.startsWith("player/") == true
    }

    // ✅ Afficher seulement si :
    // 1. On a une piste chargée
    // 2. On n'est PAS sur l'écran Player
    val shouldShow = playerState.currentTrack != null && !isInPlayerScreen

    // DEBUG : Log pour vérifier
    LaunchedEffect(playerState.currentTrack?.id, isInPlayerScreen) {
        println("🎵 MiniPlayerBar - Piste: ${playerState.currentTrack?.title}, isInPlayerScreen: $isInPlayerScreen, shouldShow: $shouldShow")
    }

    // ✅ Afficher le MiniPlayer
    if (shouldShow) {
        MiniPlayerContent(
            track = playerState.currentTrack!!,
            isPlaying = playerState.isPlaying,
            playlist = playerState.playlist,
            onTrackClick = {
                // ✅ Naviguer vers le player avec la piste actuelle
                navController.navigate("player/${playerState.currentTrack!!.id}")
            },
            onPrevious = {
                if (playerState.playlist.size > 1) {
                    playerVM.previousTrack()
                }
            },
            onPlayPause = {
                playerVM.playPause()
            },
            onNext = {
                if (playerState.playlist.size > 1) {
                    playerVM.nextTrack()
                }
            },
            modifier = modifier
        )
    }
}

@Composable
private fun MiniPlayerContent(
    track: com.example.mozika.domain.model.Track,
    isPlaying: Boolean,
    playlist: List<com.example.mozika.domain.model.Track>,
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
            // Miniature avec première lettre de l'artiste
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1DB954).copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = track.artist.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color(0xFF1DB954),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Infos de la piste
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFFB3B3B3),
                        fontSize = 12.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Boutons de contrôle
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