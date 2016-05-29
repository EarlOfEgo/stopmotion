package com.sthagios.stopmotion.create

import android.hardware.Camera
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.camera.CameraPreview
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit.SECONDS

/**
 * Stopmotion
 *
 * @author  stephan
 * @since   14.05.16
 */
class CreateNewImage : AppCompatActivity(), AbstractDialog.Callback {
    override fun amountChosen(amount: Int) {
        mBurstAmount = amount
        setBurstTexts()
    }

    private val BUNDLE_BURST_TIME = "BUNDLE_BURST_TIME"

    private val BUNDLE_BURST_AMOUNT = "BUNDLE_BURST_AMOUNT"

    private var mBurstTime = 0

    private var mBurstAmount = 3

    override fun timeChosen(time: Int) {
        mBurstTime = time
        setBurstTexts()
    }

    private fun setBurstTexts() {
        mAmountText.text = "$mBurstAmount"
        mTimeText.text = resources.getStringArray(R.array.burst_times)[mBurstTime]
    }

    private val TAG = "CreateNewImage"

    private lateinit var mSwitchButton: ImageButton

    private var mCameraId = 0

    private lateinit var mTimeText: TextView

    private lateinit var mLoadingOverlay: View

    private lateinit var mAmountText: TextView

    override fun onSaveInstanceState(outState: Bundle?) {
        outState!!.putInt(BUNDLE_BURST_TIME, mBurstTime)
        outState.putInt(BUNDLE_BURST_AMOUNT, mBurstAmount)
        super.onSaveInstanceState(outState)
    }

    private lateinit var mPreviewLayout: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_create_new_image)

        if (savedInstanceState != null) {
            mBurstTime = savedInstanceState.getInt(BUNDLE_BURST_TIME, 0)
            mBurstAmount = savedInstanceState.getInt(BUNDLE_BURST_AMOUNT, 0)
        }

        mPreviewLayout = findViewById(R.id.camera_preview) as FrameLayout

        openCamera()

        mLoadingOverlay = findViewById(R.id.progress_bar)!!

        mTimeText = findViewById(R.id.time_textView) as TextView
        mAmountText = findViewById(R.id.amount_text_view) as TextView


        findViewById(R.id.container_time)!!.setOnClickListener({
            onTimeClicked()
        })
        findViewById(R.id.container_amount)!!.setOnClickListener({
            onAmountClicked()
        })

        mSwitchButton = findViewById(R.id.button_switch_camera) as ImageButton
        mSwitchButton.setOnClickListener({
            if (mCameraId == 0) {
                mCameraId = 1
                //TODO make this work
//                mCamera!!.stopPreview();
//                safeCameraOpen(mCameraId)
//                mCamera!!.startPreview();
                openCamera()
                mSwitchButton.setImageDrawable(resources.getDrawable(R.drawable.ic_camera_rear_black_48dp))
            } else {
                mCameraId = 0
                //TODO make this work
//                mCamera!!.stopPreview();
//                safeCameraOpen(mCameraId)
//                mCamera!!.startPreview();
                openCamera()
                mSwitchButton.setImageDrawable(resources.getDrawable(R.drawable.ic_camera_front_black_48dp))
            }
        })

        setBurstTexts()

        findViewById(R.id.button_capture)!!.setOnClickListener({
            //TODO Track
            takePicture()
        })
    }

    private fun openCamera() {
        try {
            if (safeCameraOpen(mCameraId) == false)
                throw Exception("Could not open Camera")

            mPreview = CameraPreview(this, mCamera!!)

            mPreviewLayout.addView(mPreview);
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
            findViewById(R.id.button_capture)!!.isEnabled = false
            Snackbar.make(mPreviewLayout, "Camera Error", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (cameraSubscription != null)
            cameraSubscription!!.unsubscribe()
    }

    private var cameraSubscription: Subscription? = null

    private fun takePicture() {

        mLoadingOverlay.visibility = View.VISIBLE

        cameraSubscription =
                Observable
                        .interval(0, mBurstTime.toLong() + 2, SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .take(mBurstAmount)
                        .flatMap { it -> Observable.just(takePictureAndGetFileName()) }
//                        .subscribeOn(Schedulers.io())
                        .subscribe({
                            Log.d(TAG, "Image: $it")
                        }, {
                            mLoadingOverlay.visibility = View.GONE
                            Log.e(TAG, "${it.message}")
                        }, {
                            mLoadingOverlay.visibility = View.GONE
                        })


    }

    private fun takePictureAndGetFileName(): String {
        var fileName = ""

        mCamera!!.takePicture(null, null, Camera.PictureCallback { bytes, camera ->

            val pictureFile: File = getOutputMediaFile()!!;
            Log.d(TAG, "Storing image at: ${pictureFile.path}")

            try {
                val fos: FileOutputStream = FileOutputStream(pictureFile);
                fos.write(bytes);
                fos.close();
            } catch (e: FileNotFoundException) {
                Log.e(TAG, "File not found: ${e.message}");
            } catch (e: IOException) {
                Log.e(TAG, "Error accessing file: ${e.message}");
            } catch(e: Exception) {
                Log.e(TAG, "Error taking picture: ${e.message}")
            }
            fileName = pictureFile.path
            Log.d(TAG, "Image created: ${pictureFile.path}")
            Log.d(TAG, "Releasing")
            openCamera()
        })
        return fileName
    }

    fun getOutputMediaFile(): File? {
        val mediaStorageDir = File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "stopmotion")



        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("stopmotion", "failed to create directory")
                return null
            }
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val mediaFile = File(mediaStorageDir.path + File.separator + "IMG_" + timeStamp + ".jpg")

        return mediaFile;
    }

    override fun onPause() {
        super.onPause()
        releaseCameraAndPreview()
    }


    private fun getOutputMediaFileUri() = Uri.fromFile(getOutputMediaFile())

    private fun onTimeClicked() {
        val dialog = BurstTimeDialog.newInstance(mBurstTime)
        dialog.show(fragmentManager, "BurstTimingDialog")
    }

    private fun onAmountClicked() {
        val dialog = BurstAmountDialog.newInstance(mBurstAmount)
        dialog.show(fragmentManager, "BurstAmountDialog")
    }

    private var mCamera: Camera? = null

    private fun safeCameraOpen(id: Int): Boolean {
        var qOpened = false

        try {
            releaseCameraAndPreview()
            mCamera = Camera.open(id)
            qOpened = (mCamera != null)
        } catch (e: Exception) {
            Log.d(TAG, "Couldn't open camera" + e.message);
        }

        return qOpened;
    }

    private lateinit var mPreview: CameraPreview

    private fun releaseCameraAndPreview() {
        if (mCamera != null) {
            (mCamera as Camera).release()
            mCamera = null
        }
    }

}