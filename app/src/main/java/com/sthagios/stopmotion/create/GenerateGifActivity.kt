package com.sthagios.stopmotion.create

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import com.bumptech.glide.gifencoder.AnimatedGifEncoder
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.image.database.Gif
import com.sthagios.stopmotion.image.database.getRealmInstance
import com.sthagios.stopmotion.show.ShowGifActivity
import com.sthagios.stopmotion.utils.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class GenerateGifActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_gif)


        mPictureList = retrieveStringListParameter()

        LogDebug("Generating gifs from ${mPictureList.toString()}")

        startGifGeneration()
    }


    private fun startGifGeneration() {
        var gifName = ""
        rx.Observable.just(
                getGifDirectoryFile())
                .map {
                    gifName = "${SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())}.gif"
                    "$it/$gifName"
                }
                .doOnNext { LogDebug("Gif path $it") }
                .map { FileOutputStream(it) }
                .doOnNext { t -> t!!.write(generateGIF()) }
                .doOnNext { t -> t.close() }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ LogDebug("Gif created") },
                        { LogError("${it.message}") },
                        {
                            deleteTempFolderContent()
                            storeInDatabase(gifName)
                            LogDebug("Done")
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
        val mediaStorageDir = File(filesDir, "gifs");
//                File(filesDir.absolutePath + "/gifs/")
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                LogDebug("failed to create directory")
                return File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "Stopmotion" + "/gifs/")
            }
        }
        return mediaStorageDir
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
        for (path in mPictureList) {
            val matrix = Matrix()

            val bitmap = BitmapFactory.decodeFile(path)
            matrix.postRotate(90.toFloat())

            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 800, 600, true)

            val rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width,
                    scaledBitmap.height, matrix, true)

            LogDebug("Adding Frame: height:${rotatedBitmap.height} + width:${rotatedBitmap.width}")
            encoder.setDelay(200)
            encoder.addFrame(rotatedBitmap);
            bitmap.recycle()
            scaledBitmap.recycle()
            rotatedBitmap.recycle()
            LogDebug(
                    "Is recycled bitmap:${bitmap.isRecycled} scaledBitmap:${scaledBitmap.isRecycled} rotatedBitmap:${rotatedBitmap.isRecycled}")
        }
        LogDebug("Added all")
        encoder.finish();
        LogDebug("Encoding finished")
        return bos.toByteArray();
    }
}
