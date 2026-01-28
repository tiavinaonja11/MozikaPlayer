package com.example.mozika.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.mozika.data.db.entity.Artist
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {

    @Query("""
        SELECT 
            artist as id,
            artist as name,
            COUNT(DISTINCT album) as albumCount,
            COUNT(*) as trackCount
        FROM tracks
        WHERE artist != '' AND artist != 'Unknown'
        GROUP BY artist
        ORDER BY artist COLLATE NOCASE
    """)
    fun getAll(): Flow<List<Artist>>

    @Query("""
        SELECT 
            artist as id,
            artist as name,
            COUNT(DISTINCT album) as albumCount,
            COUNT(*) as trackCount
        FROM tracks
        WHERE artist LIKE '%' || :query || '%'
          AND artist != '' AND artist != 'Unknown'
        GROUP BY artist
        ORDER BY artist COLLATE NOCASE
    """)
    fun search(query: String): Flow<List<Artist>>
}