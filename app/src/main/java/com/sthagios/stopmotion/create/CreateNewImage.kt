package com.sthagios.stopmotion.create

import android.hardware.Camera
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.camera.CameraPreview

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

    private lateinit var mAmountText: TextView

    override fun onSaveInstanceState(outState: Bundle?) {
        outState!!.putInt(BUNDLE_BURST_TIME, mBurstTime)
        outState.putInt(BUNDLE_BURST_AMOUNT, mBurstAmount)
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_create_new_image)

        if (savedInstanceState != null) {
            mBurstTime = savedInstanceState.getInt(BUNDLE_BURST_TIME, 0)
            mBurstAmount = savedInstanceState.getInt(BUNDLE_BURST_AMOUNT, 0)
        }

        safeCameraOpen(mCameraId)
        mPreview = CameraPreview(this, mCamera!!)

        val preview = findViewById(R.id.camera_preview) as FrameLayout
        preview.addView(mPreview);

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
                mSwitchButton.setImageDrawable(resources.getDrawable(R.drawable.ic_camera_rear_black_48dp))
            } else {
                mCameraId = 0
                //TODO make this work
//                mCamera!!.stopPreview();
//                safeCameraOpen(mCameraId)
//                mCamera!!.startPreview();
                mSwitchButton.setImageDrawable(resources.getDrawable(R.drawable.ic_camera_front_black_48dp))
            }
        })

        setBurstTexts()
    }

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