package com.example.mozika.data.db.dao

import androidx.room.*
import com.example.mozika.data.db.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT trackId FROM favorites ORDER BY addedAt DESC")
    fun getFavoriteTrackIds(): Flow<List<Long>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE trackId = :trackId)")
    suspend fun isFavorite(trackId: Long): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE trackId = :trackId)")
    fun isFavoriteFlow(trackId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE trackId = :trackId")
    suspend fun removeFavorite(trackId: Long)
}