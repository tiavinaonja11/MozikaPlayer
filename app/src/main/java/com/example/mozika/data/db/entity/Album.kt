package com.example.mozika.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mozika.domain.model.Album as DomainAlbum

@Entity(tableName = "albums")
data class Album(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val trackCount: Int
) {
    fun asDomain(): DomainAlbum = DomainAlbum(
        id = id,
        title = title,
        artist = artist,
        trackCount = trackCount
    )
}