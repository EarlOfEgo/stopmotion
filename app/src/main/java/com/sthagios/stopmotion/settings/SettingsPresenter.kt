package com.sthagios.stopmotion.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.base.AbstractPresenter
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   05.09.16
 */
class SettingsPresenter(val mContext: Context) : AbstractPresenter<SettingsView>() {

    val mMoveGifs: PublishSubject<Boolean> = PublishSubject.create()

    override fun onStart() {

        subscribe(mContext.useExternalStorageObservable()
                .subscribe({ mView!!.setStorageOption(it) }))

        subscribe(mView!!.onStorageOptionChanged()
                .flatMap { mContext.useExternalStorageObservable() }
                .map { !it }
                .subscribe {
                    //If we want to use external storage
                    if (it) {
                        if (permissionGranted()) {
                            mMoveGifs.onNext(it)
                        } else {
                            mView!!.noStoragePermissionGranted()
                        }
                    } else {
                        mMoveGifs.onNext(it)
                    }
                }
        )

        subscribe(
                Observable.merge(mMoveGifs.asObservable(), mView!!.onPermissionResult())
                        .doOnNext { mView!!.showMovingLoading(true) }
                        .flatMap {
                            Observable.just(moveGifs(it))
                                    .subscribeOn(Schedulers.io())
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            mView!!.showMovingLoading(false)
                            if (it) {
                                mContext.setUseExternalStorage(it)
                                mView!!.setStorageOption(it)
                            } else
                                mView!!.onError(Throwable("Moving failed"))
                        }, {
                            mView!!.showMovingLoading(false)
                            mView!!.onError(it)
                        })
        )

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
                .doOnNext { mContext.setCompressionRate(it) }
                .map { getCompressionRateResourceId(it) }
                .subscribe({ mView!!.setCompressionRate(it) }))

        subscribe(mView!!.onPermissionResult()
                .filter { it }
                .subscribe())
    }

    private fun moveGifs(toExternal: Boolean): Boolean {

        if(toExternal) {

        } else {

        }
        return false
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

    fun permissionGranted() = ContextCompat.checkSelfPermission(mContext,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

}