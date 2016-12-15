@file:Suppress("NOTHING_TO_INLINE")

package com.sthagios.stopmotion.tracking

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   13.06.16
 */

inline fun Context.getFirebaseInstance() = FirebaseAnalytics.getInstance(this)

inline fun Context.logSettingsEvent(type: String) {
    val bundle = Bundle()
    bundle.putString("type", type)
    getFirebaseInstance().logEvent("settings", bundle)
}

inline fun Context.logSettingsEvent(type: String, enabled: Boolean) {
    val bundle = Bundle()

    bundle.putString("enabled", "$enabled")
    bundle.putString("type", type)
    getFirebaseInstance().logEvent("settings", bundle)
}

inline fun Context.logRatingEvent(type: String, enabled: Boolean) {
    val bundle = Bundle()

    bundle.putString("enabled", "$enabled")
    bundle.putString("type", type)
    getFirebaseInstance().logEvent("rating", bundle)
}

inline fun Context.logCameraEvent(type: String, burstAmount: Int, burstTime: Int) {
    val bundle = Bundle()

    bundle.putString("burst_amount", "$burstAmount")
    bundle.putString("burst_time", "$burstTime")
    bundle.putString("type", type)
    getFirebaseInstance().logEvent("camera", bundle)
}

inline fun Context.logSettingsEvent(type: String, content: String) {
    val bundle = Bundle()

    bundle.putString("content", content)
    bundle.putString("type", type)
    getFirebaseInstance().logEvent("settings", bundle)
}

inline fun Context.logEditEvent(type: String, content: String) {
    val bundle = Bundle()

    bundle.putString("content", content)
    bundle.putString("type", type)
    getFirebaseInstance().logEvent("edit", bundle)
}

inline fun Context.setUserProperty(key: String, value: String) {
    getFirebaseInstance().setUserProperty(key, value)
}
