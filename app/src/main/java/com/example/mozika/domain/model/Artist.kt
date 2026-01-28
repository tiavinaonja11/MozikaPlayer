package com.example.mozika.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Artist(
    val id: String,
    val name: String,
    val albumCount: Int,
    val trackCount: Int
): Parcelable