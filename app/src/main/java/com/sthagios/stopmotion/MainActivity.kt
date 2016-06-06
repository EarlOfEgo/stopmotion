package com.sthagios.stopmotion

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.sthagios.stopmotion.list.ImageListActivity
import com.sthagios.stopmotion.utils.LogDebug
import com.sthagios.stopmotion.utils.startActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val MY_PERMISSIONS_REQUEST_CAMERA: Int = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //permission check
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, it's a camera app
            LogDebug("Ask for permission")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                    MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            permissionGranted()
        }
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
                    permissionGranted()
                } else {
                    LogDebug("Permissions granted, starting Gif List")
                    showPermissionDenyInfo()
                }
                return;
            }
        }
    }

    private fun showPermissionDenyInfo() {

        permissions_denied.visibility = View.VISIBLE
        button_settings.visibility = View.VISIBLE
        button_settings.setOnClickListener({
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





