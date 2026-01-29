package com.example.mozika.ui.player.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun ElegantWaveform(
    amplitudes: IntArray,
    progress: Float,
    isPlaying: Boolean = true,
    modifier: Modifier = Modifier,
    onSeek: (Float) -> Unit
) {
    // Animation de base
    val infiniteTransition = rememberInfiniteTransition()

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isPlaying) 1200 else 2400,
                easing = LinearEasing
            )
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0A0F),
                        Color(0xFF151520),
                        Color(0xFF0A0A0F)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (amplitudes.isEmpty()) return@Canvas

            val totalWidth = size.width
            val centerY = size.height / 2
            val maxHeight = size.height * 0.4f
            val playedWidth = totalWidth * progress.coerceIn(0f, 1f)

            // Palette de couleurs élégante
            val colors = listOf(
                Color(0xFFFF4081), // Rose vif
                Color(0xFF7C4DFF), // Violet profond
                Color(0xFF00BCD4)  // Cyan
            )

            // Dessiner l'onde comme une courbe continue
            val path = Path()
            val barCount = min(amplitudes.size, 200)

            // Créer les points de la courbe
            val points = mutableListOf<Offset>()

            for (i in 0 until barCount) {
                val amplitude = amplitudes[i % amplitudes.size] / 255f
                val x = (i.toFloat() / barCount) * totalWidth

                // Ajouter de l'animation
                val animationFactor = 1f + sin(pulse + i * 0.1f) * 0.15f
                val animatedAmplitude = amplitude * animationFactor
                val height = maxHeight * animatedAmplitude.coerceIn(0.1f, 1f)

                val y = centerY - height
                points.add(Offset(x, y))
            }

            // Dessiner la courbe supérieure
            if (points.isNotEmpty()) {
                path.moveTo(points.first().x, points.first().y)

                for (i in 1 until points.size) {
                    val prev = points[i - 1]
                    val current = points[i]
                    val controlX = (prev.x + current.x) / 2

                    path.quadraticTo(
                        controlX, prev.y,
                        current.x, current.y
                    )
                }

                // Fermer le path pour créer un remplissage
                path.lineTo(totalWidth, centerY + maxHeight)
                path.lineTo(0f, centerY + maxHeight)
                path.close()

                // Remplissage avec dégradé
                drawPath(
                    path = path,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            colors[0].copy(alpha = 0.2f),
                            colors[1].copy(alpha = 0.15f),
                            colors[2].copy(alpha = 0.1f)
                        ),
                        start = Offset(0f, centerY),
                        end = Offset(totalWidth, centerY)
                    )
                )

                // Contour de l'onde
                val outlinePath = Path().apply {
                    moveTo(points.first().x, points.first().y)

                    for (i in 1 until points.size) {
                        val prev = points[i - 1]
                        val current = points[i]
                        val controlX = (prev.x + current.x) / 2

                        quadraticTo(
                            controlX, prev.y,
                            current.x, current.y
                        )
                    }
                }

                drawPath(
                    path = outlinePath,
                    brush = Brush.linearGradient(
                        colors = colors,
                        start = Offset(0f, centerY),
                        end = Offset(totalWidth, centerY)
                    ),
                    style = Stroke(
                        width = 2.5f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // Ligne centrale subtile
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.1f),
                        Color.White.copy(alpha = 0.2f),
                        Color.White.copy(alpha = 0.1f),
                        Color.Transparent
                    )
                ),
                start = Offset(0f, centerY),
                end = Offset(totalWidth, centerY),
                strokeWidth = 1f
            )

            // Curseur de progression
            val cursorX = playedWidth

            // Halo du curseur
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    center = Offset(cursorX, centerY),
                    radius = 15f
                ),
                radius = 15f,
                center = Offset(cursorX, centerY),
                blendMode = BlendMode.Plus
            )

            // Ligne du curseur
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.9f),
                        Color.White.copy(alpha = 0.9f),
                        Color.Transparent
                    )
                ),
                start = Offset(cursorX, centerY - maxHeight * 1.2f),
                end = Offset(cursorX, centerY + maxHeight * 1.2f),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )

            // Point central
            drawCircle(
                color = Color.White,
                center = Offset(cursorX, centerY),
                radius = 4f
            )
        }
    }
}

/**
 * Version simple et élégante avec barres
 */
