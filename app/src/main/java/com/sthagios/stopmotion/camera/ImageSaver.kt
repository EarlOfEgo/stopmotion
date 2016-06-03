package com.sthagios.stopmotion.camera

import android.media.Image
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   02.06.16
 */
class ImageSaver(val mImage: Image, val mFile: File, val callback: (String) -> Unit) : Runnable {

    override fun run() {
        val buffer = mImage.planes[0].buffer;
        val bytes = ByteArray(buffer.remaining());
        buffer.get(bytes);
        var output: FileOutputStream? = null;
        try {
            output = FileOutputStream(mFile);
            output.write(bytes);
        } catch (e: IOException) {
            e.printStackTrace();
        } finally {
            mImage.close();
            callback(mFile.absolutePath)
            if (null != output) {
                try {
                    output.close();
                } catch (e: IOException) {
                    e.printStackTrace();
                }
            }
        }
    }
}