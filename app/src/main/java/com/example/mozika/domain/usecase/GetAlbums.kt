package com.example.mozika.domain.usecase

import com.example.mozika.data.repo.TrackRepo
import com.example.mozika.domain.model.Album
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAlbums @Inject constructor(
    private val repo: TrackRepo
) {
    operator fun invoke(): Flow<List<Album>> = repo.albums()
}