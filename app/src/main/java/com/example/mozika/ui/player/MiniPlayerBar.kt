package com.example.mozika.ui.player

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mozika.data.datastore.PlayerState
import com.example.mozika.domain.model.Track

// Couleurs du thème
private val CyanPrimary = Color(0xFF22D3EE)
private val CyanAlpha15 = Color(0xFF22D3EE).copy(alpha = 0.15f)
private val CyanAlpha20 = Color(0xFF22D3EE).copy(alpha = 0.20f)
private val BackgroundBlack = Color(0xFF000000)
private val CardBlack = Color(0xFF141414)
private val SurfaceElevated = Color(0xFF1A1A1A)
private val TextGray = Color(0xFF888888)
private val TextGrayLight = Color(0xFF666666)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MiniPlayerBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val playerVM: PlayerVM = hiltViewModel()

    val playerState by playerVM.playerState.collectAsState()
    val savedState by playerVM.savedPlayerState.collectAsState(initial = PlayerState.empty())

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val isInPlayerScreen = currentRoute?.startsWith("player/") == true

    val hasCurrentTrack = playerState.currentTrack != null
    val hasSavedTrack = savedState.trackId > 0

    val shouldShow = (hasCurrentTrack || hasSavedTrack) && !isInPlayerScreen

    AnimatedVisibility(
        visible = shouldShow,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(animationSpec = tween(300))
    ) {
        val trackToShow = playerState.currentTrack ?: if (hasSavedTrack) {
            Track(
                id = savedState.trackId,
                title = "Continuer la lecture",
                artist = "Appuyez pour reprendre",
                album = "",
                duration = 0,
                dateAdded = 0,
                data = ""
            )
        } else null

        trackToShow?.let { track ->
            MiniPlayerContentModern(
                track = track,
                isPlaying = playerState.isPlaying,
                hasValidPlaylist = playerState.playlist.isNotEmpty(),
                isRestoredState = !hasCurrentTrack && hasSavedTrack,
                onTrackClick = {
                    if (hasCurrentTrack) {
                        navController.navigate("player/${track.id}")
                    } else {
                        playerVM.restoreAndPlay()
                        navController.navigate("player/${track.id}")
                    }
                },
                onPlayPause = {
                    if (hasCurrentTrack) {
                        playerVM.playPause()
                    } else {
                        playerVM.restoreAndPlay()
                    }
                },
                onNext = { playerVM.nextTrack() },
                modifier = modifier
            )
        }
    }
}

@Composable
private fun MiniPlayerContentModern(
    track: Track,
    isPlaying: Boolean,
    hasValidPlaylist: Boolean,
    isRestoredState: Boolean,
    onTrackClick: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable(
                onClick = onTrackClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        shape = RoundedCornerShape(16.dp),
        color = CardBlack,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            CardBlack,
                            SurfaceElevated,
                            CardBlack
                        )
                    )
                )
                .drawBehind {
                    // Ligne de progression subtile en haut
                    if (isPlaying) {
                        drawLine(
                            color = CyanPrimary.copy(alpha = 0.6f),
                            start = Offset(0f, 0f),
                            end = Offset(size.width * 0.4f, 0f),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vignette avec animation moderne
                ModernMusicThumbnail(
                    track = track,
                    isPlaying = isPlaying
                )

                Spacer(modifier = Modifier.width(14.dp))

                // Info texte avec marquee
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    ModernMarqueeText(
                        text = track.title,
                        isPlaying = isPlaying,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isPlaying) CyanPrimary else Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = if (isRestoredState) "Appuyez pour reprendre" else track.artist,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextGrayLight,
                            fontSize = 12.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Contrôles modernisés
                ModernPlayerControls(
                    isPlaying = isPlaying,
                    isEnabled = hasValidPlaylist && !isRestoredState,
                    onPlayPause = onPlayPause,
                    onNext = onNext
                )
            }
        }
    }
}

