package com.example.mozika.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mozika.domain.model.Track as DomainTrack

@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Int,
    val dateAdded: Long,
    val path: String
) {
    fun asDomain(): DomainTrack = DomainTrack(
        id = id,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        dateAdded = dateAdded,
        data = path
    )

    companion object {
        fun fromDomain(track: DomainTrack): Track = Track(
            id = track.id,
            title = track.title,
            artist = track.artist,
            album = track.album,
            duration = track.duration,
            dateAdded = track.dateAdded,
            path = track.data
        )
    }
}