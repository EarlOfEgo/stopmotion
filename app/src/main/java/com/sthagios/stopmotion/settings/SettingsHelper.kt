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
        "COMPRESSION_RATE", 0.2f)

inline fun Context.setCompressionRate(value: Float) {
    getSettingsPreferences().edit().putFloat("COMPRESSION_RATE", value).apply()
}
