package com.example.mozika.ui.service.player.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun SeekBar(progress: Float, duration: Float, onSeek: (Float) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RectangleShape)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == androidx.compose.ui.input.pointer.PointerEventType.Press) {
                            val percent = event.changes.first().position.x / size.width
                            onSeek(percent.coerceIn(0f, 1f))
                        }
                    }
                }
            }
    ) {
        LinearProgressIndicator(
            progress = { if (duration > 0f) progress / duration else 0f }, // ✅ lambda
            modifier = Modifier.fillMaxSize()
        )
    }
}