package com.example.mozika.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.mozika.data.db.Converters

@Entity(tableName = "waveforms")
@TypeConverters(Converters::class) // nécessaire pour le tableau d'entiers
data class WaveformEntity(
    @PrimaryKey val trackId: Long,
    val amplitudes: IntArray
)
