package com.example.mozika.util

import android.content.ContentUris
import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Analyzer @Inject constructor(@ApplicationContext private val ctx: Context) {

    fun compute(trackId: Long): IntArray {
        val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, trackId)
        val extractor = MediaExtractor()
        ctx.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
            extractor.setDataSource(pfd.fileDescriptor)
        } ?: return intArrayOf()

        val fmt = extractor.getTrackFormat(0)
        val duration = fmt.getLong(MediaFormat.KEY_DURATION)
        extractor.selectTrack(0)

        val points = 200
        val out = IntArray(points)
        val buf = ByteBuffer.allocate(1024)
        val step = duration / points.toLong()

        for (i in 0 until points) {
            var max = 0
            val target = i * step
            while (extractor.sampleTime < target) {
                buf.clear()
                val n = extractor.readSampleData(buf, 0)
                if (n <= 0) break
                for (k in 0 until n) {
                    val v = kotlin.math.abs(buf[k].toInt())
                    if (v > max) max = v
                }
                extractor.advance()
            }
            out[i] = max
        }
        extractor.release()
        return out
    }
}