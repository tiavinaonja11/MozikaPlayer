package com.example.mozika.data.mapper

import com.example.mozika.data.db.entity.Track
import com.example.mozika.domain.model.Track as DomainTrack

fun Track.toDomain(): DomainTrack {
    return DomainTrack(
        id = id,
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        data = path,
        dateAdded = dateAdded
    )
}
