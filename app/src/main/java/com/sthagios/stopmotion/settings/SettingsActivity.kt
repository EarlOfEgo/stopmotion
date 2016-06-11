package com.sthagios.stopmotion.settings

import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import com.sthagios.stopmotion.BuildConfig
import com.sthagios.stopmotion.R
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.toolbar.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(toolbar);
        supportActionBar!!.setDisplayHomeAsUpEnabled(true);
        title = "Settings"


        use_thumbs.setTitle("Use thumbs in List")
        use_thumbs.setSubtitle(
                "Use thumbnails in the overview instead of playing the Gifs. For a better performance.")
        use_thumbs.setChecked(getSettingsPreferences().getBoolean("THUMBS_IN_LIST", false))
        use_thumbs.onCheckChanged { b ->
            //TODO track
            getSettingsPreferences().edit().putBoolean("THUMBS_IN_LIST", b).apply()
        }

        use_pushes.setTitle("Push Notifications")
        use_pushes.setSubtitle("Receive push notifications from Stopmotion")
        use_pushes.setChecked(getSettingsPreferences().getBoolean("PUSHES", true))

        use_pushes.onCheckChanged { b ->
            //TODO track
            getSettingsPreferences().edit().putBoolean("PUSHES", b).apply()
        }

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
        version_text.text = "Version: ${BuildConfig.VERSION_NAME}"

        if (BuildConfig.DEBUG) {
            debug_info.visibility = View.VISIBLE
            version_code.text = "Version Code: ${BuildConfig.VERSION_CODE}"
            git_sha.text = "Git sha: ${BuildConfig.GIT_SHA}"
            build_time.text = "Build time: ${BuildConfig.BUILD_TIME}"
        }
    }
}
