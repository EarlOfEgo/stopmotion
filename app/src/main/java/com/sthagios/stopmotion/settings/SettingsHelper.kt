@file:Suppress("NOTHING_TO_INLINE")

package com.sthagios.stopmotion.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
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

inline fun Context.setUseExternalStorage(value: Boolean) {
    getSettingsPreferences().edit().putBoolean("USE_EXTERNAL_STORAGE", value).apply()
}

inline fun Context.useExternalStorageObservable(): Observable<Boolean> = Observable.just(
        getSettingsPreferences().getBoolean("USE_EXTERNAL_STORAGE", false))

inline fun Context.useExternalStorage(): Boolean =
        getSettingsPreferences().getBoolean("USE_EXTERNAL_STORAGE", false)
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

