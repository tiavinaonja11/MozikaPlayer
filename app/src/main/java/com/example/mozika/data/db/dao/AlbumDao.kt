package com.example.mozika.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.mozika.data.db.entity.Album
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {

    @Query("""
        SELECT 
            album as id,
            album as title,
            artist,
            COUNT(*) as trackCount
        FROM tracks
        WHERE album != '' AND album != 'Unknown'
        GROUP BY album, artist
        ORDER BY album COLLATE NOCASE
    """)
    fun getAll(): Flow<List<Album>>

    @Query("""
        SELECT 
            album as id,
            album as title,
            artist,
            COUNT(*) as trackCount
        FROM tracks
        WHERE (album LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%')
          AND album != '' AND album != 'Unknown'
        GROUP BY album, artist
        ORDER BY album COLLATE NOCASE
    """)
    fun search(query: String): Flow<List<Album>>
}