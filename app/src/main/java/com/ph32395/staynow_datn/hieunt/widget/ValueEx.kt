package com.ph32395.staynow_datn.hieunt.widget

import android.annotation.SuppressLint
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.PI
import kotlin.math.roundToInt

@SuppressLint("SimpleDateFormat")
fun Long.toDate(): String {
    val date = Date(this)
    val sdf = SimpleDateFormat("yyyy-MM-dd")
    return sdf.format(date)
}

@SuppressLint("SimpleDateFormat")
fun Long.toDateTime(): String {
    val date = Date(this)
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    return sdf.format(date)
}

fun Float.dpToPx(context: Context): Float {
    return this * context.resources.displayMetrics.density
}

fun Int.toDp(context: Context): Int = (this * context.resources.displayMetrics.density + 0.5f).toInt()

fun Float.toDp(context: Context): Int = (this * context.resources.displayMetrics.density + 0.5f).toInt()

fun String.isNumber(): Boolean {
    return try {
        this.toDouble()
        true
    } catch (e: NumberFormatException) {
        false
    }
}

fun Any.isDouble(): Boolean {
    return this is Double
}

fun Float.toRadian(): Float {
    return this * (PI / 180).toFloat()
}
fun Float.roundToFive(): Int {
    return ((this / 5.0).roundToInt() * 5.0).toInt()
}
fun Int.roundToFive(): Int {
    return ((this / 5.0).roundToInt() * 5.0).toInt()
}

fun Double.toDMSLatitude(): String {
    val degrees = this.toInt()
    val minutes = ((this - degrees) * 60).toInt()
    val seconds = ((this - degrees - minutes / 60) * 60 * 60).toInt()
    return "${degrees}° ${minutes}' ${seconds}\"" + if (this >= 0) " N" else " S"
}

fun Double.toDMSLongitude(): String {
    val degrees = this.toInt()
    val minutes = ((this - degrees) * 60).toInt()
    val seconds = ((this - degrees - minutes / 60) * 60 * 60).toInt()
    return "${degrees}° ${minutes}' ${seconds}\"" + if (this >= 0) "E" else "W"
}