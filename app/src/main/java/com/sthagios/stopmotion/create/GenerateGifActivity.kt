package com.sthagios.stopmotion.create

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.Animatable
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.FileProvider
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import com.bumptech.glide.Glide
import com.bumptech.glide.gifencoder.AnimatedGifEncoder
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.image.database.Gif
import com.sthagios.stopmotion.image.database.getRealmInstance
import com.sthagios.stopmotion.image.storage.getGifDirectoryFile
import com.sthagios.stopmotion.image.storage.getThumbDirectoryFile
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_gif)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.generate_title)

        val fileName = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        mGifName = "$fileName.gif"
        mThumbName = "$fileName.PNG"

        mPictureList = retrieveStringListParameter()

        LogDebug("Generating gifs from $mPictureList")

        if (getApproximateAppStarts() < 4) {
            Snackbar.make(gif_name, R.string.snackbar_info_taking_time,
                    Snackbar.LENGTH_LONG)
                    .show()
        }

        gif_name.setOnEditorActionListener({ textView, i, keyEvent ->
            var handleIme = false
            if (i == EditorInfo.IME_ACTION_DONE && mGifGenerated) {
                handleIme = true
                deleteTempFolderContent()
                storeInDatabase()
            }
            handleIme
        })

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

    private var mGenerationTime = 0

    private fun startGifGeneration() {
        val startTime = System.currentTimeMillis()
        rx.Observable.just(getGifDirectoryFile())
                .map { "$it/$mGifName" }
                .doOnNext { LogDebug("Gif path $it") }
                .map { FileOutputStream(it) }
                .doOnNext { t -> t?.write(generateGIF()) }
                .doOnNext { t -> t.close() }
                .subscribeOn(Schedulers.computation())
                //One second for magic
                .delay(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ LogDebug("Gif created") },
                        { LogError("${it.message}") },
                        {
                            val difference = System.currentTimeMillis() - startTime
                            mGenerationTime = (difference / 1000).toInt()
                            onGifGenerated()
                            LogDebug("Done")
                        }
                )
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private var mGifGenerated: Boolean = false

    private fun onGifGenerated() {

        val imagePath = getGifDirectoryFile()
        val newFile = File(imagePath, mGifName)
        val fileSize = newFile.length()

        val sideNodes = resources.getStringArray(R.array.generated_with)
        val sideNode = sideNodes[Random().nextInt(sideNodes.size)]

        runOnUiThread {
            mGifGenerated = true

            magic_progressbar.visibility = View.INVISIBLE
            magic_checkmark.visibility = View.VISIBLE
            button_save_gif.setOnClickListener {
                deleteTempFolderContent()
                storeInDatabase()
            }


            val gifUri = Uri.fromFile(newFile).toString()
            val target = GlideDrawableImageViewTarget(gif_preview)
            Glide.with(baseContext).load(gifUri).into(target)


            val drawable = loading_view.drawable
            if (drawable is Animatable) {
                ViewCompat.animate(loading_container)
                        .scaleY(0.toFloat())
                        .scaleX(0.toFloat())
                        .setDuration(750)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                ViewCompat.animate(container_finished)
                        .scaleY(1.toFloat()).scaleX(1.toFloat())
                        .setDuration(750)
                        .setStartDelay(750)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
            }
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

            val imagePath = getGifDirectoryFile()
            val newFile = File(imagePath, gif.fileName)

            gif.shareUriString = FileProvider.getUriForFile(this,
                    getString(R.string.fileprovider_authority), newFile).toString()

            gif.fileUriString = Uri.fromFile(newFile).toString()
            gif.thumbnailUriString = mThumbUri

            LogDebug("Stored gif $gif")
            startActivity<ShowGifActivity>(gif.id)
            finishAffinity()
        }
    }


    private var mThumbUri: String = ""

    private fun deleteTempFolderContent() {
        val directory = File(filesDir, "tmp_images")

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


    private lateinit var mPictureList: ArrayList<String>

    private fun generateGIF(): ByteArray {

        showWhichThreadInLogcat()
        val bos = ByteArrayOutputStream()
        //Use glide gif encoder
        val encoder = AnimatedGifEncoder()
        encoder.start(bos)
        encoder.setRepeat(0)

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
            var finalBitmap = bitmap

            LogDebug("Orientation: $orientation")
            if (orientation != ExifInterface.ORIENTATION_NORMAL) {
                val matrix = Matrix()

                matrix.postRotate(getRotation(orientation))
                finalBitmap = Bitmap.createBitmap(finalBitmap, 0, 0, finalBitmap.width,
                        finalBitmap.height, matrix, true)
            }

            if (path === mPictureList[0]) {
                val imagePath = getThumbDirectoryFile()
                val imageFile = File(imagePath, mThumbName)
                val fos = FileOutputStream(imageFile)
                finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.close()
                mThumbUri = Uri.fromFile(imageFile).toString()
                LogDebug("Thumb created under $mThumbUri")
            }

            LogDebug("Adding Frame: height:${finalBitmap.height} + width:${finalBitmap.width}")
            encoder.setDelay(250)
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
        encoder.finish()
        LogDebug("Encoding finished")
        return bos.toByteArray()
    }

    private fun getRotation(exif: Int): Float {
        when (exif) {
            ExifInterface.ORIENTATION_ROTATE_90 -> return 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> return 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> return 270f
        }
        return 0f
    }
}
