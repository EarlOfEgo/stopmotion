package com.sthagios.stopmotion.show

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.sthagios.stopmotion.R
import com.sthagios.stopmotion.utils.LogDebug
import com.sthagios.stopmotion.utils.retrieveStringParameter
import kotlinx.android.synthetic.main.activity_show_gif.*
import java.io.File

class ShowGifActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_gif)

        val file = retrieveStringParameter()
        LogDebug(file)


//        val mResultIntent = Intent("com.sthagios.stopmotion.ACTION_RETURN_FILE");
//        setResult(Activity.RESULT_CANCELED, null);


        val imagePath = File(filesDir, "gifs");
        val newFile = File(imagePath, file);

        val uri = Uri.fromFile(newFile)
        Glide.with(this).load(uri).into(preview)


        share_button.setOnClickListener({
            val shareIntent = Intent();
            shareIntent.action = Intent.ACTION_SEND;
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Stopmotion");
            try {
                val shareUri = FileProvider.getUriForFile(
                        this,
                        "com.sthagios.stopmotion.fileprovider",
                        newFile);

                LogDebug("Sharing ${shareUri.toString()}")
                shareIntent.putExtra(Intent.EXTRA_STREAM, shareUri);
                shareIntent.type = "image/*";

                startActivity(Intent.createChooser(shareIntent, "Stopmotion sharing"));

            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }
}
