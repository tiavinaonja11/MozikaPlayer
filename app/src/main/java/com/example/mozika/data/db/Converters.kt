package com.example.mozika.data.db

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromIntArray(value: IntArray): String =
        value.joinToString(separator = ",")

    @TypeConverter
    fun toIntArray(value: String): IntArray =
        if (value.isEmpty()) intArrayOf() else value.split(",").map { it.toInt() }.toIntArray()
}
