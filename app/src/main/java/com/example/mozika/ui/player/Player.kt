package com.example.mozika.ui.player

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.navigation.NavController
import com.example.mozika.R
import com.example.mozika.ui.player.components.Waveform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures

@Composable
fun PlayerScreen(
    navController: NavController,
    trackId: Long?,
) {
    val vm: PlayerVM = hiltViewModel()
    val context = LocalContext.current

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
            .background(Color(0xFF121212))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header avec bouton retour - HAUTEUR FIXE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = Color(0x20FFFFFF),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Retour",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = "Now Playing",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )

                IconButton(
                    onClick = { vm.refreshLibrary() },
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = Color(0x20FFFFFF),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualiser",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Image de la piste (album/cover) - HAUTEUR FIXE
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 40.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF2A2A2A))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_music_note),
                    contentDescription = "Album cover",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(40.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFFB3B3B3))
                )
            }

            // Informations de la piste - HAUTEUR FIXE
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = vm.currentTrack?.title ?: "Titre inconnu",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = vm.currentTrack?.artist ?: "Artiste inconnu",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color(0xFFB3B3B3),
                        fontSize = 16.sp
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = vm.currentTrack?.album ?: "Album inconnu",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF808080),
                        fontSize = 14.sp
                    )
                )
            }

            // Waveform avec barre de progression - HAUTEUR FIXE
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                // Waveform - VISIBLE MAINTENANT
                Waveform(
                    amplitudes = vm.waveform,
                    position = vm.position.toFloat(),
                    duration = vm.duration.toFloat(),
                    onSeek = { percent ->
                        vm.seekTo((percent * vm.duration).toLong())
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Temps écoulé/total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(vm.position),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFB3B3B3),
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = formatTime(vm.duration),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFB3B3B3),
                            fontSize = 12.sp
                        )
                    )
                }
            }

            // CONTROLES PRINCIPAUX DE LECTURE - HAUTEUR FIXE
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                // Contrôles Shuffle/Repeat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle
                    IconButton(
                        onClick = { vm.toggleShuffle() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Lecture aléatoire",
                            tint = if (vm.shuffleMode) Color(0xFF1DB954) else Color(0xFFB3B3B3),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Timer écoulé
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0x20FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = formatTime(vm.position),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Timer/Alarme
                    IconButton(
                        onClick = { /* TODO: Implémenter la fonction timer */ },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Timer",
                            tint = Color(0xFFB3B3B3),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Repeat
                    IconButton(
                        onClick = { vm.toggleRepeat() },
                        modifier = Modifier.size(36.dp)
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
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // BOUTONS PLAY/PAUSE, NEXT, PREVIOUS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bouton précédent
                    IconButton(
                        onClick = { vm.previousTrack() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Piste précédente",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Bouton play/pause principal - IMPORTANT: CE BOUTON DOIT ÊTRE VISIBLE
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.White, CircleShape)
                            .clickable {
                                println("Play/Pause cliqué")
                                vm.playPause()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (vm.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (vm.isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Bouton suivant
                    IconButton(
                        onClick = { vm.nextTrack() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Piste suivante",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // BOUTONS SUPPLÉMENTAIRES
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Partager
                    IconButton(
                        onClick = {
                            println("Partager cliqué")
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
                                    context,
                                    Intent.createChooser(shareIntent, "Partager via"),
                                    null
                                )
                            }
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Partager",
                            tint = Color(0xFFB3B3B3),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Favoris
                    var isFavorite by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = {
                            println("Favoris cliqué")
                            isFavorite = !isFavorite
                            vm.toggleFavorite()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favoris",
                            tint = if (isFavorite) Color(0xFF1DB954) else Color(0xFFB3B3B3),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Ajouter à playlist
                    IconButton(
                        onClick = {
                            println("Ajouter à playlist cliqué")
                            vm.addToPlaylist()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlaylistAdd,
                            contentDescription = "Ajouter à la liste",
                            tint = Color(0xFFB3B3B3),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Menu (3 points verticaux)
                    IconButton(
                        onClick = {
                            println("Menu optionnel cliqué")
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Plus d'options",
                            tint = Color(0xFFB3B3B3),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Indicateur de playlist
            if (vm.playlist.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${vm.playlist.size} pistes dans la playlist",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF808080),
                        fontSize = 12.sp
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
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.popBackStack() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1DB954)
            ),
            modifier = Modifier.width(200.dp)
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