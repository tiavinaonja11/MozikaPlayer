package com.example.mozika.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mozika.data.db.entity.WaveformEntity

@Dao
interface WaveformDao {

    @Query("SELECT * FROM waveforms WHERE trackId = :trackId LIMIT 1")
    suspend fun get(trackId: Long): WaveformEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(w: WaveformEntity)
}
