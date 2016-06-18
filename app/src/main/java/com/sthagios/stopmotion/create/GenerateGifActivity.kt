package com.sthagios.stopmotion.create

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityOptionsCompat
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
import com.sthagios.stopmotion.settings.COMPRESSION_HIGH
import com.sthagios.stopmotion.settings.getCompressionRate
import com.sthagios.stopmotion.show.ShowGifActivity
import com.sthagios.stopmotion.utils.*
import kotlinx.android.synthetic.main.activity_generate_gif.*
import kotlinx.android.synthetic.main.state_list_item.view.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.*
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

        var first: View? = null

        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            val image = imageList[position]
            Glide.with(mContext).load(image).into(holder!!.mImageView)
            if (imageListLoading[image]!!) {
                holder.mLoadingBar.visibility = View.VISIBLE
                holder.mConvertedText.visibility = View.GONE
            } else {
                holder.mLoadingBar.visibility = View.GONE
                holder.mConvertedText.visibility = View.VISIBLE
            }
            if (position == 0)
                first = holder.mImageView
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

    private var mCompressRate: Float = COMPRESSION_HIGH

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_gif)

        val fileName = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        mGifName = "$fileName.gif"
        mThumbName = "$fileName.PNG"

        mCompressRate = getCompressionRate()

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

            //TODO think of another solution for this
            val trans = ActivityOptionsCompat.makeSceneTransitionAnimation(this, mAdapter.first,
                    "shared_image")
            startActivity<ShowGifActivity>(gif.id, 1, trans.toBundle())
            finish()
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

            val exif = ExifInterface(path)
            val imgWidth = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 600)
            val imgLength = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 800)
            val compressedImageWidth = (imgWidth * mCompressRate).toInt()
            val compressedImageHeight = (imgLength * mCompressRate).toInt()

            LogDebug(
                    "ImgWidth $imgWidth mgLength:$imgLength CompressedWidth:$compressedImageWidth CompressedHeight:$compressedImageHeight")

            val bitmap = BitmapFactory.decodeFile(path)


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
            encoder.addFrame(finalBitmap);
            bitmap.recycle()
            finalBitmap.recycle()
            LogDebug(
                    "Is recycled bitmap:${bitmap.isRecycled} scaledBitmap:${finalBitmap.isRecycled}")
        }
        LogDebug("Added all")
        encoder.finish();
        LogDebug("Encoding finished")
        return bos.toByteArray();
    }

    private fun getRotation(exif: Int): Float {
        when (exif) {
            ExifInterface.ORIENTATION_ROTATE_90  -> return 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> return 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> return 270f
        }
        return 0f
    }

    fun decodeFile(f: File): Bitmap? {
        try {
            // Decode image size
            val options = BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(FileInputStream(f), null, options);

            // The new size we want to scale to
            val REQUIRED_SIZE = 70;

            // Find the correct scale value. It should be the power of 2.
            var scale = 1;
            while (options.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    options.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            // Decode with inSampleSize
            val options2 = BitmapFactory.Options();
            options2.inSampleSize = scale;
            return BitmapFactory.decodeStream(FileInputStream(f), null, options2);
        } catch (e: FileNotFoundException) {

        }
        return null;
    }

}
