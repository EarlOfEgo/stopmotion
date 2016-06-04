package com.sthagios.stopmotion.utils

import android.app.Activity
import android.content.Intent
import android.os.Looper
import android.util.Log

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   14.05.16
 */


inline fun <reified T : Activity> Activity.startActivity(string: String) {
    val intent = Intent(this, T::class.java)
    intent.putExtra("string_param", string)
    startActivity(intent)
}

/**
 * Starts an activity without a parameter.
 * There is a verbose log call with the calling activity's name as tag.
 */
inline fun <reified T : Activity> Activity.startActivity() {
    Log.v(this.javaClass.simpleName, "Starting activity: ${T::class.java.simpleName}")
    val intent = Intent(this, T::class.java)
    startActivity(intent)
}

fun Activity.retrieveStringParameter(): String = intent.extras.getString("string_param", "")

fun Activity.LogDebug(param: String) {
    Log.d("${this.javaClass.simpleName}", param)
}

fun Activity.showWhichThreadInLogcat() {

    val main = Looper.myLooper() == Looper.getMainLooper()
    if (main) {
        Log.d("${this.javaClass.simpleName}", "On main Thread")
    } else {
        Log.d("${this.javaClass.simpleName}", "On ${Thread.currentThread().toString()} Thread")
    }
}
