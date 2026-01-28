package com.example.mozika.domain.model

data class Waveform(val amplitudes: IntArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Waveform
        return amplitudes.contentEquals(other.amplitudes)
    }
    override fun hashCode(): Int = amplitudes.contentHashCode()
}