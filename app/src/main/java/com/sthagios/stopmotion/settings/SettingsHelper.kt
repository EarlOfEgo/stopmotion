@file:Suppress("NOTHING_TO_INLINE")

package com.sthagios.stopmotion.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import rx.Observable

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   08.06.16
 */
inline fun Context.getSettingsPreferences() = getSharedPreferences("SETTINGS", 0)

inline fun Context.isPushOn() = getSettingsPreferences().getBoolean("PUSHES", true)

inline fun Context.useThumbsInList(): Observable<Boolean> = Observable.just(
        getSettingsPreferences().getBoolean("THUMBS_IN_LIST", false))

inline fun Context.setUseThumbsInList(value: Boolean) {
    getSettingsPreferences().edit().putBoolean("THUMBS_IN_LIST", value).apply()
}

inline fun Context.getCompressionRate(): Float = getSettingsPreferences().getFloat(
        "COMPRESSION_RATE", COMPRESSION_HIGH)

inline fun Context.getCompressionRateObservable(): Observable<Float> = Observable.just(
        getCompressionRate())

inline fun Context.setCompressionRate(value: Float) {
    getSettingsPreferences().edit().putFloat("COMPRESSION_RATE", value).apply()
}

inline fun Context.setUseExternalStorage(value: Boolean) {
    getSettingsPreferences().edit().putBoolean("USE_EXTERNAL_STORAGE", value).apply()
}

inline fun Context.useExternalStorageObservable(): Observable<Boolean> = Observable.just(
        getSettingsPreferences().getBoolean("USE_EXTERNAL_STORAGE", false))

inline fun Context.useExternalStorage(): Boolean =
        getSettingsPreferences().getBoolean("USE_EXTERNAL_STORAGE", false)
                && checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

val COMPRESSION_HIGH = 0.2f
val COMPRESSION_MEDIUM = 0.4f
val COMPRESSION_LOW = 0.6f
