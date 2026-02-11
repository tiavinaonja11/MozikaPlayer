package com.example.mozika.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "play_count")
data class PlayCountEntity(
    @PrimaryKey
    val trackId: Long,
    val playCount: Int = 0,
    val lastPlayed: Long = 0
)