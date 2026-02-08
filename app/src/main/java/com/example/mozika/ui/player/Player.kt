package com.example.mozika.ui.player

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mozika.R
import com.example.mozika.ui.player.components.SeekBar
import com.example.mozika.ui.player.components.PremiumAudioWaveform
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(
    navController: NavController,
    trackId: Long?
) {
    //val vm: PlayerVM = hiltViewModel()
    val context = LocalContext.current // AJOUTÉ ICI

    val vm: PlayerVM = viewModel(
        viewModelStoreOwner = LocalContext.current as androidx.lifecycle.ViewModelStoreOwner
    )

    // Utiliser une clé dérivée pour éviter les rechargements
    val shouldLoadTrack by remember(trackId) {
        derivedStateOf {
            trackId != null && vm.currentTrack?.id != trackId
        }
    }

    LaunchedEffect(shouldLoadTrack) {
        if (shouldLoadTrack && trackId != null) {
            // Petit délai pour laisser l'UI s'initialiser
            delay(50)
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
                    listOf(
                        Color(0xFF121212),
                        Color(0xFF050505)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // HEADER compact
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        // Retourner en arrière SANS arrêter la musique
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0x20FFFFFF), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Retour",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "Now Playing",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                )

                IconButton(
                    onClick = { vm.refreshLibrary() },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0x20FFFFFF), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualiser",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ALBUM CARD compacte
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E1E)
                ),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_music_note),
                        contentDescription = "Album cover",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // INFOS TITRE / ARTISTE compact
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = vm.currentTrack?.title ?: "Titre inconnu",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp // Plus petit
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = vm.currentTrack?.artist ?: "Artiste inconnue",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color(0xFFB3B3B3),
                        fontSize = 14.sp // Plus petit
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = vm.currentTrack?.album ?: "Album inconnu",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF808080),
                        fontSize = 12.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // WAVEFORM compacte
            PremiumAudioWaveform(
                amplitudes = vm.waveform,
                progress = if (vm.duration > 0L)
                    vm.position.toFloat() / vm.duration.toFloat()
                else 0f,
                isPlaying = vm.isPlaying,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                onSeek = { percent ->
                    vm.seekTo((percent * vm.duration).toLong())
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // SEEK BAR compacte - CORRIGÉ
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Box(modifier = Modifier.height(20.dp)) {
                    SeekBar(
                        progress = vm.position.toFloat(),
                        duration = vm.duration.toFloat(),
                        onSeek = { percent ->
                            vm.seekTo((percent * vm.duration).toLong())
                        }
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(vm.position),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFB3B3B3),
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = formatTime(vm.duration),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFB3B3B3),
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // CONTROLES PRINCIPAUX (PLAY/PAUSE/PREV/NEXT) plus compacts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Contrôles secondaires à gauche
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = { vm.toggleShuffle() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Lecture aléatoire",
                            tint = if (vm.shuffleMode) Color(0xFF1DB954) else Color(0xFFB3B3B3),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Shuffle",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (vm.shuffleMode) Color(0xFF1DB954) else Color(0xFFB3B3B3),
                            fontSize = 8.sp
                        )
                    )
                }

                // Previous
                IconButton(
                    onClick = { vm.previousTrack() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Piste précédente",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Play/Pause (centré)
                Box(
                    modifier = Modifier
                        .size(60.dp) // Plus petit
                        .background(Color.White, CircleShape)
                        .clickable { vm.playPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (vm.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (vm.isPlaying) "Pause" else "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Next
                IconButton(
                    onClick = { vm.nextTrack() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Piste suivante",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Contrôles secondaires à droite
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = { vm.toggleRepeat() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = when (vm.repeatMode) {
                                PlayerVM.RepeatMode.OFF -> Icons.Default.Repeat
                                PlayerVM.RepeatMode.ALL -> Icons.Default.Repeat
                                PlayerVM.RepeatMode.ONE -> Icons.Default.RepeatOne
                            },
                            contentDescription = "Mode répétition",
                            tint = when (vm.repeatMode) {
                                PlayerVM.RepeatMode.OFF -> Color(0xFFB3B3B3)
                                else -> Color(0xFF1DB954)
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = when (vm.repeatMode) {
                            PlayerVM.RepeatMode.OFF -> "Off"
                            PlayerVM.RepeatMode.ALL -> "All"
                            PlayerVM.RepeatMode.ONE -> "One"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = when (vm.repeatMode) {
                                PlayerVM.RepeatMode.OFF -> Color(0xFFB3B3B3)
                                else -> Color(0xFF1DB954)
                            },
                            fontSize = 8.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // BARRE D'ACTIONS finale (en bas)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Partager
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = {
                            vm.currentTrack?.let { track ->
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Écoute \"${track.title}\" par ${track.artist} sur Mozika"
                                    )
                                    putExtra(Intent.EXTRA_SUBJECT, "Partager une musique")
                                }
                                ContextCompat.startActivity(
                                    context, // CORRIGÉ : Utilisation de la variable context
                                    Intent.createChooser(shareIntent, "Partager via"),
                                    null
                                )
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Partager",
                            tint = Color(0xFFB3B3B3),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Share",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFB3B3B3),
                            fontSize = 8.sp
                        )
                    )
                }

                // Favoris
                var isFavorite by remember { mutableStateOf(false) }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = {
                            isFavorite = !isFavorite
                            vm.toggleFavorite()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favoris",
                            tint = if (isFavorite) Color(0xFF1DB954) else Color(0xFFB3B3B3),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Favorite",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isFavorite) Color(0xFF1DB954) else Color(0xFFB3B3B3),
                            fontSize = 8.sp
                        )
                    )
                }

                // Ajouter à playlist
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = { vm.addToPlaylist() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlaylistAdd,
                            contentDescription = "Ajouter à la liste",
                            tint = Color(0xFFB3B3B3),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Add to",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFB3B3B3),
                            fontSize = 8.sp
                        )
                    )
                }

                // Menu
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = { /* TODO: options supplémentaires */ },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Plus d'options",
                            tint = Color(0xFFB3B3B3),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "More",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFB3B3B3),
                            fontSize = 8.sp
                        )
                    )
                }
            }

            // Indicateur de playlist (petit et discret)
            if (vm.playlist.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• ${vm.playlist.size} tracks •",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF505050),
                        fontSize = 9.sp
                    )
                )
            }
        }
    }
}

@Composable
fun EmptyPlayerScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = "Aucune musique",
            tint = Color(0xFF404040),
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Aucune piste sélectionnée",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sélectionnez une piste depuis votre bibliothèque",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF808080)
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.popBackStack() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1DB954)
            ),
            modifier = Modifier.width(220.dp)
        ) {
            Text("Retour à la bibliothèque")
        }
    }
}

fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}