package com.example.mozika.ui.player.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun Waveform(
    amplitudes: IntArray,
    position: Float,
    duration: Float,
    onSeek: (Float) -> Unit
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onSeek(offset.x / size.width)
                }
            }
    ) {
        val bar = 3f
        val gap = 2f
        val maxH = size.height * 0.8f
        val count = (size.width / (bar + gap)).toInt().coerceAtMost(amplitudes.size)

        if (count > 0 && amplitudes.isNotEmpty()) {
            val step = amplitudes.size / count.coerceAtLeast(1)
            val played = if (duration > 0) position / duration else 0f

            for (i in 0 until count) {
                val amplitude = amplitudes.getOrNull(i * step)?.toFloat() ?: 0f
                val h = (amplitude / 255f) * maxH
                val currentPos = i.toFloat() / count

                // Gradient pour la partie jouée
                val color = if (currentPos < played) {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1DB954), // Vert Spotify
                            Color(0xFF1ED760)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF535353), // Gris foncé
                            Color(0xFFB3B3B3)  // Gris clair
                        )
                    )
                }

                // Dessiner la barre avec arrondi en haut
                drawRoundRect(
                    brush = color,
                    topLeft = Offset(i * (bar + gap), (size.height - h) / 2),
                    size = Size(bar, h),
                    cornerRadius = CornerRadius(bar / 2, bar / 2)
                )
            }

            // Ligne de progression actuelle
            val progressX = played * size.width
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = Offset(progressX.coerceIn(0f, size.width), size.height / 2)
            )
        }
    }
}