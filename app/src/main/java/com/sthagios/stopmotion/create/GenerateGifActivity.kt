package com.sthagios.stopmotion.create

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.Snackbar
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.gifencoder.AnimatedGifEncoder
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.image.database.Gif
import com.sthagios.stopmotion.image.database.getRealmInstance
import com.sthagios.stopmotion.list.ItemDecorator
import com.sthagios.stopmotion.show.ShowGifActivity
import com.sthagios.stopmotion.utils.*
import kotlinx.android.synthetic.main.activity_generate_gif.*
import kotlinx.android.synthetic.main.state_list_item.view.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class GenerateGifActivity : AppCompatActivity() {


    class StateAdapter(val mContext: Context, val imageList: ArrayList<String>) : RecyclerView.Adapter<StateAdapter.ViewHolder>() {

        val imageListLoading = HashMap<String, Boolean>()

        init {
            for (image in imageList) {
                imageListLoading.put(image, true)
            }
        }

        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            val image = imageList[position]
            Glide.with(mContext).load(image).into(holder!!.mImageView)
//            holder.mImageView.rotation = 90.toFloat()
            if (imageListLoading[image]!!) {
                holder.mLoadingBar.visibility = View.VISIBLE
                holder.mConvertedText.visibility = View.GONE
            } else {
                holder.mLoadingBar.visibility = View.GONE
                holder.mConvertedText.visibility = View.VISIBLE
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? {
            val view = LayoutInflater.from(parent!!.context).inflate(R.layout.state_list_item,
                    parent,
                    false);
            return StateAdapter.ViewHolder(view)
        }

        override fun getItemCount() = imageList.size

        class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
            var mImageView = itemView!!.image_view
            var mLoadingBar = itemView!!.progress_bar
            val mConvertedText = itemView!!.converted_text
        }
    }

    private lateinit var mAdapter: StateAdapter

    private lateinit var mThumbName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_gif)

        val fileName = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        mGifName = "$fileName.gif"
        mThumbName = "$fileName.PNG"

        mPictureList = retrieveStringListParameter()

        mAdapter = StateAdapter(this, mPictureList)

        image_list_recycler.setHasFixedSize(true)
        image_list_recycler.layoutManager = LinearLayoutManager(this)

        image_list_recycler.adapter = mAdapter

        image_list_recycler.addItemDecoration(ItemDecorator())

        LogDebug("Generating gifs from ${mPictureList.toString()}")

        if (getApproximateAppStarts() < 4) {
            Snackbar.make(image_list_recycler, R.string.snackbar_info_taking_time,
                    Snackbar.LENGTH_LONG)
                    .show()
        }


        storeThumbnail()
        startGifGeneration()

    }


    private lateinit var mGifName: String

    private fun startGifGeneration() {
        rx.Observable.just(
                getGifDirectoryFile())
                .map {
                    "$it/$mGifName"
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
                            onGifGenerated()
                            LogDebug("Done")
                        }
                )
    }

    private fun onGifGenerated() {
        runOnUiThread {
            Snackbar.make(gif_name, "Gif successfully generated",
                    Snackbar.LENGTH_INDEFINITE).setAction("Save", {
                deleteTempFolderContent()
                storeInDatabase()
            }).show()
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

            gif.shareUriString = FileProvider.getUriForFile(
                    this,
                    "com.sthagios.stopmotion.fileprovider",
                    newFile).toString()

            gif.fileUriString = Uri.fromFile(newFile).toString()
            gif.thumbnailUriString = mThumbUri

            LogDebug("Stored gif ${gif.toString()}")

            startActivity<ShowGifActivity>(id)
            finish()
        }
    }


    private var mThumbUri: String = ""

    private fun storeThumbnail() {
        rx.Observable.from(mPictureList)
                .first()
                .flatMap {

                    val imagePath = getThumbDirectoryFile()
                    val imageFile = File(imagePath, mThumbName)

//                    val matrix = Matrix()

                    val bitmap = BitmapFactory.decodeFile(mPictureList[0])
//                    matrix.postRotate(90.toFloat())

                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 600, 800, true)

//                    val rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width,
//                            scaledBitmap.height, matrix, true)

                    val fos = FileOutputStream(imageFile)
                    scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    fos.close()

                    rx.Observable.just(Uri.fromFile(imageFile).toString())
                }
                .subscribeOn(Schedulers.io())
                .subscribe({
                    LogDebug("Thumb created under $it")
                    mThumbUri = it
                }, {
                    e ->
                    LogError(e.message!!)
                }, { LogDebug("Thumb created") })
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

    private fun generateGIF(): ByteArray {

        showWhichThreadInLogcat()
        val bos = ByteArrayOutputStream()
        //Use glide gif encoder
        val encoder = AnimatedGifEncoder()
        encoder.start(bos)
        encoder.setRepeat(0)
        LogDebug("Start gif encoding")
        for (path in mPictureList) {

            runOnUiThread {
                mAdapter.imageListLoading.put(path, false)
                mAdapter.notifyDataSetChanged()
            }

//            val matrix = Matrix()

            val bitmap = BitmapFactory.decodeFile(path)
//            matrix.postRotate(90.toFloat())

            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 600, 800, true)

//            val rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width,
//                    scaledBitmap.height, matrix, true)

            LogDebug("Adding Frame: height:${scaledBitmap.height} + width:${scaledBitmap.width}")
            encoder.setDelay(200)
            encoder.addFrame(scaledBitmap);
            bitmap.recycle()
            scaledBitmap.recycle()
            scaledBitmap.recycle()
            LogDebug(
                    "Is recycled bitmap:${bitmap.isRecycled} scaledBitmap:${scaledBitmap.isRecycled} rotatedBitmap:${scaledBitmap.isRecycled}")
        }
        LogDebug("Added all")
        encoder.finish();
        LogDebug("Encoding finished")
        return bos.toByteArray();
    }
}
