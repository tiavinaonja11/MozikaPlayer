package com.example.mozika.domain.usecase

import javax.inject.Inject

class GenWaveform @Inject constructor() {
    /**
     * Génère un waveform à partir du chemin d'un fichier audio
     * @param audioPath Le chemin du fichier audio (String)
     * @return Un tableau d'entiers représentant le waveform
     */
    operator fun invoke(audioPath: String): IntArray {
        // Version simplifiée pour tester
        // Génère un waveform factice basé sur le chemin
        // TODO: Implémenter la vraie génération de waveform avec une bibliothèque audio

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

    /**
     * Surcharge pour accepter un trackId (pour compatibilité)
     * @param trackId L'ID de la chanson (Long)
     * @return Un tableau d'entiers représentant le waveform
     */
    operator fun invoke(trackId: Long): IntArray {
        // Génère un waveform factice basé sur l'ID
        return IntArray(200) { index ->
            val position = (index + trackId.toInt()) % 40
            when {
                position < 10 -> (position + 1) * 10
                position < 20 -> (20 - position) * 10
                position < 30 -> (position - 20) * 10
                else -> (40 - position) * 10
            }
        }
    }
}