    package com.example.mozika.domain.model

    import android.os.Parcelable
    import kotlinx.parcelize.Parcelize

    @Parcelize
    data class Track(
        val id: Long,
        val title: String,
        val artist: String,
        val album: String,
        val duration: Int,
        val dateAdded: Long,
        val data: String
    ) : Parcelable