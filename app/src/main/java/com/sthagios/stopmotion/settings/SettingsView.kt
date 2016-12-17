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
    /**
     * Is emitted when the storage option changes
     */
    fun onStorageOptionChanged(): Observable<Void>

    /**
     * View method to set the storage option
     * @param isSet true if external is used, false if internal
     */
    fun setStorageOption(isSet: Boolean)

    /**
     * Is emitted when the thumbs option has been changed
     */
    fun onThumbsInListChanged(): Observable<Void>

    /**
     * View method if thumbs are used or not
     * @param isSet true if thumbs are used, false if not
     */
    fun setThumbsInList(isSet: Boolean)

    /**
     * Is called when there are no permissions to write the external storage
     */
    fun onPermissionNotGranted()

    /**
     * Is emitted when the user granted or denied the storage permission
     */
    fun onPermissionResult(): Observable<Boolean>

    /**
     * IS called when we don't have the permission to access external storage
     */
    fun noStoragePermissionGranted()

    /**
     * Is called when the moving of the gifs started
     */
    fun showMovingLoading(show: Boolean)

}