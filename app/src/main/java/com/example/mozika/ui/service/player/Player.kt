package com.example.mozika.ui.service.player

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mozika.domain.model.Track
import com.example.mozika.ui.player.PlayerVM
import com.example.mozika.ui.service.player.components.Controls
import com.example.mozika.ui.service.player.components.SeekBar
import com.example.mozika.ui.service.player.components.Waveform

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    navController: NavController,
    vm: PlayerVM = viewModel()
) {
    val entry by navController.currentBackStackEntryAsState()
    val track = entry?.savedStateHandle?.get<Track>("track")

    if (track == null) {
        Text("Aucune piste")
        return
    }

    LaunchedEffect(track) { vm.load(track.id) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = track.title,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "${track.artist} • ${track.album}",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.weight(1f))

        Waveform(
            amplitudes = vm.waveform,
            position = vm.position.toFloat(),
            duration = vm.duration.toFloat(),
            onSeek = { percent -> vm.seekTo((percent * vm.duration).toLong()) }
        )

        SeekBar(
            progress = vm.position.toFloat(),
            duration = vm.duration.toFloat(),
            onSeek = { percent -> vm.seekTo((percent * vm.duration).toLong()) }
        )

        Controls(isPlaying = vm.isPlaying, onPlayPause = vm::playPause)
    }
}