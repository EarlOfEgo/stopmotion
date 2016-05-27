package com.sthagios.stopmotion.camera

import android.content.Context
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View

/**
 * Stopmotion

 * @author stephan
 * *
 * @since 27.05.16
 */
class CameraPreview(private val mContext: Context, private val mCamera: Camera) : SurfaceView(mContext), SurfaceHolder.Callback {
    private val mHolder: SurfaceHolder
    private val mSupportedPreviewSizes: List<Camera.Size>?
    private var mPreviewSize: Camera.Size? = null
    private val TAG = "CameraPreview"

    init {

        // supported preview sizes
        mSupportedPreviewSizes = mCamera.parameters.supportedPreviewSizes
        for (str in mSupportedPreviewSizes!!)
            Log.e(TAG, "${str.width}/${str.height}")

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = holder
        mHolder.addCallback(this)
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // empty. surfaceChanged will take care of stuff
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        Log.e(TAG, "surfaceChanged => w=$w, h=$h")
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        if (mHolder.surface == null) {
            // preview surface does not exist
            return
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview()
        } catch (e: Exception) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or reformatting changes here
        // start preview with new settings
        try {
            val parameters = mCamera.parameters
            parameters.setPreviewSize(mPreviewSize!!.width, mPreviewSize!!.height)
            mCamera.parameters = parameters
            mCamera.setDisplayOrientation(90)
            mCamera.setPreviewDisplay(mHolder)
            mCamera.startPreview()

        } catch (e: Exception) {
            Log.d(TAG, "Error starting camera preview: " + e.message)
        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = View.resolveSize(suggestedMinimumWidth, widthMeasureSpec)
        val height = View.resolveSize(suggestedMinimumHeight, heightMeasureSpec)

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height)
        }

        val ratio: Float
        if (mPreviewSize!!.height >= mPreviewSize!!.width)
            ratio = mPreviewSize!!.height.toFloat() / mPreviewSize!!.width.toFloat()
        else
            ratio = mPreviewSize!!.width.toFloat() / mPreviewSize!!.height.toFloat()

        // One of these methods should be used, second method squishes preview slightly
        setMeasuredDimension(width, (width * ratio).toInt())
        //        setMeasuredDimension((int) (width * ratio), height);
    }

    private fun getOptimalPreviewSize(sizes: List<Camera.Size>?, w: Int, h: Int): Camera.Size? {
        val ASPECT_TOLERANCE = 0.1
        val targetRatio = h.toDouble() / w

        if (sizes == null)
            return null

        var optimalSize: Camera.Size? = null
        var minDiff = java.lang.Double.MAX_VALUE

        val targetHeight = h

        for (size in sizes) {
            val ratio = size.height.toDouble() / size.width
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue

            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size
                minDiff = Math.abs(size.height - targetHeight).toDouble()
            }
        }

        if (optimalSize == null) {
            minDiff = java.lang.Double.MAX_VALUE
            for (size in sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size
                    minDiff = Math.abs(size.height - targetHeight).toDouble()
                }
            }
        }

        return optimalSize
    }

}