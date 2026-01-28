package com.example.mozika.data.repo

import com.example.mozika.data.db.dao.WaveformDao
import com.example.mozika.data.db.entity.WaveformEntity
import com.example.mozika.util.Analyzer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WaveformRepo @Inject constructor(
    private val dao: WaveformDao,
    private val analyzer: Analyzer
) {
    suspend fun forTrack(trackId: Long): IntArray {
        dao.get(trackId)?.let { return it.amplitudes }
        val amps = analyzer.compute(trackId)
        dao.insert(WaveformEntity(trackId, amps))
        return amps
    }
}