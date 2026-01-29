package com.example.mozika.ui.player

import android.media.audiofx.Visualizer

class AudioWaveformAnalyzer(
    private val audioSessionId: Int
) {
    private var visualizer: Visualizer? = null

    fun start(onWaveform: (IntArray) -> Unit) {
        try {
            if (audioSessionId == Visualizer.ERROR_BAD_VALUE) return

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
                            if (waveform == null) return
                            val amps = IntArray(waveform.size) { i ->
                                waveform[i].toInt() and 0xFF
                            }
                            onWaveform(amps)
                        }

                        override fun onFftDataCapture(
                            visualizer: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int
                        ) {
                            // optionnel
                        }
                    },
                    Visualizer.getMaxCaptureRate() / 2,
                    true,
                    false
                )
                enabled = true
            }
        } catch (e: Throwable) {
            // Erreur -3 ou autre : on log et on désactive l’analyse temps réel
            e.printStackTrace()
            stop()
        }
    }

    fun stop() {
        visualizer?.release()
        visualizer = null
    }
}
