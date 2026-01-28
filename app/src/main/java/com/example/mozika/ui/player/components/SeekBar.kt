package com.example.mozika.ui.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun SeekBar(
    progress: Float,
    duration: Float,
    onSeek: (Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val percent = offset.x / size.width
                    onSeek(percent.coerceIn(0f, 1f))
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val currentProgress = if (duration > 0f) progress / duration else 0f

        // Barre de fond
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Color(0xFF404040))
                .clip(CircleShape)
        )

        // Barre de progression
        Box(
            modifier = Modifier
                .fillMaxWidth(currentProgress)
                .height(4.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF1DB954),
                            Color(0xFF1ED760)
                        )
                    )
                )
                .clip(CircleShape)
        )

        // Utiliser un Spacer avec un poids pour positionner le bouton
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(
                modifier = Modifier
                    .weight(currentProgress)
                    .height(0.dp)
            )

            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.White, CircleShape)
            )

            Spacer(
                modifier = Modifier
                    .weight(1f - currentProgress)
                    .height(0.dp)
            )
        }
    }
}