package com.example.mozika.data.db.dao

import androidx.room.*
import com.example.mozika.data.db.entity.PlayCountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayCountDao {

    @Query("SELECT * FROM play_count ORDER BY playCount DESC LIMIT :limit")
    fun getTopPlayed(limit: Int): Flow<List<PlayCountEntity>>

    @Query("SELECT trackId FROM play_count WHERE lastPlayed > 0 ORDER BY lastPlayed DESC LIMIT :limit")
    fun getRecentlyPlayed(limit: Int): Flow<List<Long>>

    @Query("INSERT OR REPLACE INTO play_count (trackId, playCount, lastPlayed) VALUES (:trackId, COALESCE((SELECT playCount FROM play_count WHERE trackId = :trackId), 0) + 1, :timestamp)")
    suspend fun incrementPlayCount(trackId: Long, timestamp: Long = System.currentTimeMillis())
}