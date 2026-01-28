package com.example.mozika.ui.service.player.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
            .height(120.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onSeek(offset.x / size.width)
                }
            }
    ) {
        val bar = 4f
        val gap = 2f
        val maxH = size.height
        val count = (size.width / (bar + gap)).toInt().coerceAtMost(amplitudes.size)
        val step = amplitudes.size / count
        for (i in 0 until count) {
            val h = (amplitudes[i * step] / 255f) * maxH
            drawRect(
                color = if (i * step < (position / duration) * amplitudes.size) Color.Cyan else Color.Gray,
                topLeft = Offset(i * (bar + gap), (maxH - h) / 2),
                size = Size(bar, h)
            )
        }
    }
}