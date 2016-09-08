package com.sthagios.stopmotion.settings

import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.NavUtils
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import com.sthagios.stopmotion.BuildConfig
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.settings.dialogs.CompressionDialog
import com.sthagios.stopmotion.settings.dialogs.LicensesDialog
import com.sthagios.stopmotion.tracking.getFirebaseInstance
import com.sthagios.stopmotion.tracking.logSettingsEvent
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.toolbar.*
import rx.Observable
import rx.subjects.PublishSubject


class SettingsActivity : AppCompatActivity(), CompressionDialog.Callback, SettingsView {
    override fun onCompressionRateChanged(): Observable<Float> {
        mOnCompressionRateChanged.asObservable()
    }

    override fun onError(throwable: Throwable) {
        //TODO
    }

    override fun onRateChosen(value: Int) {
        when (value) {
            0 -> {
                mOnCompressionRateChanged.onNext(COMPRESSION_HIGH)
                logSettingsEvent("gif_compression", "COMPRESSION_HIGH")
            }
            1 -> {
                mOnCompressionRateChanged.onNext(COMPRESSION_MEDIUM)
                logSettingsEvent("gif_compression", "COMPRESSION_MEDIUM")
            }
            2 -> {
                //TODO this has to be part of the presenter!
                val oldCompression = getCompressionRate().toBlocking().first()
                Snackbar.make(gif_compression, R.string.snackbar_warning_low_compression,
                        Snackbar.LENGTH_LONG)
                        .setAction(R.string.snackbar_undo_action_text, {
                            logSettingsEvent("gif_compression", "undo")
                            if (oldCompression != COMPRESSION_LOW) {
                                mOnCompressionRateChanged.onNext(oldCompression)
                            } else {
                                mOnCompressionRateChanged.onNext(COMPRESSION_HIGH)
                            }
                        })
                        .show()
                mOnCompressionRateChanged.onNext(COMPRESSION_LOW)
                logSettingsEvent("gif_compression", "COMPRESSION_LOW")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!mPresenter.hasView()) {
            mPresenter.attachView(this)
            mPresenter.onStart()
        }
    }

    override fun onStop() {
        super.onStop()
        mPresenter.detachView()
    }

    private lateinit var mPresenter: SettingsPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        mPresenter = SettingsPresenter(baseContext)

        setSupportActionBar(toolbar)
        title = getString(R.string.settings_title)

        use_thumbs.onCheckChanged { b ->
            mOnThumbsInListChanged.onNext(null)
        }

        gif_compression.setOnClickListener({
            val dialog = CompressionDialog.newInstance(getCompressionRate())
            dialog.show(fragmentManager, "CompressionDialog")
        })

        licenses.setOnClickListener({
            logSettingsEvent("license")
            val dialog = LicensesDialog()
            dialog.show(fragmentManager, "LicensesDialog")
        })

        store_options.onCheckChanged {
            mOnStorageOptionChanged.onNext(null)
        }

        icon_image_view.setOnClickListener {
            if (mImageClickCount++ > 6) {
                ViewCompat.animate(icon_image_view)
                        .scaleY(0.toFloat())
                        .scaleX(0.toFloat())
                        .setDuration(1000)
                        .setInterpolator(DecelerateInterpolator()).start()

                ViewCompat.animate(icon_image_view2)
                        .setStartDelay(1000)
                        .scaleY(1.toFloat())
                        .scaleX(1.toFloat())
                        .setDuration(1000)
                        .setInterpolator(DecelerateInterpolator()).start()

                val bundle = Bundle()
                bundle.putString("wee", "wee")
                getFirebaseInstance().logEvent("easter_egg", bundle)
            }
        }

        val uri = Uri.parse("file:///android_asset/gif.gif")
        val target = GlideDrawableImageViewTarget(icon_image_view2)
        Glide.with(this).load(uri).into(target)

        setUpVersionInfo()
    }

    override fun setCompressionRate(id: Int) {
        gif_compression.setValueText(id)
    }

    private val mOnStorageOptionChanged: PublishSubject<Void> = PublishSubject.create()
    private val mOnThumbsInListChanged: PublishSubject<Void> = PublishSubject.create()
    private val mOnCompressionRateChanged: PublishSubject<Float> = PublishSubject.create()

    override fun setStorageOption(isSet: Boolean) {
        store_options.setChecked(isSet)
    }

    override fun onThumbsInListChanged(): Observable<Void> = mOnThumbsInListChanged.asObservable()


    override fun setThumbsInList(isSet: Boolean) {
        use_thumbs.setChecked(isSet)
    }

    override fun onStorageOptionChanged(): Observable<Void> = mOnStorageOptionChanged.asObservable()

    private var mImageClickCount = 0

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpVersionInfo() {
        version_text.text = getString(R.string.settings_version, BuildConfig.VERSION_NAME)

        if (BuildConfig.DEBUG) {
            debug_info.visibility = View.VISIBLE
            version_code.text = "Version Code: ${BuildConfig.VERSION_CODE}"
            git_sha.text = "Git sha: ${BuildConfig.GIT_SHA}"
            build_time.text = "Build time: ${BuildConfig.BUILD_TIME}"
        }
    }

}

