package com.example.mozika.ui.player

import android.content.Intent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.mozika.R
import com.example.mozika.ui.player.components.SeekBar
import com.example.mozika.ui.player.components.PremiumAudioWaveform
import kotlin.math.abs

@Composable
fun PlayerScreen(
    navController: NavController,
    trackId: Long?
) {
    val vm: PlayerVM = hiltViewModel()
    val context = LocalContext.current
    val isFavorite by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(trackId) {
        if (trackId != null) {
            vm.load(trackId)
        }
    }

    if (trackId == null) {
        EmptyPlayerScreen(navController)
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0A0A),
                        Color(0xFF1A1A1A),
                        Color(0xFF121212)
                    ),
                    startY = 0f,
                    endY = 800f
                )
            )
    ) {
        // Album art background blur effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1DB954).copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        radius = 800f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Minimal Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0x15FFFFFF), CircleShape),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBackIos,
                        contentDescription = "Back",
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "NOW PLAYING",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF1DB954),
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = vm.currentTrack?.album ?: "Album",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF808080),
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { vm.refreshLibrary() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0x15FFFFFF), CircleShape),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Options",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Album Art with Floating Effect
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .shadow(
                        elevation = 40.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color(0xFF1DB954).copy(alpha = 0.3f),
                        spotColor = Color(0xFF1DB954).copy(alpha = 0.1f)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF2A2A2A))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_music_note),
                    contentDescription = "Album cover",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentScale = ContentScale.Fit
                )

                // Floating play button overlay
                if (!vm.isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { vm.playPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .background(Color(0xCC1DB954), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Track Info with Marquee-like animation
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = vm.currentTrack?.title ?: "Unknown Title",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = vm.currentTrack?.artist ?: "Unknown Artist",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color(0xFFB3B3B3),
                        fontSize = 16.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Favorite indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFF1DB954) else Color(0xFF808080),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "•",
                        color = Color(0xFF404040),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = vm.currentTrack?.album ?: "Album",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF808080),
                            fontSize = 13.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Waveform with glowing effect
            PremiumAudioWaveform(
                amplitudes = vm.waveform,
                progress = if (vm.duration > 0L)
                    vm.position.toFloat() / vm.duration.toFloat()
                else 0f,
                isPlaying = vm.isPlaying,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 4.dp),
                onSeek = { percent ->
                    vm.seekTo((percent * vm.duration).toLong())
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                SeekBar(
                    progress = vm.position.toFloat(),
                    duration = vm.duration.toFloat(),
                    onSeek = { percent ->
                        vm.seekTo((percent * vm.duration).toLong())
                    },
                    modifier = Modifier.height(4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(vm.position),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFB3B3B3),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = formatTime(vm.duration),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFB3B3B3),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Main Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                IconButton(
                    onClick = { vm.toggleShuffle() },
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (vm.shuffleMode) Color(0xFF1DB954).copy(alpha = 0.2f)
                            else Color.Transparent,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (vm.shuffleMode) Color(0xFF1DB954) else Color(0xFFB3B3B3),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Previous with skip 10s
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { vm.seekTo(maxOf(0L, vm.position - 10000)) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text(
                            text = "-10",
                            color = Color(0xFF808080),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    IconButton(
                        onClick = { vm.previousTrack() },
                        modifier = Modifier.size(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = "Previous",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Play/Pause Button
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(
                            elevation = 15.dp,
                            shape = CircleShape,
                            ambientColor = Color(0xFF1DB954).copy(alpha = 0.4f)
                        )
                        .background(Color(0xFF1DB954), CircleShape)
                        .clickable { vm.playPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Crossfade(targetState = vm.isPlaying) { playing ->
                        if (playing) {
                            Icon(
                                imageVector = Icons.Rounded.Pause,
                                contentDescription = "Pause",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Next with skip 10s
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { vm.nextTrack() },
                        modifier = Modifier.size(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(
                        onClick = { vm.seekTo(minOf(vm.duration, vm.position + 10000)) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text(
                            text = "+10",
                            color = Color(0xFF808080),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Repeat
                IconButton(
                    onClick = { vm.toggleRepeat() },
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (vm.repeatMode != PlayerVM.RepeatMode.OFF)
                                Color(0xFF1DB954).copy(alpha = 0.2f)
                            else Color.Transparent,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = when (vm.repeatMode) {
                            PlayerVM.RepeatMode.ONE -> Icons.Rounded.RepeatOne
                            else -> Icons.Rounded.Repeat
                        },
                        contentDescription = "Repeat",
                        tint = when (vm.repeatMode) {
                            PlayerVM.RepeatMode.OFF -> Color(0xFFB3B3B3)
                            else -> Color(0xFF1DB954)
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Bottom Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Share
                ActionButton(
                    icon = Icons.Outlined.Share,
                    label = "Share",
                    onClick = {
                        vm.currentTrack?.let { track ->
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Listening to \"${track.title}\" by ${track.artist} on Mozika"
                                )
                                putExtra(Intent.EXTRA_SUBJECT, "Share this track")
                            }
                            ContextCompat.startActivity(
                                context,
                                Intent.createChooser(shareIntent, "Share via"),
                                null
                            )
                        }
                    }
                )

                // Favorite
                ActionButton(
                    icon = if (isFavorite) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                    label = "Like",
                    isActive = isFavorite,
                    onClick = {
                        // Toggle favorite state
                        vm.toggleFavorite()
                    }
                )

                // Add to Playlist
                ActionButton(
                    icon = Icons.Rounded.PlaylistAdd,
                    label = "Playlist",
                    onClick = { vm.addToPlaylist() }
                )

                // Lyrics
                ActionButton(
                    icon = Icons.Rounded.Lyrics,
                    label = "Lyrics",
                    onClick = { /* TODO: Show lyrics */ }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Playlist indicator
            if (vm.playlist.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(1.dp)
                        .background(Color(0xFF2A2A2A))
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "UP NEXT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF808080),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        )
                    )

                    Text(
                        text = "${vm.playlist.size} tracks",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF808080),
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(70.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(
                    if (isActive) Color(0xFF1DB954).copy(alpha = 0.15f)
                    else Color(0x15FFFFFF),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color(0xFF1DB954) else Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (isActive) Color(0xFF1DB954) else Color(0xFFB3B3B3),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
fun EmptyPlayerScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0A0A),
                        Color(0xFF1A1A1A)
                    )
                )
            )
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color(0xFF1DB954).copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = "No music",
                tint = Color(0xFF1DB954),
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "No Track Selected",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Select a track from your library to start listening",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF808080),
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { navController.popBackStack() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1DB954)
            ),
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth(0.7f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Browse Library",
                fontWeight = FontWeight.Medium
            )
        }
    }
}

fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}