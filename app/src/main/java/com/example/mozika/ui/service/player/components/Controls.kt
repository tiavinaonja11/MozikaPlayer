package com.example.mozika.ui.service.player.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun Controls(isPlaying: Boolean, onPlayPause: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Button(onClick = onPlayPause) {
            Text(if (isPlaying) "PAUSE" else "PLAY")
        }
    }
}