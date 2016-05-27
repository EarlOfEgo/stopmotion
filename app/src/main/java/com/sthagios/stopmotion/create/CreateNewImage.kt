package com.sthagios.stopmotion.create

import android.hardware.Camera
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.FrameLayout
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.camera.CameraPreview

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   14.05.16
 */
class CreateNewImage : AppCompatActivity() {

    private val TAG = "CreateNewImage"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_create_new_image)

        safeCameraOpen(0)
        mPreview = CameraPreview(this, mCamera!!)

        val preview = findViewById(R.id.camera_preview) as FrameLayout;
        preview.addView(mPreview);
    }

    private var mCamera: Camera? = null

    private fun safeCameraOpen(id: Int): Boolean {
        var qOpened = false

        try {
            releaseCameraAndPreview()
            mCamera = Camera.open(id)
            qOpened = (mCamera != null)
        } catch (e: Exception) {
            Log.d(TAG, "Couldn' open camera" + e.message);
        }

        return qOpened;
    }

    private var mPreview: CameraPreview? = null

    private fun releaseCameraAndPreview() {
        if (mCamera != null) {
            (mCamera as Camera).release()
            mCamera = null
        }
    }
}