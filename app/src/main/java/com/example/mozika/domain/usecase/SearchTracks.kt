package com.example.mozika.domain.usecase

import com.example.mozika.data.repo.TrackRepo
import com.example.mozika.domain.model.Track
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchTracks @Inject constructor(
    private val repo: TrackRepo
) {
    operator fun invoke(query: String): Flow<List<Track>> =
        repo.searchTracks(query)  // Changez "search" en "searchTracks"
}