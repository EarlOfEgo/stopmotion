@file:Suppress("NOTHING_TO_INLINE")

package com.sthagios.stopmotion.utils

import android.content.Context
import android.support.v4.content.ContextCompat
import com.sthagios.stopmotion.BuildConfig
import com.sthagios.stopmotion.R
import com.wooplr.spotlight.SpotlightConfig

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   15.08.16
 */

inline fun Context.getTutorialSharedPrefs() = getSharedPreferences("TUTORIAL", 0)!!

inline fun Context.shouldShowTutorial(): Boolean {
    val name = this.javaClass.simpleName
    return getTutorialSharedPrefs().getBoolean(name, true) && !BuildConfig.DEBUG
}

inline fun Context.showedTutorial() {
    val name = this.javaClass.simpleName
    getTutorialSharedPrefs().edit().putBoolean(name, false).apply()
}

inline fun Context.getSpotlightConfiguration(): SpotlightConfig {
    val config = SpotlightConfig()
    config.isDismissOnBackpress = true
    config.isRevealAnimationEnabled = true
    config.lineAnimationDuration = 500
    config.lineAndArcColor = ContextCompat.getColor(this, R.color.accent)
    config.headingTvColor = ContextCompat.getColor(this, R.color.accent)
    config.headingTvSize = 28
    config.subHeadingTvColor = ContextCompat.getColor(this, R.color.white)
    config.subHeadingTvSize = 16
    config.isDismissOnTouch = true
    config.maskColor = ContextCompat.getColor(this, R.color.tutorial_background)
    return config
}