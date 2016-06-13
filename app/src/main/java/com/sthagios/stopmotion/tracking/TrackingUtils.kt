package com.sthagios.stopmotion.tracking

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   13.06.16
 */

inline fun Context.getFirebaseInstance() = FirebaseAnalytics.getInstance(this)