@Composable
private fun ModernMusicThumbnail(
    track: Track,
    isPlaying: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "thumbnail_pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPlaying) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isPlaying) CyanAlpha15 else Color(0xFF252525)
            )
            .graphicsLayer {
                if (isPlaying) {
                    scaleX = scale
                    scaleY = scale
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Effet de glow quand lecture active
        if (isPlaying) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                CyanPrimary.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Icône ou première lettre
        if (track.title.isNotEmpty() && track.title != "Continuer la lecture") {
            Text(
                text = track.title.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = if (isPlaying) CyanPrimary else TextGray
                )
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = if (isPlaying) CyanPrimary else TextGray,
                modifier = Modifier.size(24.dp)
            )
        }

        // Indicateur de lecture visuel
        if (isPlaying) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
                    .height(8.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                repeat(3) { index ->
                    val barHeight by infiniteTransition.animateFloat(
                        initialValue = 3f,
                        targetValue = 8f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(400 + index * 100, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "bar_$index"
                    )
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(barHeight.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(CyanPrimary)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernPlayerControls(
    isPlaying: Boolean,
    isEnabled: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Bouton Play/Pause moderne
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (isPlaying) CyanPrimary else CyanAlpha15
                )
                .clickable(
                    onClick = onPlayPause,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Lecture",
                tint = if (isPlaying) BackgroundBlack else CyanPrimary,
                modifier = Modifier.size(26.dp)
            )
        }

        // Bouton Suivant subtil
        IconButton(
            onClick = onNext,
            modifier = Modifier.size(36.dp),
            enabled = isEnabled
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = "Suivant",
                tint = if (isEnabled) TextGray else TextGray.copy(alpha = 0.3f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * Texte avec animation de défilement sécurisée
 */
@Composable
private fun ModernMarqueeText(
    text: String,
    isPlaying: Boolean,
    style: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    var textWidth by remember { mutableIntStateOf(0) }
    var containerWidth by remember { mutableIntStateOf(0) }

    val scrollSpeed = if (isPlaying) 40f else 25f
    val shouldScroll = textWidth > containerWidth && containerWidth > 0 && isPlaying && textWidth > 0
    val durationMillis = remember(textWidth, scrollSpeed) {
        if (textWidth > 0 && scrollSpeed > 0) {
            ((textWidth / scrollSpeed) * 1000).toInt().coerceAtLeast(1000)
        } else {
            1000
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "marquee")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (shouldScroll) -textWidth.toFloat() else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "marquee_offset"
    )

    val spacerWidth = with(density) { 40.dp.toPx() }.toInt()

    SubcomposeLayout(modifier = modifier.fillMaxWidth()) { constraints ->
        val measurePlaceable = subcompose("measure_${text.hashCode()}") {
            Text(text = text, style = style, maxLines = 1)
        }.first().measure(Constraints())

        textWidth = measurePlaceable.width
        containerWidth = constraints.maxWidth

        if (!shouldScroll || textWidth <= 0) {
            val placeable = subcompose("static_${text.hashCode()}") {
                Text(
                    text = text,
                    style = style,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }.first().measure(constraints)

            layout(containerWidth, placeable.height) {
                placeable.placeRelative(0, 0)
            }
        } else {
            val placeable = subcompose("scrolling_${text.hashCode()}") {
                Row {
                    Text(
                        text = text,
                        style = style,
                        maxLines = 1,
                        modifier = Modifier.offset(x = with(density) { offset.toDp() })
                    )
                    Spacer(modifier = Modifier.width(40.dp))
                    Text(
                        text = text,
                        style = style,
                        maxLines = 1,
                        modifier = Modifier.offset(x = with(density) { offset.toDp() })
                    )
                }
            }.first().measure(
                Constraints(maxWidth = (textWidth + spacerWidth) * 2, maxHeight = constraints.maxHeight)
            )

            layout(containerWidth, placeable.height) {
                placeable.placeRelative(0, 0)
            }
        }
    }
}