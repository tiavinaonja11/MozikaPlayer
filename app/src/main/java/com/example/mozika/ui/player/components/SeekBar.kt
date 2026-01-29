package com.example.mozika.ui.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
    val rawProgress = if (duration > 0f) (progress / duration) else 0f
    val currentProgress = rawProgress.coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val percent = (offset.x / size.width).coerceIn(0f, 1f)
                    onSeek(percent)
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Track arrière
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0))
        )

        // Track rempli
        Box(
            modifier = Modifier
                .fillMaxWidth(currentProgress)
                .height(4.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color(0xFF4DA3FF),
                            Color(0xFF9CCDFF)
                        )
                    )
                )
        )

        // Thumb sans weight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = (currentProgress * 300f).dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(20.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(
                        width = 2.dp,
                        color = Color(0xFF4DA3FF),
                        shape = CircleShape
                    )
            )
        }
    }
}
