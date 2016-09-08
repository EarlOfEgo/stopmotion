package com.sthagios.stopmotion.settings

import android.content.Context
import com.sthagios.stopmotion.R
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

        subscribe(mContext.getCompressionRate()
                .map { getCompressionRateResourceId(it) }
                .subscribe({ mView!!.setCompressionRate(it) }))

        subscribe(mView!!.onCompressionRateChanged()

                .map { getCompressionRateResourceId(it) }
                .subscribe({ mView!!.setCompressionRate(it) }))
    }

    private fun getCompressionRateResourceId(it: Float?): Int {
        return when (it) {
            COMPRESSION_HIGH   -> R.string.compression_rate_high
            COMPRESSION_MEDIUM -> R.string.compression_rate_medium
            COMPRESSION_LOW    -> R.string.compression_rate_low
            else               -> {
                R.string.compression_rate_high
            }
        }
    }

}