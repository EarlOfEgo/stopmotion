package com.sthagios.stopmotion.settings

import com.sthagios.stopmotion.base.AbstractView
import rx.Observable

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   05.09.16
 */
interface SettingsView : AbstractView {
    fun onStorageOptionChanged(): Observable<Void>

    fun setStorageOption(isSet: Boolean)

    fun onThumbsInListChanged(): Observable<Void>

    fun setThumbsInList(isSet: Boolean)

    fun setCompressionRate(id: Int)

    fun onCompressionRateChanged() : Observable<Float>
}