package com.example.mozika.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mozika.domain.model.Artist as DomainArtist

@Entity(tableName = "artists")
data class Artist(
    @PrimaryKey val id: String,
    val name: String,
    val albumCount: Int,
    val trackCount: Int
) {
    fun asDomain(): DomainArtist = DomainArtist(
        id = id,
        name = name,
        albumCount = albumCount,
        trackCount = trackCount
    )
}