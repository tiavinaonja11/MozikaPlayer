package com.example.mozika.domain.usecase

import javax.inject.Inject

class GenWaveform @Inject constructor() {
    operator fun invoke(trackId: Long): IntArray {
        // Version simplifiée pour tester
        // Génère un waveform factice
        return IntArray(200) { index ->
            val position = index % 40
            when {
                position < 10 -> (position + 1) * 10
                position < 20 -> (20 - position) * 10
                position < 30 -> (position - 20) * 10
                else -> (40 - position) * 10
            }
        }
    }
}