package com.sthagios.stopmotion.settings

import android.content.Context
import com.sthagios.stopmotion.base.AbstractPresenter

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   05.09.16
 */
class SettingsPresenter(val mContext: Context) : AbstractPresenter<SettingsView>() {
    override fun onStart() {

        subscribe(mContext.useExternalStorage()
                .subscribe({ mView!!.setStorageOption(it) }))
        subscribe(mView!!.onStorageOptionChanged()
                .flatMap { mContext.useExternalStorage() }
                .map { !it }
                .doOnNext { mContext.setUseExternalStorage(it) }
                .subscribe({ mView!!.setStorageOption(it) }))


        subscribe(mContext.useThumbsInList()
                .subscribe({ mView!!.setThumbsInList(it) }))

        subscribe(mView!!.onThumbsInListChanged()
                .flatMap { mContext.useThumbsInList() }
                .map { !it }
                .doOnNext { mContext.setUseThumbsInList(it) }
                .subscribe({ mView!!.setThumbsInList(it) }))
    }
}