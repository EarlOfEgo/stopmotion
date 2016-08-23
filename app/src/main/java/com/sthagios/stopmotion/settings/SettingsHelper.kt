@file:Suppress("NOTHING_TO_INLINE")

package com.sthagios.stopmotion.settings

import android.content.Context

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   08.06.16
 */
inline fun Context.getSettingsPreferences() = getSharedPreferences("SETTINGS", 0)

inline fun Context.isPushOn() = getSettingsPreferences().getBoolean("PUSHES", true)

inline fun Context.getCompressionRate(): Float = getSettingsPreferences().getFloat(
        "COMPRESSION_RATE", COMPRESSION_HIGH)

inline fun Context.setCompressionRate(value: Float) {
    getSettingsPreferences().edit().putFloat("COMPRESSION_RATE", value).apply()
}

val COMPRESSION_HIGH = 0.2f
val COMPRESSION_MEDIUM = 0.4f
val COMPRESSION_LOW = 0.6f
