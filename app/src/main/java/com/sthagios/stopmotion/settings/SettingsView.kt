package com.sthagios.stopmotion.settings

import com.sthagios.stopmotion.base.AbstractView

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   05.09.16
 */
interface SettingsView : AbstractView {

    fun setStoragePath(path: String)
}