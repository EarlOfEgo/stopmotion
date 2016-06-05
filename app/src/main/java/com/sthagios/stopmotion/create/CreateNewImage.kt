package com.sthagios.stopmotion.create

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.hardware.camera2.*
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.ImageReader
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.support.design.widget.Snackbar
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.view.View
import com.bumptech.glide.gifencoder.AnimatedGifEncoder
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.camera.ImageSaver
import com.sthagios.stopmotion.image.database.Gif
import com.sthagios.stopmotion.image.database.getRealmInstance
import com.sthagios.stopmotion.show.ShowGifActivity
import com.sthagios.stopmotion.utils.LogDebug
import com.sthagios.stopmotion.utils.showWhichThreadInLogcat
import com.sthagios.stopmotion.utils.startActivity
import kotlinx.android.synthetic.main.activity_create_new_image.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

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
        amount_text_view.text = "$mBurstAmount"
        time_textView.text = resources.getStringArray(R.array.burst_times)[mBurstTime]
    }

    private val mSurfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?) = true

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            openCamera(width, height)
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int,
                height: Int) {
            configureTransform(width, height)
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
        }

    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param width  The width of `mTextureView`
     * @param height The height of `mTextureView`
     */
    private fun configureTransform(width: Int, height: Int) {
        if (null == camera_preview || null == mPreviewSize) {
            return
        }
        val rotation = windowManager.defaultDisplay.rotation
        val matrix = Matrix()
        val viewRect = RectF(0.toFloat(), 0.toFloat(), width.toFloat(), height.toFloat())
        val bufferRect = RectF(0.toFloat(), 0.toFloat(), mPreviewSize!!.height.toFloat(),
                mPreviewSize!!.width.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
            val scale = Math.max((height / mPreviewSize!!.height).toFloat(),
                    (width / mPreviewSize!!.width).toFloat())
            matrix.postScale(scale, scale, centerX, centerY)
            matrix.postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180.toFloat(), centerX, centerY)
        }
        camera_preview.setTransform(matrix)
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()

        if (mAvailableCameras.size > 0) {

            if (camera_preview.isAvailable) {
                openCamera(camera_preview.width, camera_preview.height)
            } else {
                camera_preview.surfaceTextureListener = mSurfaceTextureListener
            }
        } else {
            Snackbar.make(camera_preview, "No camera available", Snackbar.LENGTH_INDEFINITE).show()
        }
    }

    private var mCaptureSession: CameraCaptureSession? = null

    private var mImageReader: ImageReader? = null

    private fun closeCamera() {
        try {
            mCameraOpenCloseLock.acquire()
            if (null != mCaptureSession) {
                mCaptureSession!!.close()
                mCaptureSession = null
            }
            if (null != mCameraDevice) {
                (mCameraDevice as CameraDevice).close()
                mCameraDevice = null
            }
            if (null != mImageReader) {
                mImageReader!!.close()
                mImageReader = null
            }
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            mCameraOpenCloseLock.release()
        }
    }


    private val mCameraOpenCloseLock = Semaphore(1)

    private var mCameraDevice: CameraDevice? = null

    private var mPreviewSize: Size? = null

    private var mPreviewRequestBuilder: CaptureRequest.Builder? = null

    private fun createCameraPreviewSession() {
        try {
            val texture = camera_preview.surfaceTexture

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize!!.width, mPreviewSize!!.height)

            // This is the output Surface we need to start preview.
            val surface = Surface(texture)

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder = mCameraDevice!!.createCaptureRequest(
                    CameraDevice.TEMPLATE_PREVIEW)
            mPreviewRequestBuilder!!.addTarget(surface)

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice!!.createCaptureSession(
                    Arrays.asList(surface, mImageReader!!.surface),
                    object : CameraCaptureSession.StateCallback() {

                        override fun onConfigured(session: CameraCaptureSession?) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return
                            }

                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = session
                            try {
                                // Auto focus should be continuous for camera preview.
                                mPreviewRequestBuilder!!.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                                // Flash is automatically enabled when necessary.
//                                setAutoFlash(mPreviewRequestBuilder!!)
//                                mPreviewRequestBuilder!!.set(CaptureRequest.CONTROL_AE_MODE,
//                                        CaptureRequest.FLASH_MODE_OFF)

                                // Finally, we start displaying the camera preview.
                                mPreviewRequest = mPreviewRequestBuilder!!.build()
                                mCaptureSession!!.setRepeatingRequest(mPreviewRequest,
                                        mCaptureCallback, mBackgroundHandler)
                            } catch (e: CameraAccessException) {
                                e.printStackTrace()
                            }
                        }


                        override fun onConfigureFailed(session: CameraCaptureSession?) {
                            Log.e(TAG, "Failed to create capture session")
                        }
                    }, null
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }


    private val mCaptureCallback: CameraCaptureSession.CaptureCallback = object : CameraCaptureSession.CaptureCallback() {

        override fun onCaptureCompleted(session: CameraCaptureSession?, request: CaptureRequest?,
                result: TotalCaptureResult?) {
            process(result!!)
        }

        override fun onCaptureProgressed(session: CameraCaptureSession?, request: CaptureRequest?,
                partialResult: CaptureResult?) {
            process(partialResult!!)
        }

        private fun process(result: CaptureResult) {
            when (mState) {
            // We have nothing to do when the camera preview is working normally.
                STATE_PREVIEW                -> {
                }
                STATE_WAITING_LOCK           -> {
                    val afState = result.get(CaptureResult.CONTROL_AF_STATE)
                    if (afState == null)
                        captureStillPicture()
                    else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = STATE_PICTURE_TAKEN
                            captureStillPicture()
                        } else {
                            runPrecaptureSequence()
                        }
                    }
                }
                STATE_WAITING_PRECAPTURE     -> {
                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE
                    }
                }
                STATE_WAITING_NON_PRECAPTURE -> {
                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN
                        captureStillPicture()
                    }
                }
            }
        }
    }

    /**
     * Run the precapture sequence for capturing a still image. This method should be called when
     * we get a response in {@link #mCaptureCallback} from {@link #lockFocus()}.
     */
    private fun runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mPreviewRequestBuilder!!.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START)
            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            mState = STATE_WAITING_PRECAPTURE
            mCaptureSession!!.capture(mPreviewRequestBuilder!!.build(), mCaptureCallback,
                    mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private var mPreviewRequest: CaptureRequest? = null

    private fun setAutoFlash(mPreviewRequestBuilder: CaptureRequest.Builder) {
        if (mFlashSupported) {
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
        }
    }

    /**
     * Capture a still picture. This method should be called when we get a response in
     * {@link #mCaptureCallback} from both {@link #lockFocus()}.
     */
    private fun captureStillPicture() {
        try {
            if (null == mCameraDevice) {
                return
            }
            // This is the CaptureRequest.Builder that we use to take a picture.
            val captureBuilder = mCameraDevice!!.createCaptureRequest(
                    CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(mImageReader!!.surface)

            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
//            setAutoFlash(captureBuilder)

            // Orientation
            val rotation = windowManager.defaultDisplay.rotation
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation))

            val CaptureCallback = object : CameraCaptureSession.CaptureCallback() {


                override fun onCaptureCompleted(session: CameraCaptureSession?,
                        request: CaptureRequest?, result: TotalCaptureResult?) {
                    Log.d(TAG, "Capture complete")
                    unlockFocus()
                }
            }

            mCaptureSession!!.stopRepeating()
            mCaptureSession!!.capture(captureBuilder.build(), CaptureCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * finished.
     */
    private fun unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreviewRequestBuilder!!.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)
            setAutoFlash(mPreviewRequestBuilder!!)
            mCaptureSession!!.capture(mPreviewRequestBuilder!!.build(), mCaptureCallback,
                    mBackgroundHandler)
            // After this, the camera will go back to the normal state of preview.
            mState = STATE_PREVIEW
            mCaptureSession!!.setRepeatingRequest(mPreviewRequest, mCaptureCallback,
                    mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    /**
     * Conversion from screen rotation to JPEG orientation.
     */
    private val ORIENTATIONS: SparseIntArray = SparseIntArray()

    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @param rotation The screen rotation.
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    private fun getOrientation(rotation: Int): Int {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360
    }


    private val mStateCallback = object : CameraDevice.StateCallback() {

        override fun onOpened(camera: CameraDevice?) {
            mCameraOpenCloseLock.release()
            mCameraDevice = camera
            createCameraPreviewSession()
        }


        override fun onDisconnected(camera: CameraDevice?) {
            mCameraOpenCloseLock.release()
            camera!!.close()
            mCameraDevice = null
        }

        override fun onError(camera: CameraDevice?, error: Int) {
            mCameraOpenCloseLock.release()
            camera!!.close()
            mCameraDevice = null
            finish()
        }

    }

    private var mBackgroundThread: HandlerThread? = null

    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("CameraBackground")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
    }

    private var mBackgroundHandler: Handler? = null

    private fun openCamera(width: Int, height: Int) {
        setUpCameraOutputs(width, height)
        configureTransform(width, height)

        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler)
            Log.v(TAG, "Camera($mCameraId) opened")
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera opening.", e)
        }
    }

    private val mOnImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        val file = getOutputMediaFileForImage()
        mBackgroundHandler!!.post(ImageSaver(reader.acquireNextImage(), file, {
            Log.d(TAG, "Saved $it")
            mPictureList = mPictureList.plus(it)

            if (mPictureList.size == mBurstAmount) {
                Log.d(TAG, "All images taken, converting to gif")
                createGif()
            }
        }
        ))
    }

    private var mSensorOrientation: Int = 0

    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    private val MAX_PREVIEW_WIDTH = 1920

    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    private val MAX_PREVIEW_HEIGHT = 1080

    private var mFlashSupported: Boolean = false

    private var mAvailableCameras: Array<String> = emptyArray()

    private fun setUpCameraInfos() {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        mAvailableCameras = manager.cameraIdList
        Log.v(TAG, "Available cameraids: ${manager.cameraIdList.size}")
    }

    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    private fun setUpCameraOutputs(width: Int, height: Int) {
        Log.v(TAG, "Setting camera output, width: $width, height: $height")
        try {
            val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager


            //TODO check for camera if available
            val characteristics = manager.getCameraCharacteristics(mCameraId)

            // We don't use a front facing camera in this sample.
//            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
//            if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
//                continue
//            }

            val map: StreamConfigurationMap = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

            // For still image captures, we use the largest available size.
            val largest: Size = Collections.max(map.getOutputSizes(ImageFormat.JPEG).asList(),
                    { lhs, rhs ->
                        Math.signum(
                                (lhs!!.width * lhs.height - rhs!!.width * rhs.height).toDouble()).toInt()
                    })
            mImageReader = ImageReader.newInstance(largest.width, largest.height,
                    ImageFormat.JPEG, /*maxImages*/2)
            mImageReader!!.setOnImageAvailableListener(
                    mOnImageAvailableListener, mBackgroundHandler)

            // Find out if we need to swap dimension to get the preview size relative to sensor
            // coordinate.
            val displayRotation = windowManager.defaultDisplay.rotation
            //noinspection ConstantConditions
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
            var swappedDimensions = false

            when (displayRotation) {
                Surface.ROTATION_0, Surface.ROTATION_180  ->
                    if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                        swappedDimensions = true
                    }
                Surface.ROTATION_90, Surface.ROTATION_270 ->
                    if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                        swappedDimensions = true
                    }
                else                                      ->
                    Log.e(TAG, "Display rotation is invalid: " + displayRotation)
            }

            val displaySize = Point()
            windowManager.defaultDisplay.getSize(displaySize)
            var rotatedPreviewWidth = width
            var rotatedPreviewHeight = height
            var maxPreviewWidth = displaySize.x
            var maxPreviewHeight = displaySize.y

            if (swappedDimensions) {
                rotatedPreviewWidth = height
                rotatedPreviewHeight = width
                maxPreviewWidth = displaySize.y
                maxPreviewHeight = displaySize.x
            }

            if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                maxPreviewWidth = MAX_PREVIEW_WIDTH
            }

            if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                maxPreviewHeight = MAX_PREVIEW_HEIGHT
            }

            // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
            // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
            // garbage capture data.
            mPreviewSize = chooseOptimalSize(
                    map.getOutputSizes(SurfaceTexture::class.java).asList(),
                    rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                    maxPreviewHeight, largest)
            Log.v(TAG,
                    "Preview size: height=${mPreviewSize!!.height} width=${mPreviewSize!!.width}")

            // We fit the aspect ratio of TextureView to the size of preview we picked.
            val orientation = resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                camera_preview.setAspectRatio(mPreviewSize!!.width, mPreviewSize!!.height)
            } else {
                camera_preview.setAspectRatio(mPreviewSize!!.height, mPreviewSize!!.width)
            }

            // Check if the flash is supported.
            val available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
            if (available == null) mFlashSupported = false else mFlashSupported = available

        } catch (e: CameraAccessException) {
            Log.e(TAG, e.message)
            e.printStackTrace()
        } catch (e: NullPointerException) {
            Log.e(TAG, e.message)
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
//            ErrorDialog.newInstance(getString(R.string.camera_error))
//                    .show(getChildFragmentManager(), FRAGMENT_DIALOG)
        }
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
     * is at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size,
     * and whose aspect ratio matches with the specified value.
     *
     * @param choices           The list of sizes that the camera supports for the intended output
     *                          class
     * @param textureViewWidth  The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth          The maximum width that can be chosen
     * @param maxHeight         The maximum height that can be chosen
     * @param aspectRatio       The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    private fun chooseOptimalSize(choices: List<Size>, textureViewWidth: Int,
            textureViewHeight: Int, maxWidth: Int, maxHeight: Int, aspectRatio: Size): Size? {
        Log.v(TAG, "Choosing optimal size from ${choices.toString()}\n" +
                "with: textureViewWidth:$textureViewWidth textureViewHeight: $textureViewHeight, maxWidth: $maxWidth, maxHeight: $maxHeight, aspectRatio: $aspectRatio")
        // Collect the supported resolutions that are at least as big as the preview Surface
        val bigEnough = ArrayList<Size>()
        // Collect the supported resolutions that are smaller than the preview Surface
        val notBigEnough = ArrayList<Size>()
        val w = aspectRatio.width
        val h = aspectRatio.height
        for (option in choices) {
            if (option.width <= maxWidth && option.height <= maxHeight &&
                    option.height == option.width * h / w) {
                if (option.width >= textureViewWidth &&
                        option.height >= textureViewHeight) {
                    bigEnough.add(option)
                } else {
                    notBigEnough.add(option)
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size > 0) {
            return Collections.min(bigEnough, {
                lhs, rhs ->
                Math.signum(
                        (lhs!!.width * lhs.height - rhs!!.width * rhs.height).toDouble()).toInt()
            })
        } else if (notBigEnough.size > 0) {
            return Collections.max(notBigEnough, {
                lhs, rhs ->
                Math.signum(
                        (lhs!!.width * lhs.height - rhs!!.width * rhs.height).toDouble()).toInt()
            })
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size")
            return choices[0]
        }
    }

    private val TAG = "CreateNewImage"

    override fun onSaveInstanceState(outState: Bundle?) {
        outState!!.putInt(BUNDLE_BURST_TIME, mBurstTime)
        outState.putInt(BUNDLE_BURST_AMOUNT, mBurstAmount)
        super.onSaveInstanceState(outState)
    }

    private val APP_FOLDER_NAME = "Stopmotion"

    private fun createGif() {
        var gifName = ""
        rx.Observable.just(
                getGifDirectoryFile())
                .map {
                    gifName = "${SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())}.gif"
                    "$it/$gifName"
                }
                .doOnNext { Log.d(TAG, "Gif path $it") }
                .map { FileOutputStream(it) }
                .doOnNext { t -> t!!.write(generateGIF()) }
                .doOnNext { t -> t.close() }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ Log.d(TAG, "Gif created") },
                        { Log.e(TAG, "${it.message}") },
                        {
//                            deleteTempFolder()

                            storeInDatabase(gifName)

                            Log.d(TAG, "Done")

                        }
                )
    }

    private fun storeInDatabase(gifName: String) {


        val realm = getRealmInstance()

        realm.executeTransaction {
            val gif = realm.createObject(Gif::class.java)
            val id = Math.abs(Random().nextLong())
            gif.id = id
            gif.fileName = gifName
            val imagePath = File(filesDir, "gifs");
            val newFile = File(imagePath, gif.fileName);

            gif.shareUriString = FileProvider.getUriForFile(
                    this,
                    "com.sthagios.stopmotion.fileprovider",
                    newFile).toString()


            gif.name = "Stopmotion Gif"
            gif.fileUriString = Uri.fromFile(newFile).toString()


            LogDebug("Stored gif ${gif.toString()}")

            realm.close()

            startActivity<ShowGifActivity>(id)
            finish()
        }
    }

    private fun deleteTempFolder() {
        val file = File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), APP_FOLDER_NAME + "/tmp_images/")
        file.delete()
    }

    private fun generateGIF(): ByteArray {
        showWhichThreadInLogcat()
        val bos = ByteArrayOutputStream()
        //Use glide gif encoder
        val encoder = AnimatedGifEncoder()
        encoder.start(bos)
        encoder.setRepeat(0)
        Log.d(TAG, "Start gif encoding")
        for (path in mPictureList) {
            val matrix = Matrix()

            val bitmap = BitmapFactory.decodeFile(path)
            matrix.postRotate(90.toFloat())

            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 800, 600, true)

            val rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width,
                    scaledBitmap.height, matrix, true)

            Log.d(TAG,
                    "Adding Frame: height:${rotatedBitmap.height} + width:${rotatedBitmap.width}")
            encoder.setDelay(200)
            encoder.addFrame(rotatedBitmap);
            bitmap.recycle()
            scaledBitmap.recycle()
            rotatedBitmap.recycle()
            LogDebug(
                    "Is recycled bitmap:${bitmap.isRecycled} scaledBitmap:${scaledBitmap.isRecycled} rotatedBitmap:${rotatedBitmap.isRecycled}")
        }
        Log.d(TAG, "Added all")
        encoder.finish();
        Log.d(TAG, "Encoding finished")
        return bos.toByteArray();
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_create_new_image)

        if (savedInstanceState != null) {
            mBurstTime = savedInstanceState.getInt(BUNDLE_BURST_TIME, 0)
            mBurstAmount = savedInstanceState.getInt(BUNDLE_BURST_AMOUNT, 0)
        }

        container_time.setOnClickListener({
            onTimeClicked()
        })
        container_amount.setOnClickListener({
            onAmountClicked()
        })

        setUpCameraInfos()

        if (mAvailableCameras.size > 0) {
            mCameraId = mAvailableCameras[0]
        }

        if (mAvailableCameras.size > 1) {
            button_switch_camera.setOnClickListener({
                if (mCameraId == mAvailableCameras[0]) {
                    mCameraId = mAvailableCameras[1]
                    button_switch_camera.setImageDrawable(
                            resources.getDrawable(R.drawable.ic_camera_rear_black_48dp))
                } else {
                    mCameraId = mAvailableCameras[0]
                    button_switch_camera.setImageDrawable(
                            resources.getDrawable(R.drawable.ic_camera_front_black_48dp))
                }

                closeCamera()
                if (camera_preview.isAvailable) {
                    openCamera(camera_preview.width, camera_preview.height)
                } else {
                    camera_preview.surfaceTextureListener = mSurfaceTextureListener
                }
            })
        } else {
            button_switch_camera.visibility = View.GONE
        }

        setBurstTexts()

        button_capture.setOnClickListener({

            container_amount.visibility = View.GONE
            container_time.visibility = View.GONE
            button_switch_camera.visibility = View.GONE
            button_capture.visibility = View.GONE

            mPictureList = emptyArray()

//            cameraSubscription =
            rx.Observable
                    .interval(0, mBurstTime.toLong() + 1, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .take(mBurstAmount)

                    .flatMap { it -> rx.Observable.just(takePicture()) }
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        Log.d(TAG, "Image: $it")
                    }, {
                        Log.e(TAG, "${it.message}")
                        container_amount.visibility = View.VISIBLE
                        container_time.visibility = View.VISIBLE
                        button_switch_camera.visibility = View.VISIBLE
                        button_capture.visibility = View.VISIBLE
                    }, {
                        container_amount.visibility = View.VISIBLE
                        container_time.visibility = View.VISIBLE
                        button_switch_camera.visibility = View.VISIBLE
                        button_capture.visibility = View.VISIBLE
                    })

        })
    }

    private var mPictureList = emptyArray<String>()

    private fun takePicture() {
        lockFocus()
    }

    /**
     * Camera state: Showing camera preview.
     */
    private val STATE_PREVIEW: Int = 0

    /**
     * Camera state: Waiting for the focus to be locked.
     */
    private val STATE_WAITING_LOCK: Int = 1

    /**
     * Camera state: Waiting for the exposure to be precapture state.
     */
    private val STATE_WAITING_PRECAPTURE = 2

    /**
     * Camera state: Waiting for the exposure state to be something other than precapture.
     */
    private val STATE_WAITING_NON_PRECAPTURE = 3

    /**
     * Camera state: Picture was taken.
     */
    private val STATE_PICTURE_TAKEN = 4

    private var mState: Int = STATE_PREVIEW


    private fun lockFocus() {
        try {
            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder!!.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START)
            // Tell #mCaptureCallback to wait for the lock.
            mState = STATE_WAITING_LOCK
            mCaptureSession!!.capture(mPreviewRequestBuilder!!.build(), mCaptureCallback,
                    mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun getGifDirectoryFile(): File {
        val mediaStorageDir = File(filesDir, "gifs");
//                File(filesDir.absolutePath + "/gifs/")
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory")
                return File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), APP_FOLDER_NAME + "/gifs/")
            }
        }
        return mediaStorageDir
    }


    private lateinit var mCameraId: String

    fun getOutputMediaFileForImage(): File {
//        val mediaStorageDir = File(Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES), APP_FOLDER_NAME + "/tmp_images/")

        var mediaStorageDir = File(filesDir, "tmp_images");
//                File(filesDir.absolutePath + "/tmp_images/")

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create internal directory")
                mediaStorageDir = File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), APP_FOLDER_NAME + "/tmp_images/")
            }
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val mediaFile = File(mediaStorageDir.path + File.separator + "IMG_" + timeStamp + ".jpg")

        return mediaFile
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    private fun stopBackgroundThread() {
        mBackgroundThread!!.quitSafely()
        try {
            mBackgroundThread!!.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun onTimeClicked() {
        val dialog = BurstTimeDialog.newInstance(mBurstTime)
        dialog.show(fragmentManager, "BurstTimingDialog")
    }

    private fun onAmountClicked() {
        val dialog = BurstAmountDialog.newInstance(mBurstAmount)
        dialog.show(fragmentManager, "BurstAmountDialog")
    }

}
