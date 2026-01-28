package com.example.mozika.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val trackCount: Int
) : Parcelable