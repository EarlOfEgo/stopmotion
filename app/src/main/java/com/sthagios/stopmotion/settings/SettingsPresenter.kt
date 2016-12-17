package com.sthagios.stopmotion.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.base.AbstractPresenter
import com.sthagios.stopmotion.image.database.Gif
import com.sthagios.stopmotion.image.database.getRealmInstance
import com.sthagios.stopmotion.image.storage.getExternalGifStoragePath
import com.sthagios.stopmotion.image.storage.getInternalGifStoragePath
import com.sthagios.stopmotion.utils.LogError
import com.sthagios.stopmotion.utils.LogVerbose
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

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
                .subscribe({ mView?.setStorageOption(it) }))

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
                            LogVerbose("Done moving")
                            mView!!.showMovingLoading(false)
                            mContext.setUseExternalStorage(it)
                            mView!!.setStorageOption(it)
                        }, {
                            LogError(it)
                            mView!!.showMovingLoading(false)
                            mView!!.onError(it)
                        })
        )

        subscribe(mContext.useThumbsInList()
                .subscribe({ mView?.setThumbsInList(it) }))

        subscribe(mView!!.onThumbsInListChanged()
                .flatMap { mContext.useThumbsInList() }
                .map { !it }
                .doOnNext { mContext.setUseThumbsInList(it) }
                .subscribe({ mView?.setThumbsInList(it) }))

        subscribe(mView!!.onPermissionResult()
                .filter { it }
                .subscribe())
    }

    private fun moveGifs(toExternal: Boolean): Boolean {

        LogVerbose("Move toExternal: $toExternal")
        val externalGifPath = getExternalGifStoragePath()
        val internalGifPath = mContext.getInternalGifStoragePath()

        val realm = mContext.getRealmInstance()
        val gifs = realm.where(Gif::class.java).findAll()
        for (gif in gifs) {

            val fromGifPath = if (toExternal) internalGifPath else externalGifPath
            val toGifPath = if (toExternal) externalGifPath else internalGifPath

            val from = File("$fromGifPath/${gif.fileName}")
            val to = File("$toGifPath/${gif.fileName}")
            copyFile(from, to)
            if (!from.delete())
                throw Exception("Deletion failed")

            realm.executeTransaction {
                gif.shareUriString = FileProvider.getUriForFile(mContext,
                        mContext.getString(R.string.fileprovider_authority), to).toString()
                gif.fileUriString = Uri.fromFile(to).toString()

            }
        }
        return toExternal
    }

    private fun copyFile(src: File, dst: File) {
        val inChannel = FileInputStream(src).channel
        val outChannel = FileOutputStream(dst).channel
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel)
        } finally {
            inChannel?.close()
            outChannel?.close()
        }
    }

    fun permissionGranted() = ContextCompat.checkSelfPermission(mContext,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

}