package com.example.mozika.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mozika.data.db.entity.Track
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tracks: List<Track>)

    @Query("DELETE FROM tracks")
    suspend fun clear()

    @Query("SELECT * FROM tracks")
    fun getAll(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE title LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<Track>>
}