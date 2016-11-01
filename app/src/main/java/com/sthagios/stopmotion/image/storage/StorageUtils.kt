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

fun Context.getInternalGifStoragePath(): File {
    val file = File(filesDir, "gifs")
    if (!file.exists()) {
        file.mkdirs()
    }
    return file
}

fun Context.getGifDirectoryFile(): File {
    if (useExternalStorage()) {
        //            LogDebug("failed to create directory")
        return getExternalGifStoragePath()
    } else {
        return getInternalGifStoragePath()
    }
}

fun getExternalGifStoragePath(): File {
    val file = File(Environment.getExternalStorageDirectory(), "Stopmotion" + "/gifs/")
    if (!file.exists()) {
        file.mkdirs()
    }
    return file
}


fun Context.getThumbDirectoryFile(): File {
    val mediaStorageDir = File(filesDir, "thumbs")
    if (!mediaStorageDir.exists()) {
        if (!mediaStorageDir.mkdirs()) {
//            LogDebug("failed to create directory")
            return File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "Stopmotion" + "/thumbs/")
        }
    }
    return mediaStorageDir
}
