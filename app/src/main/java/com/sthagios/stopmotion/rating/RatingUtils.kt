package com.sthagios.stopmotion.rating

import android.content.Context
import com.sthagios.stopmotion.utils.getApproximateAppStarts

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   20.06.16
 */
inline fun Context.shouldShowRating(): Boolean {
    val userRated = userRated()
    val userGaveFeedback = userGaveFeedback()
    if (!userRated && !userGaveFeedback) {
        val userWontFeedback = userWontFeedback()
        val userWontRated = userWontRated()
        val approximateAppStarts = getApproximateAppStarts()
        if (!userWontFeedback && !userWontRated) {
            if (approximateAppStarts % 4 == 0)
                return true
        }
        if (userWontFeedback && approximateAppStarts == 10)
            return true
        if (userWontFeedback && approximateAppStarts == 6)
            return true
    }
    return false
}


inline fun Context.userRated() = getRatingPreferences().getBoolean("USER_RATED", false)

inline fun Context.userGaveFeedback() = getRatingPreferences().getBoolean("USER_FEEDBACK", false)

inline fun Context.userWontRated() = getRatingPreferences().getBoolean("USER_WONT_RATED", false)

inline fun Context.userWontFeedback() = getRatingPreferences().getBoolean("USER_WONT_FEEDBACK",
        false)

inline fun Context.getRatingPreferences() = getSharedPreferences("RATE", 0)

inline fun Context.setUserRated() = getRatingPreferences().edit()
        .putBoolean("USER_RATED", true).apply()

inline fun Context.setUserFeedback() = getRatingPreferences().edit()
        .putBoolean("USER_FEEDBACK", true).apply()

inline fun Context.setUserWontRate() = getRatingPreferences().edit()
        .putBoolean("USER_WONT_RATED", true).apply()

inline fun Context.setUserWontFeedback() = getRatingPreferences().edit()
        .putBoolean("USER_WONT_FEEDBACK", true).apply()