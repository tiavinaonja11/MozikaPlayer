package com.example.mozika.ui.player

import android.content.Context
import android.media.audiofx.Visualizer
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer

class AudioWaveformAnalyzer(
    private val context: Context
) {
    private var visualizer: Visualizer? = null
    private var audioSessionId: Int = Visualizer.ERROR_BAD_VALUE

    @OptIn(UnstableApi::class)
    fun start(exoPlayer: ExoPlayer, onWaveform: (IntArray) -> Unit) {
        try {
            // Récupérer l'audio session ID depuis ExoPlayer
            audioSessionId = exoPlayer.audioSessionId
            if (audioSessionId == Visualizer.ERROR_BAD_VALUE || audioSessionId < 0) {
                return
            }

            stop() // sécurité

            visualizer = Visualizer(audioSessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1]

                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            visualizer: Visualizer?,
                            waveform: ByteArray?,
                            samplingRate: Int
                        ) {
                            waveform?.let { bytes ->
                                val amps = IntArray(bytes.size) { i ->
                                    bytes[i].toInt() and 0xFF
                                }
                                onWaveform(amps)
                            }
                        }

                        override fun onFftDataCapture(
                            visualizer: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int
                        ) {
                            // Optionnel - non utilisé ici
                        }
                    },
                    Visualizer.getMaxCaptureRate() / 2,
                    true,
                    false
                )
                enabled = true
            }
        } catch (e: Throwable) {
            // Erreur -3 ou autre : on log et on désactive l'analyse temps réel
            e.printStackTrace()
            stop()
        }
    }

    fun start(exoPlayer: ExoPlayer) {
        start(exoPlayer) { /* default empty callback */ }
    }

    fun stop() {
        visualizer?.release()
        visualizer = null
    }
}