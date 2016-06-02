package com.sthagios.stopmotion.camera

import android.content.Context
import android.content.pm.PackageManager

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   16.05.16
 */

/**
 * returns whether there is a camera or not
 */
fun Context.hasCamera() = this.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)


//fun getCameraInstance(): Camera {
//
//    return Camera.open();
//}