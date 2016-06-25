package com.sthagios.stopmotion.utils

import android.content.Context

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
