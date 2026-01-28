package com.example.mozika.domain.usecase

import com.example.mozika.data.repo.TrackRepo
import javax.inject.Inject

class RefreshTracks @Inject constructor(
    private val repo: TrackRepo
) {
    suspend operator fun invoke() {
        repo.refreshTracks()
    }
}