@Composable
fun SimpleElegantWaveform(
    amplitudes: IntArray,
    progress: Float,
    isPlaying: Boolean = true,
    modifier: Modifier = Modifier,
    onSeek: (Float) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()

    val animationPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isPlaying) 1000 else 2000,
                easing = LinearEasing
            )
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(
                Color(0xFF121212),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (amplitudes.isEmpty()) return@Canvas

            val totalWidth = size.width
            val centerY = size.height / 2
            val maxHeight = size.height * 0.35f
            val playedWidth = totalWidth * progress

            // Palette de couleurs modernes
            val playedColor = Color(0xFF1DB954) // Vert Spotify
            val unplayedColor = Color(0xFF535353)

            // Barres minimalistes
            val barCount = min(amplitudes.size, 120)
            val barWidth = totalWidth / barCount * 0.7f
            val gap = totalWidth / barCount * 0.3f

            for (i in 0 until barCount) {
                val amplitude = amplitudes[i % amplitudes.size] / 255f
                val x = i * (barWidth + gap) + barWidth / 2
                val isPlayed = x < playedWidth

                // Animation subtile
                val animation = 1f + sin(animationPhase + i * 0.2f) * 0.1f
                val animatedHeight = amplitude * animation
                val height = maxHeight * animatedHeight.coerceIn(0.05f, 1f)

                // Choisir la couleur
                val color = if (isPlayed) playedColor else unplayedColor
                val alpha = if (isPlayed) 0.9f else 0.4f

                // Barre supérieure
                drawLine(
                    color = color.copy(alpha = alpha),
                    start = Offset(x, centerY),
                    end = Offset(x, centerY - height),
                    strokeWidth = barWidth,
                    cap = StrokeCap.Round
                )

                // Barre inférieure (symétrique)
                drawLine(
                    color = color.copy(alpha = alpha),
                    start = Offset(x, centerY),
                    end = Offset(x, centerY + height),
                    strokeWidth = barWidth,
                    cap = StrokeCap.Round
                )

                // Point lumineux sur les barres jouées
                if (isPlayed && amplitude > 0.3f) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.3f),
                        center = Offset(x, centerY - height),
                        radius = barWidth / 3
                    )
                }
            }

            // Curseur minimaliste
            drawLine(
                color = Color.White,
                start = Offset(playedWidth, centerY - maxHeight * 1.1f),
                end = Offset(playedWidth, centerY + maxHeight * 1.1f),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )

            drawCircle(
                color = Color.White,
                center = Offset(playedWidth, centerY),
                radius = 3f
            )
        }
    }
}

/**
 * Waveform inspirée des applications audio premium
 */
@Composable
fun PremiumAudioWaveform(
    amplitudes: IntArray,
    progress: Float,
    isPlaying: Boolean = true,
    modifier: Modifier = Modifier,
    onSeek: (Float) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()

    val waveMotion by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3000,
                easing = LinearEasing
            )
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF000000),
                        Color(0xFF0F0F0F),
                        Color(0xFF000000)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (amplitudes.isEmpty()) return@Canvas

            val totalWidth = size.width
            val centerY = size.height / 2
            val maxHeight = size.height * 0.38f
            val playedWidth = totalWidth * progress

            // Effet de vague animé
            val waveOffset = waveMotion * 2 * Math.PI.toFloat()

            // Dessiner des barres avec effet de vague
            val barCount = min(amplitudes.size, 180)
            val barWidth = totalWidth / barCount * 0.8f
            val gap = totalWidth / barCount * 0.2f

            for (i in 0 until barCount) {
                val baseAmplitude = amplitudes[i % amplitudes.size] / 255f
                val x = i * (barWidth + gap) + barWidth / 2
                val isPlayed = x < playedWidth

                // Effet de vague
                val waveEffect = sin(waveOffset + i * 0.1f) * 0.15f
                val animatedAmplitude = baseAmplitude * (1f + waveEffect)
                val height = maxHeight * animatedAmplitude.coerceIn(0.08f, 1f)

                // Dégradé de couleur basé sur la position
                val colorProgress = (i.toFloat() / barCount).coerceIn(0f, 1f)
                val color = when {
                    colorProgress < 0.33f -> Color(0xFFFF2D55) // Rouge
                    colorProgress < 0.66f -> Color(0xFFAF52DE) // Violet
                    else -> Color(0xFF0A84FF) // Bleu
                }

                val alpha = if (isPlayed) {
                    0.8f - colorProgress * 0.2f // Transparence progressive
                } else {
                    0.3f - colorProgress * 0.1f
                }

                // Barre avec dégradé vertical
                val barGradient = Brush.verticalGradient(
                    colors = listOf(
                        color.copy(alpha = alpha),
                        color.copy(alpha = alpha * 0.7f)
                    ),
                    startY = centerY - height,
                    endY = centerY + height
                )

                // Dessiner une ligne avec dégradé (simuler une barre)
                drawLine(
                    brush = barGradient,
                    start = Offset(x, centerY - height),
                    end = Offset(x, centerY + height),
                    strokeWidth = barWidth,
                    cap = StrokeCap.Round
                )

                // Reflet sur le dessus des barres jouées
                if (isPlayed && height > maxHeight * 0.2f) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.15f),
                        start = Offset(x - barWidth / 2, centerY - height),
                        end = Offset(x + barWidth / 2, centerY - height),
                        strokeWidth = 1f
                    )
                }
            }

            // Ligne de progression avec effet glow
            val glowSize = 20f

            // Fond du glow
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0x20FFFFFF),
                        Color(0x10FFFFFF),
                        Color(0x00FFFFFF)
                    )
                ),
                topLeft = Offset(playedWidth - glowSize, 0f),
                size = Size(glowSize * 2, size.height)
            )

            // Ligne principale
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White,
                        Color.White,
                        Color.Transparent
                    )
                ),
                start = Offset(playedWidth, centerY - maxHeight * 1.2f),
                end = Offset(playedWidth, centerY + maxHeight * 1.2f),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )

            // Point central avec glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        Color(0x80FFFFFF)
                    ),
                    center = Offset(playedWidth, centerY),
                    radius = 6f
                ),
                radius = 6f,
                center = Offset(playedWidth, centerY)
            )
        }
    }
}