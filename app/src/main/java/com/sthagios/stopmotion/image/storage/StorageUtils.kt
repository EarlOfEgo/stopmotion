package com.sthagios.stopmotion.image.storage

import android.content.Context
import android.os.Environment
import com.sthagios.stopmotion.settings.useExternalStorage
import java.io.File

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   17.09.16
 */

fun Context.getInternalGifStoragePath() = File(filesDir, "gifs")

fun Context.getGifDirectoryFile(): File {
    if (useExternalStorage()) {
        return getExternalGifStoragePath()
    } else {
        return getInternalGifStoragePath()
    }
}

fun getExternalGifStoragePath() = File(Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_PICTURES), "Stopmotion" + "/gifs/")


fun getExternalThumbsStoragePath() = File(Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_PICTURES), "Stopmotion" + "/thumbs/")

fun Context.getThumbDirectoryFile(): File {
    if (useExternalStorage()) {
        return getExternalThumbsStoragePath()
    } else {
        return getInternalThumbsStoragePath()
    }
}

fun Context.getInternalThumbsStoragePath() = File(filesDir, "thumbs")
