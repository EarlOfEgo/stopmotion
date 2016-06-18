package com.sthagios.stopmotion.settings

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import com.sthagios.stopmotion.BuildConfig
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.settings.dialogs.CompressionDialog
import com.sthagios.stopmotion.settings.dialogs.LicensesDialog
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.toolbar.*

class SettingsActivity : AppCompatActivity(), CompressionDialog.Callback {
    override fun onRateChosen(value: Int) {
        when (value) {
            0 -> {
                setCompressionRate(COMPRESSION_HIGH)
                setCompressionRateText(COMPRESSION_HIGH)
            }
            1 -> {
                setCompressionRate(COMPRESSION_MEDIUM)
                setCompressionRateText(COMPRESSION_MEDIUM)
            }
            2 -> {
                val oldCompression = getCompressionRate()
                Snackbar.make(gif_compression, R.string.snackbar_warning_low_compression,
                        Snackbar.LENGTH_LONG)
                        .setAction(R.string.snackbar_undo_action_text, {
                            if (oldCompression != COMPRESSION_LOW) {
                                setCompressionRate(oldCompression)
                                setCompressionRateText(oldCompression)
                            } else {
                                setCompressionRate(COMPRESSION_HIGH)
                                setCompressionRateText(COMPRESSION_HIGH)
                            }
                        })
                        .show()
                setCompressionRate(COMPRESSION_LOW)
                setCompressionRateText(COMPRESSION_LOW)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(toolbar);
        supportActionBar!!.setDisplayHomeAsUpEnabled(true);
        title = getString(R.string.settings_title)


        use_thumbs.setTitle(R.string.settings_use_thumbnails_title)
        use_thumbs.setSubtitle(R.string.settings_use_thumbnails_description)
        use_thumbs.setChecked(getSettingsPreferences().getBoolean("THUMBS_IN_LIST", false))
        use_thumbs.onCheckChanged { b ->
            //TODO track
            getSettingsPreferences().edit().putBoolean("THUMBS_IN_LIST", b).apply()
        }

        use_pushes.setTitle(R.string.settings_pushes_title)
        use_pushes.setSubtitle(R.string.settings_pushes_description)
        use_pushes.setChecked(getSettingsPreferences().getBoolean("PUSHES", true))

        use_pushes.onCheckChanged { b ->
            //TODO track
            getSettingsPreferences().edit().putBoolean("PUSHES", b).apply()
        }

        gif_compression.setTitle(R.string.settings_gif_compression_rate_title)
        gif_compression.setSubtitle(R.string.settings_gif_compression_rate_description)
        val compressionRate = getCompressionRate()
        setCompressionRateText(compressionRate)
        gif_compression.setOnClickListener({
            val dialog = CompressionDialog.newInstance(getCompressionRate())
            dialog.show(fragmentManager, "CompressionDialog")
        })

        licenses.setTitle(R.string.settings_licences_title)
        licenses.setSubtitle(R.string.settings_licences_description)
        licenses.setOnClickListener({
            val dialog = LicensesDialog()
            dialog.show(fragmentManager, "LicensesDialog")
        })


        setUpVersionInfos()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                return true;
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpVersionInfos() {
        version_text.text = getString(R.string.settings_version, BuildConfig.VERSION_NAME)

        if (BuildConfig.DEBUG) {
            debug_info.visibility = View.VISIBLE
            version_code.text = "Version Code: ${BuildConfig.VERSION_CODE}"
            git_sha.text = "Git sha: ${BuildConfig.GIT_SHA}"
            build_time.text = "Build time: ${BuildConfig.BUILD_TIME}"
        }
    }

    private fun setCompressionRateText(compressionRate: Float) {
        when (compressionRate) {
            COMPRESSION_HIGH   -> gif_compression.setValueText(R.string.compression_rate_high)
            COMPRESSION_MEDIUM -> gif_compression.setValueText(R.string.compression_rate_medium)
            COMPRESSION_LOW -> gif_compression.setValueText(R.string.compression_rate_low)
        }
    }
}

