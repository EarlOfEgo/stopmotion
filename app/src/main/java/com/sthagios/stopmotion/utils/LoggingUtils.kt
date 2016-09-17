@file:Suppress("NOTHING_TO_INLINE")

package com.sthagios.stopmotion.utils

import android.util.Log
import com.sthagios.stopmotion.BuildConfig

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   17.09.16
 */
inline fun Any.LogDebug(param: String) {
    if (BuildConfig.DEBUG)
        Log.d("${this.javaClass.simpleName}", param)
}

inline fun Any.LogVerbose(param: String) {
    if (BuildConfig.DEBUG)
        Log.v("${this.javaClass.simpleName}", param)
}

inline fun Any.LogError(param: String) {
    if (BuildConfig.DEBUG)
        Log.e("${this.javaClass.simpleName}", param)
}