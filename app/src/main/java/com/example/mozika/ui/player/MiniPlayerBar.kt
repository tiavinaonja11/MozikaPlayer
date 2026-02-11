package com.example.mozika.ui.player

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mozika.domain.model.Track

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MiniPlayerBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val playerVM: PlayerVM = viewModel(
        viewModelStoreOwner = LocalContext.current as androidx.lifecycle.ViewModelStoreOwner
    )

    val playerState by playerVM.playerState.collectAsState()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val isInPlayerScreen = remember(currentRoute) {
        currentRoute?.startsWith("player/") == true
    }

    val shouldShow = playerState.currentTrack != null && !isInPlayerScreen

    AnimatedVisibility(
        visible = shouldShow,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        MiniPlayerContentMinimal(
            track = playerState.currentTrack!!,
            isPlaying = playerState.isPlaying,
            playlist = playerState.playlist,
            onTrackClick = {
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
private fun MiniPlayerContentMinimal(
    track: Track,
    isPlaying: Boolean,
    playlist: List<Track>,
    onTrackClick: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(
                onClick = onTrackClick,
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ),
        color = Color(0xFF1E1E1E),
        tonalElevation = 0.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icône - espacement standard
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF2A2A2A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = Color(0xFF666666),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Textes - taille réduite comme dans la liste
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF888888),
                        fontSize = 12.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Contrôles - espacement aéré
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier.size(36.dp),
                    enabled = playlist.size > 1
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Précédent",
                        tint = if (playlist.size > 1) Color(0xFFCCCCCC) else Color(0xFF444444),
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Lecture",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                IconButton(
                    onClick = onNext,
                    modifier = Modifier.size(36.dp),
                    enabled = playlist.size > 1
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Suivant",
                        tint = if (playlist.size > 1) Color(0xFFCCCCCC) else Color(0xFF444444),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}