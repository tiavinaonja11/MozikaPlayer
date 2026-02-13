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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
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

// 🎨 Couleurs Premium
private val CyanPrimary = Color(0xFF22D3EE)
private val CyanSecondary = Color(0xFF06B6D4)
private val CyanAlpha15 = Color(0xFF22D3EE).copy(alpha = 0.15f)
private val CyanAlpha20 = Color(0xFF22D3EE).copy(alpha = 0.20f)
private val CyanAlpha08 = Color(0xFF22D3EE).copy(alpha = 0.08f)
private val BackgroundBlack = Color(0xFF000000)
private val CardBlack = Color(0xFF0F0F0F)
private val SurfaceElevated = Color(0xFF1C1C1C)
private val TextGray = Color(0xFF999999)
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
        enter = slideInVertically(
            initialOffsetY = { it + 100 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it + 50 },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeOut(
            animationSpec = tween(200)
        )
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
            MiniPlayerContentEnhanced(
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
private fun MiniPlayerContentEnhanced(
    track: Track,
    isPlaying: Boolean,
    hasValidPlaylist: Boolean,
    isRestoredState: Boolean,
    onTrackClick: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animation de glow pulsant
    val infiniteTransition = rememberInfiniteTransition(label = "glow_pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying) 0.08f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // Glow effect background
        if (isPlaying) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .offset(y = 2.dp)
                    .blur(20.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                CyanPrimary.copy(alpha = glowAlpha),
                                CyanSecondary.copy(alpha = glowAlpha * 0.6f),
                                CyanPrimary.copy(alpha = glowAlpha)
                            )
                        )
                    )
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clickable(
                    onClick = onTrackClick,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            shape = RoundedCornerShape(24.dp),
            color = CardBlack,
            tonalElevation = 0.dp,
            shadowElevation = 16.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                SurfaceElevated.copy(alpha = 0.6f),
                                CardBlack,
                                CardBlack,
                                SurfaceElevated.copy(alpha = 0.4f)
                            )
                        )
                    )
            ) {
                // Ligne de progression animée en haut
                if (isPlaying) {
                    val progressOffset by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(3000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "progress_line"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        CyanPrimary.copy(alpha = 0.3f),
                                        CyanPrimary,
                                        CyanSecondary,
                                        CyanPrimary,
                                        CyanPrimary.copy(alpha = 0.3f),
                                        Color.Transparent
                                    ),
                                    startX = progressOffset * 1000f - 500f,
                                    endX = progressOffset * 1000f + 500f
                                )
                            )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Vignette musicale avec animation
                    EnhancedMusicThumbnail(
                        track = track,
                        isPlaying = isPlaying
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Info texte
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        val titleColor by animateColorAsState(
                            targetValue = if (isPlaying) Color.White else Color(0xFFDDDDDD),
                            animationSpec = tween(300),
                            label = "title_color"
                        )

                        EnhancedMarqueeText(
                            text = track.title,
                            isPlaying = isPlaying,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = titleColor
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val artistColor by animateColorAsState(
                            targetValue = if (isPlaying) CyanPrimary.copy(alpha = 0.9f) else TextGray,
                            animationSpec = tween(300),
                            label = "artist_color"
                        )

                        Text(
                            text = track.artist,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                color = artistColor
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Contrôles
                    EnhancedPlayerControls(
                        isPlaying = isPlaying,
                        isEnabled = hasValidPlaylist,
                        onPlayPause = onPlayPause,
                        onNext = onNext
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedMusicThumbnail(
    track: Track,
    isPlaying: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "thumbnail_anim")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier.size(56.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background glow
        if (isPlaying) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .blur(12.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                CyanPrimary.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        // Thumbnail principal
        Box(
            modifier = Modifier
                .size(56.dp)
                .scale(scale)
                .graphicsLayer { rotationZ = rotation }
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            CyanPrimary.copy(alpha = 0.8f),
                            CyanSecondary.copy(alpha = 0.6f),
                            Color(0xFF8B5CF6).copy(alpha = 0.7f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Cercle intérieur
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(CardBlack)
            )

            // Icône overlay
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    CardBlack.copy(alpha = 0.3f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedWaveformIndicator()
                }
            }
        }
    }
}

@Composable
private fun AnimatedWaveformIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")

    Row(
        modifier = Modifier.padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(4) { index ->
            val barHeight by infiniteTransition.animateFloat(
                initialValue = 4f,
                targetValue = 12f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 400 + index * 100,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$index"
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(barHeight.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(Color.White.copy(alpha = 0.9f))
            )
        }
    }
}

@Composable
private fun EnhancedPlayerControls(
    isPlaying: Boolean,
    isEnabled: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Bouton Play/Pause premium
        val playScale by animateFloatAsState(
            targetValue = if (isPlaying) 1.08f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "play_scale"
        )

        Box(
            modifier = Modifier
                .size(52.dp)
                .scale(playScale)
                .shadow(
                    elevation = if (isPlaying) 12.dp else 4.dp,
                    shape = CircleShape,
                    spotColor = if (isPlaying) CyanPrimary else Color.Transparent
                )
                .clip(CircleShape)
                .background(
                    if (isPlaying) {
                        Brush.linearGradient(
                            colors = listOf(CyanPrimary, CyanSecondary)
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(CyanAlpha20, CyanAlpha15)
                        )
                    }
                )
                .clickable(
                    onClick = onPlayPause,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(
                targetState = isPlaying,
                animationSpec = tween(250),
                label = "play_icon_crossfade"
            ) { playing ->
                Icon(
                    imageVector = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (playing) "Pause" else "Lecture",
                    tint = if (playing) BackgroundBlack else CyanPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // Bouton Suivant
        val nextAlpha by animateFloatAsState(
            targetValue = if (isEnabled) 1f else 0.3f,
            animationSpec = tween(200),
            label = "next_alpha"
        )

        Box(
            modifier = Modifier
                .size(44.dp)
                .alpha(nextAlpha)
                .clip(CircleShape)
                .background(SurfaceElevated.copy(alpha = 0.5f))
                .clickable(
                    onClick = onNext,
                    enabled = isEnabled,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = "Suivant",
                tint = if (isEnabled) TextGray else TextGrayLight,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun EnhancedMarqueeText(
    text: String,
    isPlaying: Boolean,
    style: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    var textWidth by remember { mutableIntStateOf(0) }
    var containerWidth by remember { mutableIntStateOf(0) }

    val shouldScroll = textWidth > containerWidth && containerWidth > 0 && textWidth > 0

    val scrollSpeed = if (isPlaying) 50f else 30f
    val durationMillis = remember(textWidth, scrollSpeed) {
        if (textWidth > 0 && scrollSpeed > 0) {
            ((textWidth / scrollSpeed) * 1000).toInt().coerceAtLeast(800)
        } else {
            800
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

    val spacerWidth = with(density) { 48.dp.toPx() }.toInt()

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
                    Spacer(modifier = Modifier.width(48.dp))
                    Text(
                        text = text,
                        style = style,
                        maxLines = 1,
                        modifier = Modifier.offset(x = with(density) { offset.toDp() })
                    )
                    Spacer(modifier = Modifier.width(48.dp))
                    Text(
                        text = text,
                        style = style,
                        maxLines = 1,
                        modifier = Modifier.offset(x = with(density) { offset.toDp() })
                    )
                }
            }.first().measure(
                Constraints(maxWidth = (textWidth + spacerWidth) * 3, maxHeight = constraints.maxHeight)
            )

            layout(containerWidth, placeable.height) {
                placeable.placeRelative(0, 0)
            }
        }
    }
}