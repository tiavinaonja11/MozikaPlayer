package com.example.mozika.util

import kotlin.math.roundToInt

fun Long.formatDuration(): String {
    val s = (this / 1000f).roundToInt()
    val m = s / 60
    val sec = s % 60
    return "%d:%02d".format(m, sec)
}