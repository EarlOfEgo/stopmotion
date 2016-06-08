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