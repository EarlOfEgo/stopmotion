@file:Suppress("NOTHING_TO_INLINE")

package com.sthagios.stopmotion.utils

import android.content.Context
import android.util.Log
import com.sthagios.stopmotion.BuildConfig

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   12.06.16
 */


inline fun Context.getAppStartPreferences() = getSharedPreferences("APP_START", 0)

inline fun Context.getApproximateAppStarts() = getAppStartPreferences().getInt("APP_START_AMOUNT",
        0)

inline fun Context.addAppStart() {
    val appstarts = getApproximateAppStarts() + 1
    getAppStartPreferences().edit().putInt("APP_START_AMOUNT", appstarts).apply()
}

inline fun Context.LogDebug(param: String) {
    if (BuildConfig.DEBUG)
        Log.d("${this.javaClass.simpleName}", param)
}

inline fun Context.LogVerbose(param: String) {
    if (BuildConfig.DEBUG)
        Log.v("${this.javaClass.simpleName}", param)
}

inline fun Context.LogError(param: String) {
    if (BuildConfig.DEBUG)
        Log.e("${this.javaClass.simpleName}", param)
}
