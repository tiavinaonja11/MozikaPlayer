package com.example.mozika.ui.player

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
private val SurfaceDark = Color(0xFF0A0A0A)
private val GradientStart = Color(0xFF1E1E1E)
private val GradientEnd = Color(0xFF252525)

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
            MiniPlayerContent(
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
private fun MiniPlayerContent(
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
            .height(68.dp)
            .clickable(
                onClick = onTrackClick,
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ),
        color = SurfaceDark,
        tonalElevation = 0.dp,
        shadowElevation = 12.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(GradientStart, GradientEnd, GradientStart)
                    )
                )
                .drawBehind {
                    drawLine(
                        color = if (isPlaying) CyanPrimary else Color(0xFF404040),
                        start = Offset(0f, 0f),
                        end = Offset(size.width * 0.3f, 0f),
                        strokeWidth = 2.dp.toPx()
                    )
                }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedMusicIcon(isPlaying = isPlaying)

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // ✅ CORRECTION : Animation marquee avec protection contre division par zéro
                    MarqueeText(
                        text = track.title,
                        isPlaying = isPlaying,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isPlaying) CyanPrimary else Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isRestoredState) "Appuyez pour reprendre" else track.artist,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFAAAAAA),
                            fontSize = 12.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                PlayerControls(
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
private fun AnimatedMusicIcon(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "music_icon")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPlaying) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = if (isPlaying) 1f else 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isPlaying)
                    CyanPrimary.copy(alpha = 0.15f)
                else
                    Color(0xFF2A2A2A)
            )
            .graphicsLayer {
                if (isPlaying) {
                    scaleX = scale
                    scaleY = scale
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (isPlaying) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                CyanPrimary.copy(alpha = alpha * 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        Icon(
            imageVector = Icons.Rounded.MusicNote,
            contentDescription = null,
            tint = if (isPlaying) CyanPrimary else Color(0xFF666666),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun PlayerControls(
    isPlaying: Boolean,
    isEnabled: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilledIconButton(
            onClick = onPlayPause,
            modifier = Modifier.size(44.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (isPlaying) CyanPrimary.copy(alpha = 0.2f) else Color(0xFF333333),
                contentColor = if (isPlaying) CyanPrimary else Color.White
            )
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Lecture",
                modifier = Modifier.size(28.dp)
            )
        }

        IconButton(
            onClick = onNext,
            modifier = Modifier.size(40.dp),
            enabled = isEnabled
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = "Suivant",
                tint = if (isEnabled) Color(0xFFCCCCCC) else Color(0xFF444444),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * ✅ CORRECTION : Texte avec animation de défilement sécurisée
 */
@Composable
private fun MarqueeText(
    text: String,
    isPlaying: Boolean,
    style: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    var textWidth by remember { mutableIntStateOf(0) }
    var containerWidth by remember { mutableIntStateOf(0) }

    // ✅ PROTECTION : Vitesse minimum pour éviter division par zéro
    val scrollSpeed = if (isPlaying) 40f else 25f

    // ✅ PROTECTION : Ne pas animer si pas assez de place ou pas en lecture
    val shouldScroll = textWidth > containerWidth && containerWidth > 0 && isPlaying && textWidth > 0

    // ✅ CORRECTION : Durée minimum de 1000ms pour éviter division par zéro
    val durationMillis = remember(textWidth, scrollSpeed) {
        if (textWidth > 0 && scrollSpeed > 0) {
            ((textWidth / scrollSpeed) * 1000).toInt().coerceAtLeast(1000)
        } else {
            1000 // Valeur par défaut sécurisée
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "marquee")

    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (shouldScroll) -textWidth.toFloat() else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing
            ),
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