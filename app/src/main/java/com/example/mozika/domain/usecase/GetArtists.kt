package com.example.mozika.domain.usecase

import com.example.mozika.data.repo.TrackRepo
import com.example.mozika.domain.model.Artist
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetArtists @Inject constructor(
    private val repo: TrackRepo
) {
    operator fun invoke(): Flow<List<Artist>> = repo.artists()
}