package com.sthagios.stopmotion

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.view.animation.DecelerateInterpolator
import com.sthagios.stopmotion.list.ImageListActivity
import com.sthagios.stopmotion.utils.LogDebug
import com.sthagios.stopmotion.utils.addAppStart
import com.sthagios.stopmotion.utils.startActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val MY_PERMISSIONS_REQUEST_CAMERA: Int = 123

    override fun onResume() {
        super.onResume()
        //permission check
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            permissionGranted()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        //Use default theme and replace start theme
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Count this as an approximately app start
        addAppStart()

        button_exit.setOnClickListener {
            //TODO track

            finish()
        }
        button_next.setOnClickListener {
            //TODO track
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }
    }


    private var mAnimationStarted: Boolean = false

    override fun onWindowFocusChanged(hasFocus: Boolean) {

        if (!hasFocus || mAnimationStarted) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            animate()
        }

        super.onWindowFocusChanged(hasFocus);
    }

    private val ITEM_DELAY = 300

    private val START_DELAY: Long = 250

    private fun animate() {

        mAnimationStarted = true

        ViewCompat.animate(stopmotion_icon)
                .translationY((-450).toFloat())
                .setStartDelay(START_DELAY)
                .setDuration(1000).setInterpolator(DecelerateInterpolator(1.2f)).start();

        ViewCompat.animate(access_title)
                .translationY(50f)
                .alpha(1f)
                .setStartDelay((START_DELAY + 500).toLong())
                .setDuration(2500)
                .setInterpolator(DecelerateInterpolator()).start()

        ViewCompat.animate(access_text)
                .translationY(50f)
                .alpha(1f)
                .setStartDelay(((START_DELAY * 2) + 500).toLong())
                .setDuration(2500)
                .setInterpolator(DecelerateInterpolator()).start()

        ViewCompat.animate(button_exit)
                .scaleY(1f).scaleX(1f)
                .setStartDelay(((ITEM_DELAY * 3) + 500).toLong())
                .setDuration(1000)
                .setInterpolator(DecelerateInterpolator()).start()

        ViewCompat.animate(button_next)
                .scaleY(1.toFloat()).scaleX(1.toFloat())
                .setStartDelay(((ITEM_DELAY * 3) + 500).toLong())
                .setDuration(1000)
                .setInterpolator(DecelerateInterpolator()).start()

        ViewCompat.animate(divider)
                .scaleY(1.toFloat()).scaleX(1.toFloat())
                .setStartDelay(((ITEM_DELAY * 3) + 500).toLong())
                .setDuration(1000)
                .setInterpolator(DecelerateInterpolator()).start()
    }

    private fun permissionGranted() {
        LogDebug("Permissions granted, starting Gif List")
        startActivity<ImageListActivity>()
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
            grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //TODO track
                    permissionGranted()
                } else {
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                            showPermissionDenyInfo()
                        }
                    }
                }
                return
            }
        }
    }

    private fun showPermissionDenyInfo() {

        button_next.setOnClickListener({
            val i = Intent();
            i.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS;
            i.addCategory(Intent.CATEGORY_DEFAULT);
            i.data = Uri.parse("package:" + packageName);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(i);
        })
    }
}





