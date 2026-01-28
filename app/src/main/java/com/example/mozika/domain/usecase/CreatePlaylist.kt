package com.example.mozika.domain.usecase

import com.example.mozika.data.repo.PlaylistRepo
import javax.inject.Inject

class CreatePlaylist @Inject constructor(private val repo: PlaylistRepo) {

    suspend operator fun invoke(name: String): Long = repo.create(name)
}