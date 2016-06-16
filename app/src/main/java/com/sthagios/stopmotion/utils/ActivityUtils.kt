package com.sthagios.stopmotion.utils

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.util.Log
import com.sthagios.stopmotion.BuildConfig
import java.util.*

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

inline fun <reified T : Activity> Activity.startActivity(long: Long) {
    val intent = Intent(this, T::class.java)
    intent.putExtra("long_param", long)
    startActivity(intent)
}

inline fun <reified T : Activity> Activity.startActivity(long: Long, resultCode: Int) {
    val intent = Intent(this, T::class.java)
    intent.putExtra("long_param", long)
    startActivityForResult(intent, resultCode)
}

inline fun <reified T : Activity> Activity.startActivity(long: Long, resultCode: Int,
        bundle: Bundle) {
    val intent = Intent(this, T::class.java)
    intent.putExtra("long_param", long)
    startActivityForResult(intent, resultCode, bundle)
}

inline fun <reified T : Activity> Activity.startActivity(stringList: ArrayList<String>) {
    val intent = Intent(this, T::class.java)
    intent.putStringArrayListExtra("string_list_param", stringList)
    startActivity(intent)
}

inline fun Activity.retrieveStringListParameter(): ArrayList<String> {
    if (intent != null && intent.extras != null && intent.extras.containsKey("string_list_param"))
        return intent.extras.getStringArrayList("string_list_param")
    else
        return ArrayList()
}

inline fun Activity.retrieveLongParameter(): Long {
    if (intent != null && intent.extras != null)
        return intent.extras.getLong("long_param", 0)
    else
        return 0
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

inline fun Activity.retrieveStringParameter(): String {
    if (intent != null && intent.extras != null)
        return intent.extras.getString("string_param", "")
    else
        return ""
}

inline fun Activity.LogDebug(param: String) {
    if (BuildConfig.DEBUG)
        Log.d("${this.javaClass.simpleName}", param)
}

inline fun Activity.LogVerbose(param: String) {
    if (BuildConfig.DEBUG)
        Log.v("${this.javaClass.simpleName}", param)
}

inline fun Activity.LogError(param: String) {
    if (BuildConfig.DEBUG)
        Log.e("${this.javaClass.simpleName}", param)
}

inline fun Activity.showWhichThreadInLogcat() {

    val main = Looper.myLooper() == Looper.getMainLooper()
    if (main) {
        Log.d("${this.javaClass.simpleName}", "On main Thread")
    } else {
        Log.d("${this.javaClass.simpleName}", "On ${Thread.currentThread().toString()} Thread")
    }
}
