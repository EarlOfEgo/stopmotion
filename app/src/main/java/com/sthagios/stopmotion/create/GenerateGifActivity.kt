package com.sthagios.stopmotion.create

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.Animatable
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.Snackbar
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.gifencoder.AnimatedGifEncoder
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.image.database.Gif
import com.sthagios.stopmotion.image.database.getRealmInstance
import com.sthagios.stopmotion.settings.COMPRESSION_HIGH
import com.sthagios.stopmotion.settings.getCompressionRate
import com.sthagios.stopmotion.show.ShowGifActivity
import com.sthagios.stopmotion.utils.*
import kotlinx.android.synthetic.main.activity_generate_gif.*
import kotlinx.android.synthetic.main.toolbar.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class GenerateGifActivity : AppCompatActivity() {


    private lateinit var mThumbName: String

    private var mCompressRate: Float = COMPRESSION_HIGH

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_gif)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.generate_title)

        val fileName = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        mGifName = "$fileName.gif"
        mThumbName = "$fileName.PNG"

        mCompressRate = getCompressionRate()

        mPictureList = retrieveStringListParameter()

        LogDebug("Generating gifs from ${mPictureList.toString()}")

        if (getApproximateAppStarts() < 4) {
            Snackbar.make(gif_name, R.string.snackbar_info_taking_time,
                    Snackbar.LENGTH_LONG)
                    .show()
        }

        loading_view.setImageResource(R.drawable.animate_generate_gif)
        val drawable = loading_view.drawable
        if (drawable is Animatable)
            drawable.start()


        container_magic.visibility = View.INVISIBLE
        startGifGeneration()

    }


    private fun getAmountString(amount: Int): String {
        return getString(R.string.converting_images_amount_state, "$amount/${mPictureList.size}")
    }

    private lateinit var mGifName: String

    private fun startGifGeneration() {
        rx.Observable.just(
                getGifDirectoryFile())
                .map { "$it/$mGifName" }
                .doOnNext { LogDebug("Gif path $it") }
                .map { FileOutputStream(it) }
                .doOnNext { t -> t!!.write(generateGIF()) }
                .doOnNext { t -> t.close() }
                .subscribeOn(Schedulers.computation())
                //One second for magic
                .delay(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ LogDebug("Gif created") },
                        { LogError("${it.message}") },
                        {
                            onGifGenerated()
                            LogDebug("Done")
                        }
                )
    }

    private fun onGifGenerated() {
        runOnUiThread {
            magic_progressbar.visibility = View.INVISIBLE
            magic_checkmark.visibility = View.VISIBLE
            Snackbar.make(gif_name, "Gif successfully generated",
                    Snackbar.LENGTH_INDEFINITE).setAction("Save", {
                deleteTempFolderContent()
                storeInDatabase()
            }).show()
            val drawable = loading_view.drawable
            if (drawable is Animatable)
                drawable.stop()
            loading_view.setImageResource(R.drawable.ic_tag_faces_black_24dp)
        }
    }

    private fun storeInDatabase() {

        val gifTitle = gif_name.text.toString()

        val realm = getRealmInstance()

        realm.executeTransaction {
            val gif = realm.createObject(Gif::class.java)
            val id = Math.abs(Random().nextLong())
            gif.id = id
            gif.fileName = mGifName
            if (!TextUtils.isEmpty(gifTitle))
                gif.name = gifTitle
            else
                gif.name = "Stopmotion Gif"

            val imagePath = File(filesDir, "gifs");
            val newFile = File(imagePath, gif.fileName);

            gif.shareUriString = FileProvider.getUriForFile(this,
                    "com.sthagios.stopmotion.fileprovider", newFile).toString()

            gif.fileUriString = Uri.fromFile(newFile).toString()
            gif.thumbnailUriString = mThumbUri

            LogDebug("Stored gif ${gif.toString()}")
//
//            //TODO think of another solution for this
//            val trans = ActivityOptionsCompat.makeSceneTransitionAnimation(this, mAdapter.first,
//                    "shared_image")
//            startActivity<ShowGifActivity>(gif.id, 1, trans.toBundle())
            startActivity<ShowGifActivity>(gif.id)
            finishAffinity()
        }
    }


    private var mThumbUri: String = ""

    private fun deleteTempFolderContent() {
        val directory = File(filesDir, "tmp_images");

        rx.Observable.from(directory.listFiles())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    file ->
                    LogDebug("Deleting ${file.name}")
                    file.delete()
                }, {
                    e ->
                    e.printStackTrace()
                }, { LogDebug("Deleted all files") })

        for (file in directory.listFiles()) {
            file.delete()
        }
    }

    private fun getGifDirectoryFile(): File {
        val mediaStorageDir = File(filesDir, "gifs")
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                LogDebug("failed to create directory")
                return File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "Stopmotion" + "/gifs/")
            }
        }
        return mediaStorageDir
    }

    private fun getThumbDirectoryFile(): File {
        val mediaStorageDir = File(filesDir, "thumbs")
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                LogDebug("failed to create directory")
                return File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "Stopmotion" + "/thumbs/")
            }
        }
        return mediaStorageDir
    }


    private lateinit var mPictureList: ArrayList<String>

    private fun getCompressedWidthHeight(list: List<String>): Pair<Int, Int> {
        var compressedImageWidth = 0
        var compressedImageHeight = 0

        for (path in list) {

            val exif = ExifInterface(path)
            val imgWidth = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 600)
            val imgLength = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 800)
            val tmpWidth = (imgWidth * mCompressRate).toInt()
            val tmpHeight = (imgLength * mCompressRate).toInt()
            if (tmpWidth > compressedImageWidth)
                compressedImageWidth = tmpWidth

            if (tmpHeight > compressedImageHeight)
                compressedImageHeight = tmpHeight

            LogDebug(
                    "ImgWidth $imgWidth mgLength:$imgLength CompressedWidth:$compressedImageWidth CompressedHeight:$compressedImageHeight")


        }

        return Pair(compressedImageWidth, compressedImageHeight)
    }


    private fun generateGIF(): ByteArray {

        showWhichThreadInLogcat()
        val bos = ByteArrayOutputStream()
        //Use glide gif encoder
        val encoder = AnimatedGifEncoder()
        encoder.start(bos)
        encoder.setRepeat(0)

        val compressedImageSize = getCompressedWidthHeight(mPictureList)
        val compressedImageWidth = compressedImageSize.first
        val compressedImageHeight = compressedImageSize.second

        LogDebug("Start gif encoding")
        var i = 1
        for (path in mPictureList) {

            runOnUiThread {
                converting_info_text.text = getAmountString(i++)
            }


            val bitmap = BitmapFactory.decodeFile(path)

            val exif = ExifInterface(path)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL)

            var finalBitmap = Bitmap.createScaledBitmap(bitmap, compressedImageWidth,
                    compressedImageHeight,
                    true)

            LogDebug("Orientation: $orientation")
            if (orientation != ExifInterface.ORIENTATION_NORMAL) {
                val matrix = Matrix()

                matrix.postRotate(getRotation(orientation))
                finalBitmap = Bitmap.createBitmap(finalBitmap, 0, 0, finalBitmap.width,
                        finalBitmap.height, matrix,
                        true)
            }

            if (path === mPictureList[0]) {
                val imagePath = getThumbDirectoryFile()
                val imageFile = File(imagePath, mThumbName)
                val fos = FileOutputStream(imageFile)
                finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.close()
                mThumbUri = Uri.fromFile(imageFile).toString()
                LogDebug("Thumb created under ${mThumbUri.toString()}")
            }

            LogDebug("Adding Frame: height:${finalBitmap.height} + width:${finalBitmap.width}")
            encoder.setDelay(200)
            encoder.addFrame(finalBitmap)
            bitmap.recycle()
            finalBitmap.recycle()
            LogDebug(
                    "Is recycled bitmap:${bitmap.isRecycled} scaledBitmap:${finalBitmap.isRecycled}")
        }
        runOnUiThread {
            converting_progressbar.visibility = View.INVISIBLE
            converted_checkmark.visibility = View.VISIBLE
            container_magic.visibility = View.VISIBLE
        }
        LogDebug("Added all")
        encoder.finish();
        LogDebug("Encoding finished")
        return bos.toByteArray()
    }

    private fun getRotation(exif: Int): Float {
        when (exif) {
            ExifInterface.ORIENTATION_ROTATE_90  -> return 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> return 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> return 270f
        }
        return 0f
    }
}
