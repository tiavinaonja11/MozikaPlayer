package com.example.mozika.ui.playlist

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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.mozika.ui.theme.CyanAlpha15
import com.example.mozika.ui.theme.CyanAlpha20
import com.example.mozika.ui.theme.CyanPrimary
import com.example.mozika.ui.theme.BackgroundBlack
import com.example.mozika.ui.theme.CardBlack
import com.example.mozika.ui.theme.TextGrayLight
import com.example.mozika.ui.playlist.viewmodel.SpecialPlaylistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialPlaylistScreen(
    playlistType: String,
    navController: NavController,
    viewModel: SpecialPlaylistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(playlistType) {
        viewModel.loadSpecialPlaylist(playlistType)
    }

    // Configuration selon le type avec dégradés améliorés
    val (title, icon, accentColor, gradientColors) = when (playlistType) {
        "favorites" -> Quadruple(
            "Favoris",
            Icons.Rounded.Favorite,
            Color(0xFFFF6B6B),
            listOf(Color(0xFFFF6B6B), Color(0xFFEE5A52))
        )
        "recently_played" -> Quadruple(
            "Récemment écoutés",
            Icons.Rounded.History,
            Color(0xFF4ECDC4),
            listOf(Color(0xFF4ECDC4), Color(0xFF44A08D))
        )
        "most_played", "top_played" -> Quadruple(
            "Les plus écoutés",
            Icons.Rounded.TrendingUp,
            Color(0xFF44A08D),
            listOf(Color(0xFF44A08D), Color(0xFF2E8B7B))
        )
        else -> Quadruple(
            playlistType.replace("_", " ").replaceFirstChar { it.uppercase() },
            Icons.Rounded.MusicNote,
            CyanPrimary,
            listOf(CyanPrimary, Color(0xFF1A8A9E))
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundBlack
    ) {
        Scaffold(
            topBar = {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundBlack)
                        .padding(top = 8.dp, bottom = 4.dp),
                    color = BackgroundBlack
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.ArrowBack,
                                    contentDescription = "Retour",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 26.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = Color.White
                            )
                        }

                        // Bouton shuffle seul en haut
                        IconButton(
                            onClick = { /* TODO: shuffle */ },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Shuffle,
                                contentDescription = "Aléatoire",
                                tint = accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            },
            // ❌ PAS DE bottomBar - supprimé pour ne pas afficher la navigation
            containerColor = BackgroundBlack
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundBlack)
                    .padding(paddingValues)
            ) {
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = accentColor,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    uiState.error != null -> {
                        ErrorStateModern(
                            message = uiState.error ?: "Une erreur est survenue",
                            onRetry = { viewModel.loadSpecialPlaylist(playlistType) }
                        )
                    }
                    uiState.tracks.isEmpty() -> {
                        EmptySpecialPlaylistState(
                            icon = icon,
                            title = title,
                            accentColor = accentColor
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            // En-tête amélioré avec grande vignette
                            item {
                                SpecialPlaylistHeaderModern(
                                    title = title,
                                    icon = icon,
                                    trackCount = uiState.tracks.size,
                                    totalDuration = uiState.tracks.sumOf { it.duration },
                                    accentColor = accentColor,
                                    gradientColors = gradientColors,
                                    onPlayAll = { /* TODO */ },
                                    onShuffle = { /* TODO */ }
                                )
                            }

                            // Liste des pistes
                            items(
                                items = uiState.tracks,
                                key = { "special_${it.id}" }
                            ) { track ->
                                SpecialPlaylistTrackItemModern(
                                    track = track,
                                    position = uiState.tracks.indexOf(track) + 1,
                                    accentColor = accentColor,
                                    onClick = {
                                        navController.navigate("player/${track.id}")
                                    },
                                    onOptionsClick = { /* TODO: options */ }
                                )
                            }

                            item { Spacer(modifier = Modifier.height(24.dp)) }
                        }
                    }
                }
            }
        }
    }
}

// Data class pour retourner 4 valeurs
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun SpecialPlaylistHeaderModern(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trackCount: Int,
    totalDuration: Int,
    accentColor: Color,
    gradientColors: List<Color>,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Grande vignette avec dégradé
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            gradientColors[0].copy(alpha = 0.5f),
                            gradientColors[1].copy(alpha = 0.2f),
                            BackgroundBlack
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Grande icône dans un cercle
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = 0.3f),
                                    accentColor.copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = accentColor
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.White
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Stats sur une ligne avec bouton Lire tout intégré
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Carte Nombre de titres
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = CardBlack
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            "$trackCount",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            "titres",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextGrayLight
                        )
                    }
                }
            }

            // Carte Durée + Bouton Lire tout
            Surface(
                modifier = Modifier.weight(1.5f),
                shape = RoundedCornerShape(12.dp),
                color = CardBlack
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(accentColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                formatDuration(totalDuration),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                "durée",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextGrayLight
                            )
                        }
                    }

                    // Bouton Lire tout compact
                    Button(
                        onClick = onPlayAll,
                        modifier = Modifier
                            .height(40.dp)
                            .padding(start = 8.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Lire",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Divider(
            color = Color(0xFF1A1A1A),
            thickness = 0.5.dp
        )
    }
}

@Composable
fun SpecialPlaylistTrackItemModern(
    track: com.example.mozika.domain.model.Track,
    position: Int,
    accentColor: Color,
    onClick: () -> Unit,
    onOptionsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Numéro avec fond coloré selon position
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    when (position) {
                        1 -> accentColor.copy(alpha = 0.25f)
                        2, 3 -> accentColor.copy(alpha = 0.15f)
                        else -> CyanAlpha15
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$position",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                color = when (position) {
                    1 -> accentColor
                    2, 3 -> accentColor.copy(alpha = 0.8f)
                    else -> CyanPrimary
                }
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = TextGrayLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                track.duration?.let { duration ->
                    if (duration > 0) {
                        Text(
                            text = " • ${formatDuration(duration)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp
                            ),
                            color = Color(0xFF444444)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        IconButton(
            onClick = onOptionsClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.Rounded.MoreVert,
                contentDescription = "Options",
                tint = TextGrayLight,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun EmptySpecialPlaylistState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.2f),
                            accentColor.copy(alpha = 0.05f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = accentColor
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Cette playlist est vide pour le moment.\nAjoutez des titres depuis votre bibliothèque.",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                color = TextGrayLight,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 20.sp
            ),
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { /* TODO: navigate to library */ },
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = Color.White
            )
        ) {
            Icon(
                Icons.Rounded.LibraryMusic,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Explorer la bibliothèque",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ErrorStateModern(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFCF6679).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = Color(0xFFCF6679)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Oups !",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                color = TextGrayLight,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(44.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyanPrimary,
                contentColor = BackgroundBlack
            )
        ) {
            Icon(
                Icons.Rounded.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Réessayer",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

private fun formatDuration(milliseconds: Int): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}